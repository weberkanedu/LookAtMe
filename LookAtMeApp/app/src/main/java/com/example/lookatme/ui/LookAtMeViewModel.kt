package com.example.lookatme.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lookatme.data.*
import com.example.lookatme.notification.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context
import java.time.LocalDate
import java.time.LocalTime

// ─── Slot state enum ─────────────────────────────────────────────

enum class SlotState { FUTURE, ACTIVE, PAST_PENDING, DONE, MISSED }

data class SlotUiItem(
    val slot: SlotEntity,
    val state: SlotState,
    val completion: CompletionEntity?
)

data class DaySummary(
    val date: LocalDate,
    val items: List<SlotUiItem>,
    val totalSlotsCount: Int = items.size,
    val doneSlotsCount: Int = items.count { it.state == SlotState.DONE }
)

data class WeekSummary(
    val start: LocalDate,
    val end: LocalDate,
    val total: Int,
    val done: Int,
    val missed: Int,
    val dayDetails: List<DaySummary> = emptyList()
) {
    val pct: Int get() = if (total > 0) (done * 100 / total) else 0
}

// ─── ViewModel ───────────────────────────────────────────────────

class LookAtMeViewModel(
    private val repo: Repository,
    private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("lookatme_prefs", Context.MODE_PRIVATE)

    val isHijriMode          = MutableStateFlow(prefs.getBoolean("is_hijri", false))
    val isDarkMode           = MutableStateFlow(true)
    val notificationsEnabled = MutableStateFlow(prefs.getBoolean("notif_enabled", true))

    fun toggleHijriMode(enabled: Boolean) {
        isHijriMode.value = enabled
        prefs.edit().putBoolean("is_hijri", enabled).apply()
    }

    fun toggleDarkMode(enabled: Boolean) {
        // Permanently dark mode
        isDarkMode.value = true
    }

    fun toggleNotifications(enabled: Boolean) {
        notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notif_enabled", enabled).apply()
    }

    // Currently selected date on calendar
    val selectedDate = MutableStateFlow(LocalDate.now())

    // Trigger to refresh time-based slot states every minute
    private val _timeTick = MutableStateFlow(0L)

    val slotsForSelected: StateFlow<List<SlotUiItem>> = combine(
        selectedDate.flatMapLatest { repo.slotsForDate(it) },
        selectedDate.flatMapLatest { repo.completionsForDate(it) },
        _timeTick
    ) { slots, completions, _ ->
        val date = selectedDate.value
        buildSlotUiItems(slots, completions, date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaySlots: StateFlow<List<SlotUiItem>> = combine(
        LocalDate::class.let {
            repo.slotsForDate(LocalDate.now())
        },
        repo.completionsForDate(LocalDate.now()),
        _timeTick
    ) { slots, completions, _ ->
        buildSlotUiItems(slots, completions, LocalDate.now())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visibleSlotsForSelected: StateFlow<List<SlotUiItem>> = combine(
        slotsForSelected,
        selectedDate,
        _timeTick
    ) { slots, date, _ ->
        val today = LocalDate.now()
        val now = LocalTime.now()
        when {
            date < today -> slots
            date == today -> slots.filter { item ->
                item.completion != null || LocalTime.parse(item.slot.endTime) <= now
            }
            else -> emptyList() // Future days: events only appear when their day and end time arrive
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActiveSlots: StateFlow<List<SlotEntity>> = repo.allActiveSlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repo.allCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DEFAULT_CATEGORIES)

    val cachedSummaries = MutableStateFlow<List<WeekSummary>>(emptyList())

    init {
        viewModelScope.launch {
            repo.clearDefaultCategories()
            repo.getAllCategoriesOnce()
        }
        viewModelScope.launch {
            combine(
                repo.allActiveSlots(),
                repo.allCompletions(),
                _timeTick
            ) { _, _, _ -> }.collect {
                refreshSummaries()
            }
        }
    }

    fun addCategory(id: String, name: String, iconName: String, colorHex: String, bgStyle: String = "default") {
        viewModelScope.launch {
            repo.insertCategory(CategoryEntity(id, name, iconName, colorHex, bgStyle))
        }
    }

    fun refreshSummaries() {
        viewModelScope.launch(Dispatchers.IO) {
            val s = weekSummaries(LocalDate.of(2026, 9, 20))
            cachedSummaries.value = s
        }
    }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun tick() {
        _timeTick.value = System.currentTimeMillis()
    }

    fun markSlot(slotId: Long, date: LocalDate, status: String, note: String) {
        viewModelScope.launch {
            repo.upsertCompletion(slotId, date, status, note)
            NotificationScheduler.cancelSlotNotification(context, slotId, date)
            refreshSummaries()
        }
    }

    fun unmarkSlot(slotId: Long, date: LocalDate) {
        viewModelScope.launch {
            repo.deleteCompletion(slotId, date)
            refreshSummaries()
        }
    }

    fun addSlot(
        title: String, emoji: String, subtitle: String,
        category: String, startTime: String, endTime: String,
        dayOfWeek: Int, specificDate: LocalDate?, isRecurring: Boolean,
        colorHex: String = "", bgStyle: String = "default"
    ) {
        viewModelScope.launch {
            val slot = SlotEntity(
                title = title, emoji = emoji, subtitle = subtitle,
                category = category, startTime = startTime, endTime = endTime,
                dayOfWeek = if (isRecurring) dayOfWeek else -1,
                specificDate = specificDate?.toISO() ?: "",
                isRecurring = isRecurring,
                colorHex = colorHex,
                bgStyle = bgStyle
            )
            val id = repo.insertSlot(slot)
            val targetDate = specificDate ?: LocalDate.now()
            if (notificationsEnabled.value && (!isRecurring || slot.dayOfWeek == LocalDate.now().dayOfWeek.value - 1)) {
                val inserted = slot.copy(id = id)
                NotificationScheduler.scheduleSlotEndNotification(context, inserted, targetDate)
            }
            refreshSummaries()
        }
    }

    fun updateSlot(slot: SlotEntity) {
        viewModelScope.launch {
            repo.updateSlot(slot)
            refreshSummaries()
        }
    }

    fun deleteSlot(id: Long) {
        viewModelScope.launch {
            repo.deleteSlot(id)
            refreshSummaries()
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repo.resetAllData()
        }
    }

    // History: return week summaries going back from today to app start
    // NOTE: Each week's pct is computed only from days that actually have tasks (dayDetails),
    // so a Sunday-start won't show Mon-Sat as failed.
    suspend fun weekSummaries(appStart: LocalDate): List<WeekSummary> {
        val today = LocalDate.now()
        val summaries = mutableListOf<WeekSummary>()
        // Start from appStart itself (not Monday of appStart week)
        // so success rate is only calculated from days with tasks
        var weekStart = monday(appStart)
        while (weekStart <= monday(today)) {
            val weekEnd = weekStart.plusDays(6)
            val effectiveEnd = if (weekEnd > today) today else weekEnd
            val completions = repo.completionsForRange(weekStart, effectiveEnd)
            val compMap = completions.groupBy { it.date }
            var done = 0; var missed = 0; var total = 0
            val dayDetails = mutableListOf<DaySummary>()

            for (di in 0..6) {
                val d = weekStart.plusDays(di.toLong())
                if (d > today) break
                // Skip days before appStart — don't count them as missed
                if (d < appStart) continue
                val slots = repo.slotsForDateOnce(d)
                val dayCompletions = compMap[d.toISO()] ?: emptyList()
                val uiItems = buildSlotUiItems(slots, dayCompletions, d)
                val dayTotal = slots.size
                val dayDone = uiItems.count { it.state == SlotState.DONE }

                if (d < today) {
                    total += slots.size
                    done   += dayDone
                    missed += uiItems.count { it.state == SlotState.MISSED }
                    if (uiItems.isNotEmpty()) {
                        dayDetails.add(DaySummary(d, uiItems, dayTotal, dayDone))
                    }
                } else {
                    // Bugün: Henüz bitiş saati gelmemiş ve tamamlanmamış etkinlikler
                    // haftalık dökümde listelenmesin ve başarı oranını haksız yere düşürmesin.
                    val now = LocalTime.now()
                    val actionableToday = uiItems.filter {
                        it.completion != null || LocalTime.parse(it.slot.endTime) <= now
                    }
                    total += actionableToday.size
                    done   += actionableToday.count { it.state == SlotState.DONE }
                    missed += actionableToday.count { it.state == SlotState.MISSED }
                    if (actionableToday.isNotEmpty()) {
                        dayDetails.add(DaySummary(d, actionableToday, dayTotal, dayDone))
                    }
                }
            }
            summaries.add(WeekSummary(weekStart, weekEnd, total, done, missed, dayDetails))
            weekStart = weekStart.plusWeeks(1)
        }
        return summaries.reversed()
    }

    private fun monday(date: LocalDate): LocalDate {
        val dow = date.dayOfWeek.value  // Mon=1
        return date.minusDays((dow - 1).toLong())
    }

    companion object {
        fun buildSlotUiItems(
            slots: List<SlotEntity>,
            completions: List<CompletionEntity>,
            date: LocalDate
        ): List<SlotUiItem> {
            val today = LocalDate.now()
            val now   = LocalTime.now()
            val compMap = completions.associateBy { it.slotId }

            return slots.map { slot ->
                val completion = compMap[slot.id]
                val state = when {
                    completion != null -> if (completion.status == "done") SlotState.DONE else SlotState.MISSED
                    date > today  -> SlotState.FUTURE
                    date < today  -> SlotState.PAST_PENDING
                    else -> { // today
                        val start = LocalTime.parse(slot.startTime)
                        val end   = LocalTime.parse(slot.endTime)
                        when {
                            now < start  -> SlotState.FUTURE
                            now <= end   -> SlotState.ACTIVE
                            else         -> SlotState.PAST_PENDING
                        }
                    }
                }
                SlotUiItem(slot, state, completion)
            }
        }
    }
}
