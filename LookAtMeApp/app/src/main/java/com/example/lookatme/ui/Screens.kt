package com.example.lookatme.ui

import com.example.lookatme.data.*
import android.graphics.BitmapFactory
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.roundToInt

// ─────────────────────────────────────────────────────────────────
//  CALENDAR SCREEN
// ─────────────────────────────────────────────────────────────────

@Composable
fun CalendarScreen(
    viewModel: LookAtMeViewModel,
    isEditMode: Boolean,
    onToggleEditMode: () -> Unit,
    triggerAddSheet: Boolean = false,
    onAddSheetTriggered: () -> Unit = {}
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isHijri      by viewModel.isHijriMode.collectAsState()
    val isDark       by viewModel.isDarkMode.collectAsState()
    val colors       = if (isDark) DarkThemeColors else LightThemeColors

    val visibleSlots by viewModel.visibleSlotsForSelected.collectAsState()
    val allSlots     by viewModel.slotsForSelected.collectAsState()

    val today = LocalDate.now()
    val appStart = LocalDate.of(2026, 9, 20)
    var startDate by remember { mutableStateOf(selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())) }
    var markingItem  by remember { mutableStateOf<SlotUiItem?>(null) }
    var editingItem  by remember { mutableStateOf<SlotUiItem?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    val summaries by viewModel.cachedSummaries.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    val currentWeekSum = summaries.firstOrNull { s ->
        !startDate.isBefore(s.start) && !startDate.isAfter(s.end)
    }

    val dayProgressMap = remember(summaries, startDate) {
        val map = mutableMapOf<LocalDate, Float>()
        currentWeekSum?.dayDetails?.forEach { daySum ->
            map[daySum.date] = if (daySum.totalSlotsCount > 0) {
                daySum.doneSlotsCount.toFloat() / daySum.totalSlotsCount
            } else 0f
        }
        map
    }

    // Open add sheet when triggered from TopBar pencil icon
    LaunchedEffect(triggerAddSheet) {
        if (triggerAddSheet) {
            showAddSheet = true
            onAddSheetTriggered()
        }
    }

    LaunchedEffect(selectedDate) {
        val weekMon = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
        if (selectedDate < startDate || selectedDate > startDate.plusDays(6)) {
            startDate = weekMon
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.bgApp)
    ) {
        // Day navigation header - allows navigating to previous and next weeks
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val prev = startDate.minusDays(7)
                    startDate = prev
                    viewModel.selectDate(prev)
                }
            ) {
                Text(
                    "‹",
                    fontSize = 22.sp,
                    color = colors.text2,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${startDate.dayOfMonth} ${TR_MONTHS[startDate.monthValue-1]} – " +
                    "${startDate.plusDays(6).dayOfMonth} ${TR_MONTHS[startDate.plusDays(6).monthValue-1]}",
                    fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1
                )
                val isCurrentWeek = (startDate <= today && today <= startDate.plusDays(6))
                Text(
                    if (isCurrentWeek) "Bu Hafta" else "${startDate.dayOfMonth} ${TR_MONTHS[startDate.monthValue-1]} Haftası",
                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.text3
                )
            }
            IconButton(onClick = {
                val next = startDate.plusDays(7)
                startDate = next
                viewModel.selectDate(next)
            }) {
                Text("›", fontSize = 22.sp, color = colors.text2, fontWeight = FontWeight.Bold)
            }
        }

        // Day strip with animated water fill
        Box(modifier = Modifier.padding(horizontal = 14.dp)) {
            DayStrip(
                weekStart = startDate,
                selectedDate = selectedDate,
                dayProgress = dayProgressMap,
                isDark = isDark,
                onDateSelected = { viewModel.selectDate(it) }
            )
        }

        Spacer(Modifier.height(8.dp))

        // Day header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(selectedDate.dayNameTR(), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1)
            Text(selectedDate.formatCustom(false), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.text3)
            if (selectedDate == today) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF97316).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("BUGÜN", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF97316))
                }
            } else if (selectedDate == today.minusDays(1)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF4F8EF7).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("DÜN", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4F8EF7))
                }
            }
        }

        // Slot list - shows actionable slots for today (future tasks hidden until their end time arrives)
        val displaySlots = if (isEditMode) allSlots else visibleSlots

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Future Date Info Banner in Action Mode
            if (!isEditMode && selectedDate > today && displaySlots.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.bgCard),
                        border = BorderStroke(1.dp, Color(0xFF4F8EF7).copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("⏰", fontSize = 18.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Gelecek Gün Planı", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.text1)
                                Text("Günü ve saati geldiğinde otomatik aksiyona dönüşecektir.", fontSize = 10.sp, color = colors.text3)
                            }
                            TextButton(onClick = { onToggleEditMode() }) {
                                Text("Düzenle", fontSize = 11.sp, color = Color(0xFF4F8EF7), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (displaySlots.isEmpty()) {
                item {
                    if (allSlots.isEmpty()) {
                        EmptyState(
                            "📭",
                            if (selectedDate < today) "Bu güne ait etkinlik bulunamadı" else "Bu gün etkinlik yok",
                            "Haftalık programdan ekleyebilirsin",
                            colors
                        )
                    } else {
                        EmptyState(
                            "⏳",
                            "Henüz bitiş saati gelen etkinlik yok",
                            "Etkinlikler bitiş saatleri geldiğinde otomatik olarak görünecektir",
                            colors
                        )
                    }
                }
            } else {
                items(displaySlots, key = { it.slot.id }) { item ->
                    SlotCard(
                        item = item,
                        isDark = isDark,
                        categories = categories,
                        onClick = { if (isEditMode) editingItem = item else markingItem = item }
                    )
                }

                // If today and there are future tasks whose end time hasn't arrived yet
                val remainingToday = if (selectedDate == today && !isEditMode) allSlots.size - displaySlots.size else 0
                if (remainingToday > 0) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.bgCard),
                            border = BorderStroke(1.dp, colors.border)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⏰", fontSize = 16.sp)
                                Text(
                                    "$remainingToday etkinlik daha bitiş saati gelince listenize eklenecektir.",
                                    fontSize = 11.sp,
                                    color = colors.text2
                                )
                            }
                        }
                    }
                }
            }

            // ── Haftalık Detaylı Döküm (below tasks) — only the week containing selectedDate ──
            val selectedWeekSummary = summaries.firstOrNull { s ->
                !selectedDate.isBefore(s.start) && !selectedDate.isAfter(s.end)
            }
            if (selectedWeekSummary != null) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Haftalık Detaylı Döküm", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1)
                }
                item {
                    val summary = selectedWeekSummary
                    val pctColor = when {
                        summary.pct >= 70 -> Color(0xFF16A34A)
                        summary.pct >= 40 -> Color(0xFFCA8A04)
                        else              -> Color(0xFFDC2626)
                    }
                    val animPct by animateFloatAsState(summary.pct / 100f, tween(600), label = "cal_pct_sel")
                    val trDays = listOf("Pt", "Sa", "Çr", "Pe", "Cu", "Ct", "Pa")

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.bgCard),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    val weekLabel = "${summary.start.dayOfMonth} ${TR_MONTHS[summary.start.monthValue-1]} – ${summary.end.dayOfMonth} ${TR_MONTHS[summary.end.monthValue-1]}"
                                    Text(weekLabel, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1)
                                    Text(
                                        "${summary.start.formatCustom(isHijri)} – ${summary.end.formatCustom(isHijri)}",
                                        fontSize = 11.sp, color = colors.text3
                                    )
                                }

                                // ── Lüks Cam Hap (Glass Pill) Rozet ──
                                val badgeBg = when {
                                    summary.pct >= 70 -> Color(0xFF16A34A).copy(alpha = 0.16f)
                                    summary.pct >= 40 -> Color(0xFFCA8A04).copy(alpha = 0.16f)
                                    else              -> Color(0xFFDC2626).copy(alpha = 0.16f)
                                }
                                val badgeBorder = when {
                                    summary.pct >= 70 -> Color(0xFF16A34A).copy(alpha = 0.45f)
                                    summary.pct >= 40 -> Color(0xFFCA8A04).copy(alpha = 0.45f)
                                    else              -> Color(0xFFDC2626).copy(alpha = 0.45f)
                                }
                                val badgeIcon = when {
                                    summary.pct >= 85 -> "🏆"
                                    summary.pct >= 70 -> "✨"
                                    summary.pct >= 40 -> "⚡"
                                    else              -> "🎯"
                                }
                                val statusText = when {
                                    summary.pct >= 85 -> "Mükemmel"
                                    summary.pct >= 70 -> "Başarılı"
                                    summary.pct >= 40 -> "İyi"
                                    summary.pct > 0   -> "Devam"
                                    else              -> "Başlangıç"
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(badgeBg)
                                        .border(1.dp, badgeBorder, RoundedCornerShape(20.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(badgeIcon, fontSize = 11.sp)
                                        Text(
                                            text = "%${summary.pct}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = pctColor
                                        )
                                        Text(
                                            text = statusText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = pctColor.copy(alpha = 0.9f)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))

                            // Weekly Mini Heatmap Tile Row — Turkish day initials
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                summary.dayDetails.forEach { daySum ->
                                    val dayDone = daySum.items.count { it.state == SlotState.DONE }
                                    val dayMiss = daySum.items.count { it.state == SlotState.MISSED }
                                    val dayTot = daySum.items.size
                                    val bg = when {
                                        dayTot == 0 -> colors.bgCard2
                                        dayDone > 0 -> Color(0xFF16A34A)
                                        dayMiss > 0 -> Color(0xFFDC2626)
                                        else -> colors.bgCard2
                                    }
                                    // Turkish day abbreviation (Pt=Pazartesi, Sa=Salı, Çr=Çarşamba, Pe=Perşembe, Cu=Cuma, Ct=Cumartesi, Pa=Pazar)
                                    val trDayLabel = trDays[(daySum.date.dayOfWeek.value - 1) % 7]
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(trDayLabel, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = colors.text3)
                                        Spacer(Modifier.height(2.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(bg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("${daySum.date.dayOfMonth}", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            // Modern dual-color gradient progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.bgCard2)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animPct.coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color(0xFF4F8EF7),
                                                    pctColor
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    markingItem?.let { item ->
        MarkSlotSheet(item = item, isDark = isDark, onDismiss = { markingItem = null }) { status, note ->
            viewModel.markSlot(item.slot.id, selectedDate, status, note)
        }
    }
    editingItem?.let { item ->
        EditSlotSheet(
            existingSlot = item, defaultDate = selectedDate, categories = categories, isDark = isDark,
            onDismiss = { editingItem = null },
            onAddCategory = { id, name, icon, colorHex, bgStyle -> viewModel.addCategory(id, name, icon, colorHex, bgStyle) },
            onSave = { title, emoji, sub, cat, start, end, recurring, colorHex, bgStyle ->
                viewModel.updateSlot(item.slot.copy(title=title, emoji=emoji, subtitle=sub,
                    category=cat, startTime=start, endTime=end, isRecurring=recurring, colorHex=colorHex, bgStyle=bgStyle))
            },
            onDelete = { viewModel.deleteSlot(item.slot.id) }
        )
    }
    if (showAddSheet) {
        EditSlotSheet(
            existingSlot = null, defaultDate = selectedDate, categories = categories, isDark = isDark,
            onDismiss = { showAddSheet = false },
            onAddCategory = { id, name, icon, colorHex, bgStyle -> viewModel.addCategory(id, name, icon, colorHex, bgStyle) },
            onSave = { title, emoji, sub, cat, start, end, recurring, colorHex, bgStyle ->
                val dow = selectedDate.dayOfWeek.value - 1
                viewModel.addSlot(title, emoji, sub, cat, start, end, dow,
                    if (!recurring) selectedDate else null, recurring, colorHex, bgStyle)
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  TODAY SCREEN
// ─────────────────────────────────────────────────────────────────

@Composable
fun TodayScreen(viewModel: LookAtMeViewModel) {
    val today      = LocalDate.now()
    val isHijri    by viewModel.isHijriMode.collectAsState()
    val isDark     by viewModel.isDarkMode.collectAsState()
    val colors     = if (isDark) DarkThemeColors else LightThemeColors
    val todaySlots by viewModel.todaySlots.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    var markingItem by remember { mutableStateOf<SlotUiItem?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.bgApp),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Column {
                Text(
                    text = today.formatCustom(isHijri),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = LocalTextStyle.current.copy(
                        brush = Brush.linearGradient(listOf(Color(0xFF4F8EF7), Color(0xFFA855F7))),
                        fontSize = 24.sp, fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(today.dayNameTR(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.text3)
            }
        }
        item {
            Text("ZAMAN ÇİZELGESİ", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp, color = colors.text3)
        }
        if (todaySlots.isEmpty()) {
            item { EmptyState("☀️", "Bugün etkinlik yok", "Haftalık programdan ekleyebilirsin", colors) }
        } else {
            items(todaySlots, key = { it.slot.id }) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.width(38.dp), horizontalAlignment = Alignment.End) {
                        Spacer(Modifier.height(14.dp))
                        Text(item.slot.startTime, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                            color = colors.text3, textAlign = TextAlign.End)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(16.dp))
                        Box(Modifier.size(12.dp).clip(CircleShape).background(
                            when (item.state) {
                                SlotState.DONE   -> Color(0xFF16A34A)
                                SlotState.MISSED -> Color(0xFFDC2626)
                                SlotState.ACTIVE -> Color(0xFFFBBF24)
                                else             -> colors.text3
                            }
                        ))
                        Box(Modifier.width(2.dp).height(28.dp).background(colors.bgCard2))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        SlotCard(item = item, isDark = isDark, categories = categories, onClick = { markingItem = item })
                    }
                }
            }
        }
    }

    markingItem?.let { item ->
        MarkSlotSheet(item = item, isDark = isDark, onDismiss = { markingItem = null }) { status, note ->
            viewModel.markSlot(item.slot.id, today, status, note)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  HISTORY SCREEN (GÜN GÜN DETAYLI DÖKÜM + LUCIDE ICONS)
// ─────────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(viewModel: LookAtMeViewModel) {
    val appStart = LocalDate.of(2026, 9, 20)
    val isDark   by viewModel.isDarkMode.collectAsState()
    val colors   = if (isDark) DarkThemeColors else LightThemeColors

    var selectedYear by remember { mutableStateOf(2026) }
    val summaries by viewModel.cachedSummaries.collectAsState()
    var expandedWeekIndex by remember { mutableStateOf<Int?>(null) }

    // Aggregate statistics across all summaries
    val totalDone   = summaries.sumOf { it.done }
    val totalMissed = summaries.sumOf { it.missed }
    val totalSlots  = summaries.sumOf { it.total }
    val overallPct  = if (totalSlots > 0) (totalDone * 100 / totalSlots) else 0

    // Day map for quick lookup
    val dayMap = remember(summaries) {
        summaries.flatMap { it.dayDetails }.associateBy { it.date }
    }

    // Streak calculation
    val streakDays = remember(summaries) {
        var streak = 0
        val allDays = summaries.flatMap { it.dayDetails }.sortedByDescending { it.date }
        for (day in allDays) {
            val doneCount = day.items.count { it.state == SlotState.DONE }
            if (doneCount > 0) streak++ else if (day.date < LocalDate.now()) break
        }
        if (streak == 0 && totalDone > 0) 1 else streak
    }

    // Daily streak data for Streak Evolution line chart
    val streakData = remember(summaries) {
        val today = LocalDate.now()
        listOf(
            "Jun 23" to 0,
            "Jul 15" to 0,
            "Aug 10" to 0,
            "Aug 25" to 2,
            "Sep 10" to 0,
            "Sep 20" to streakDays
        )
    }

    // Monthly completions data for Tamamlanmalar / Aylık dual-line chart (done + total)
    val monthlyData = remember(summaries) {
        val monthsShortTR = listOf("Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara")
        val allDayItems = summaries.flatMap { it.dayDetails }
        val doneByMonth = allDayItems
            .flatMap { day -> day.items.filter { it.state == SlotState.DONE }.map { day.date.monthValue } }
            .groupingBy { it }.eachCount()
        val totalByMonth = allDayItems
            .flatMap { day -> day.items.map { day.date.monthValue } }
            .groupingBy { it }.eachCount()

        monthsShortTR.mapIndexed { idx, m ->
            val monthNum = idx + 1
            Triple(m, doneByMonth[monthNum] ?: 0, totalByMonth[monthNum] ?: 0)
        }
    }

    val (bestMonthName, bestMonthPct) = remember(summaries) { calculateBestMonth(summaries) }
    val last90Pct = remember(summaries) { calculateLast90DaysPct(summaries) }
    val dayOfWeekData = remember(summaries) { calculateDayOfWeekData(summaries) }

    // Category breakdown calculation
    val categories by viewModel.allCategories.collectAsState()
    val categoryMap = remember(categories) { categories.associate { it.id to it.name } }
    fun getCatName(id: String): String = categoryMap[id] ?: CategoryLabels[id] ?: id
    fun getCatIcon(id: String): String = categories.firstOrNull { it.id == id }?.iconName ?: id
    fun getCatColor(id: String): Color = categories.firstOrNull { it.id == id }?.let { parseColorHex(it.colorHex) } ?: categoryAccent(id)

    val catStats = remember(summaries) {
        val allItems = summaries.flatMap { it.dayDetails }.flatMap { it.items }
        allItems.groupBy { it.slot.category }.mapValues { (_, items) ->
            val d = items.count { it.state == SlotState.DONE }
            val t = items.size
            val pct = if (t > 0) (d * 100 / t) else 0
            Triple(d, t, pct)
        }
    }

    val bestCat = remember(catStats) {
        catStats.maxByOrNull { it.value.third }?.key ?: "study"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.bgApp),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Column {
                Text("Alışkanlık & Raporlar", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1)
                Text("Performans ve tamamlama istatistiklerin", fontSize = 11.sp, color = colors.text3)
            }
        }

        // Top 5 Metric Stat Cards Grid (Row 1: 3 cards, Row 2: 2 cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1: Streak Card
                    DashboardStatCard(
                        iconName = "flame",
                        value = "$streakDays Gün",
                        label = "Aktif Seri",
                        sub = "Kesintisiz",
                        accentColor = Color(0xFFF97316),
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    // 2: Weekly Success Rate Card
                    DashboardStatCard(
                        iconName = "target",
                        value = "%$overallPct",
                        label = "Başarı Oranı",
                        sub = "$totalDone/$totalSlots Yapıldı",
                        accentColor = Color(0xFF16A34A),
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    // 3: Total Done Card
                    DashboardStatCard(
                        iconName = "check-circle",
                        value = "$totalDone",
                        label = "Tamamlanan",
                        sub = "$totalMissed Kaçan",
                        accentColor = Color(0xFF4F8EF7),
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 4: Peak Category Card
                    DashboardStatCard(
                        iconName = getCatIcon(bestCat),
                        value = getCatName(bestCat),
                        label = "Zirve Kategori",
                        sub = "%${catStats[bestCat]?.third ?: 0} Başarı",
                        accentColor = getCatColor(bestCat),
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                    // 5: Best Month Card
                    DashboardStatCard(
                        iconName = "calendar",
                        value = bestMonthName,
                        label = "En Başarılı Ay",
                        sub = "%$bestMonthPct Oran",
                        accentColor = Color(0xFF6366F1),
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Full Yearly Contribution Heatmap Matrix Card (< 2026 >)
        item {
            YearlyHeatmapMatrixCard(
                year = selectedYear,
                onYearChange = { selectedYear = it },
                dayMap = dayMap,
                colors = colors
            )
        }

        // Side-by-Side Analytics Cards: Tutarlılık Ring & En Tutarlı Günler Bar Chart
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ConsistencyRingChartCard(
                    pct = last90Pct,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                DayConsistencyBarChartCard(
                    dayOfWeekData = dayOfWeekData,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Line Chart: Tamamlanmalar / Aylık (dual line)
        item {
            MonthlyCompletionsChartCard(
                monthlyData = monthlyData,
                colors = colors
            )
        }

        // Category Breakdown Progress Bars Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.bgCard),
                border = BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Kategori Bazlı Başarı", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1)

                    val sortedCats = catStats.entries.sortedByDescending { it.value.third }
                    if (sortedCats.isEmpty()) {
                        Text("Henüz kategori verisi bulunmuyor", fontSize = 11.sp, color = colors.text3)
                    } else {
                        sortedCats.forEach { (cat, stats) ->
                            val (doneCount, totalCount, pct) = stats
                            val accent = getCatColor(cat)
                            val label = getCatName(cat)
                            val icon = getCatIcon(cat)
                            val animPct by animateFloatAsState(pct / 100f, tween(600), label = "cat_$cat")

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        LucideIcon(iconName = icon, category = cat, tint = accent, modifier = Modifier.size(16.dp))
                                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.text1)
                                    }
                                    Text("%$pct ($doneCount/$totalCount)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = accent)
                                }
                                LinearProgressIndicator(
                                    progress = { animPct },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(6.dp)),
                                    color = accent,
                                    trackColor = colors.bgCard2,
                                    strokeCap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

private fun calculateLast90DaysPct(summaries: List<WeekSummary>): Int {
    val today = LocalDate.now()
    val startDate = today.minusDays(89)
    val dayMap = summaries.flatMap { it.dayDetails }.associateBy { it.date }
    var done = 0
    var total = 0
    for (i in 0..89) {
        val d = startDate.plusDays(i.toLong())
        if (d >= today) continue // Sadece geçmiş günler — bugün dahil edilmez
        val sum = dayMap[d] ?: continue
        val actionableItems = sum.items.filter {
            it.state == SlotState.DONE || it.state == SlotState.MISSED
        }
        if (actionableItems.isEmpty()) continue // Henüz işlem yapılmamış günleri sayma
        done  += actionableItems.count { it.state == SlotState.DONE }
        total += actionableItems.size
    }
    return if (total > 0) (done * 100 / total) else if (done > 0) 100 else 15
}

private fun calculateDayOfWeekData(summaries: List<WeekSummary>): List<Pair<String, Int>> {
    val daysTR = listOf("P", "S", "Ç", "P", "C", "C", "P")
    val counts = IntArray(7)
    summaries.flatMap { it.dayDetails }.forEach { daySum ->
        val dayIdx = daySum.date.dayOfWeek.value - 1
        val done = daySum.items.count { it.state == SlotState.DONE }
        counts[dayIdx] += done
    }
    return daysTR.mapIndexed { idx, name -> name to counts[idx] }
}

private fun calculateBestMonth(summaries: List<WeekSummary>): Pair<String, Int> {
    val monthsTR = listOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
    val monthDone = IntArray(12)
    val monthTotal = IntArray(12)
    summaries.flatMap { it.dayDetails }.forEach { daySum ->
        val mIdx = daySum.date.monthValue - 1
        monthDone[mIdx] += daySum.items.count { it.state == SlotState.DONE }
        monthTotal[mIdx] += daySum.items.size
    }
    var bestMonthIdx = 8
    var bestPct = 0
    for (m in 0..11) {
        if (monthTotal[m] > 0) {
            val pct = (monthDone[m] * 100 / monthTotal[m])
            if (pct >= bestPct) {
                bestPct = pct
                bestMonthIdx = m
            }
        }
    }
    if (bestPct == 0 && monthDone.sum() > 0) bestPct = 100
    return monthsTR[bestMonthIdx] to (if (bestPct == 0) 100 else bestPct)
}


@Composable
fun YearlyHeatmapMatrixCard(
    year: Int,
    onYearChange: (Int) -> Unit,
    dayMap: Map<LocalDate, DaySummary>,
    colors: ThemeColors
) {
    val today = LocalDate.now()
    val firstDayOfYear = LocalDate.of(year, 1, 1)
    val jan1Mon = firstDayOfYear.minusDays((firstDayOfYear.dayOfWeek.value - 1).toLong())
    val monthsTR = listOf("Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara")
    var selectedTileDate by remember { mutableStateOf<LocalDate?>(null) }

    // Calculate initial week index of current date to auto-scroll to current month
    val currentWeekIdx = remember(today, year) {
        if (today.year == year) {
            val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(jan1Mon, today)
            (daysDiff / 7).toInt().coerceIn(0, 51)
        } else 0
    }
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = (currentWeekIdx - 3).coerceAtLeast(0))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bgCard),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Year Selector Header: < 2026 >
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onYearChange(year - 1) }, modifier = Modifier.size(28.dp)) {
                    Text("‹", fontSize = 18.sp, color = colors.text2, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Text("$year", fontSize = 20.sp, fontWeight = FontWeight.Black, color = colors.text1)
                Spacer(Modifier.width(12.dp))
                IconButton(onClick = { onYearChange(year + 1) }, modifier = Modifier.size(28.dp)) {
                    Text("›", fontSize = 18.sp, color = colors.text2, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Day of week labels on left
                Column(
                    modifier = Modifier.padding(end = 5.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Spacer(Modifier.height(20.dp)) // Aligns with month label height
                    listOf("Pt", "Sa", "Çr", "Pe", "Cu", "Ct", "Pa").forEach { dName ->
                        Box(
                            modifier = Modifier.height(18.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(dName, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = colors.text3)
                        }
                        Spacer(Modifier.height(3.dp))
                    }
                }

                // LazyRow of 52 weeks
                LazyRow(
                    state = lazyListState,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(52) { weekIdx ->
                        val weekStart = jan1Mon.plusWeeks(weekIdx.toLong())
                        val firstOfMonth = (0..6).map { weekStart.plusDays(it.toLong()) }
                            .firstOrNull { it.dayOfMonth == 1 && it.year == year }
                        val monthLabel = if (firstOfMonth != null) monthsTR[firstOfMonth.monthValue - 1] else ""

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.height(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = monthLabel,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (monthLabel.isNotBlank()) Color(0xFF4F8EF7) else colors.text3
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                for (dayIdx in 0..6) {
                                    val date = weekStart.plusDays(dayIdx.toLong())
                                    val daySum = dayMap[date]
                                    val doneCount = daySum?.items?.count { it.state == SlotState.DONE } ?: 0
                                    val isCurrentDay = date == today
                                    val isSelected = date == selectedTileDate

                                    val tileColor = when {
                                        date > today -> colors.bgCard2.copy(alpha = 0.3f)
                                        doneCount > 0 -> Color(0xFFA855F7)
                                        else -> colors.bgCard2
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(tileColor)
                                            .border(
                                                width = if (isCurrentDay) 1.5.dp else if (isSelected) 1.5.dp else 0.5.dp,
                                                color = when {
                                                    isCurrentDay -> Color(0xFFF97316)
                                                    isSelected -> Color(0xFF4F8EF7)
                                                    else -> colors.border.copy(alpha = 0.3f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .clickable {
                                                selectedTileDate = if (selectedTileDate == date) null else date
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (date.year == year) {
                                            Text(
                                                text = "${date.dayOfMonth}",
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (doneCount > 0) Color.White else colors.text3.copy(alpha = 0.8f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tile detail card on tap
            selectedTileDate?.let { date ->
                val daySum = dayMap[date]
                val doneCount = daySum?.items?.count { it.state == SlotState.DONE } ?: 0
                val totalCount = daySum?.items?.size ?: 0
                val pct = if (totalCount > 0) (doneCount * 100 / totalCount) else 0

                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.bgCard2),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "${date.dayOfMonth} ${TR_MONTHS[date.monthValue - 1]} ${date.year}",
                                    fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1
                                )
                                Text("• ${date.dayNameTR()}", fontSize = 12.sp, color = colors.text3)
                            }
                            if (date == today) {
                                Text("Bugün", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF97316))
                            }
                        }
                        if (totalCount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "$totalCount etkinlikten $doneCount tanesi tamamlandı",
                                    fontSize = 11.sp, color = colors.text2
                                )
                                Text(
                                    "%$pct",
                                    fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                                    color = if (pct >= 70) Color(0xFF16A34A) else Color(0xFFF97316)
                                )
                            }
                        } else {
                            Text(
                                if (date > today) "Gelecek gün" else "Kayıtlı etkinlik bulunmuyor",
                                fontSize = 11.sp, color = colors.text3
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConsistencyRingChartCard(
    pct: Int,
    colors: ThemeColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bgCard),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tutarlılık", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1)
                Box(
                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFA855F7).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    LucideNavIcon(name = "target", tint = Color(0xFFA855F7), modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier.size(105.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 10.dp.toPx()
                    val diameter = size.minDimension - stroke
                    val topLeft = Offset(stroke / 2, stroke / 2)

                    // Draw background track arc (270 degrees)
                    drawArc(
                        color = colors.bgCard2,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = androidx.compose.ui.geometry.Size(diameter, diameter),
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )

                    // Draw active progress arc
                    val sweep = (pct / 100f) * 270f
                    if (sweep > 0) {
                        drawArc(
                            brush = Brush.horizontalGradient(listOf(Color(0xFFA855F7), Color(0xFFC084FC))),
                            startAngle = 135f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = androidx.compose.ui.geometry.Size(diameter, diameter),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$pct%", fontSize = 18.sp, fontWeight = FontWeight.Black, color = colors.text1)
                    Text("Son 90 gün", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.text3)
                }
            }
        }
    }
}

@Composable
fun DayConsistencyBarChartCard(
    dayOfWeekData: List<Pair<String, Int>>,
    colors: ThemeColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(180.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bgCard),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("En Tutarlı Günler", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1)
                Box(
                    modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFFA855F7).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    LucideNavIcon(name = "calendar", tint = Color(0xFFA855F7), modifier = Modifier.size(14.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            val maxVal = (dayOfWeekData.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dayOfWeekData.forEach { (dayLabel, count) ->
                    val heightFactor = if (maxVal > 0) (count.toFloat() / maxVal) else 0f
                    val barHeight = (heightFactor * 65).dp.coerceAtLeast(6.dp)
                    val barColor = if (count > 0) Color(0xFFA855F7) else colors.bgCard2

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(10.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(5.dp))
                                .background(barColor)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(dayLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.text3)
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyCompletionsChartCard(
    monthlyData: List<Triple<String, Int, Int>>,  // (monthLabel, done, total)
    colors: ThemeColors
) {
    val currentMonthIdx = remember { (LocalDate.now().monthValue - 1).coerceIn(0, 11) }
    var selectedIndex by remember { mutableStateOf<Int?>(currentMonthIdx) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bgCard),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tamamlanmalar / Aylık", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1)
                }
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFA855F7).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    LucideNavIcon(name = "bar-chart", tint = Color(0xFFA855F7), modifier = Modifier.size(16.dp))
                }
            }

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFA855F7)))
                    Text("Tamamlanan", fontSize = 9.sp, color = colors.text3)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4F8EF7)))
                    Text("Toplam Görev", fontSize = 9.sp, color = colors.text3)
                }
            }

            Spacer(Modifier.height(12.dp))

            val maxVal = (monthlyData.maxOfOrNull { maxOf(it.second, it.third) } ?: 2).coerceAtLeast(2)
            val purpleColor = Color(0xFFA855F7)
            val blueColor = Color(0xFF4F8EF7)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .pointerInput(monthlyData) {
                        detectTapGestures { offset ->
                            if (monthlyData.isNotEmpty()) {
                                val w = size.width
                                val lastIdx = (monthlyData.size - 1).coerceAtLeast(1)
                                val fraction = (offset.x / w).coerceIn(0f, 1f)
                                val idx = (fraction * lastIdx).roundToInt().coerceIn(0, monthlyData.size - 1)
                                selectedIndex = idx
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height - 24.dp.toPx()
                    val gridColor = colors.border.copy(alpha = 0.4f)
                    val lastIdx = (monthlyData.size - 1).coerceAtLeast(1)

                    // Grid lines
                    for (i in 0..4) {
                        val y = h - (i.toFloat() / 4f) * h
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    if (monthlyData.isNotEmpty()) {
                        // ─── Guideline for selected month ───
                        if (selectedIndex != null && selectedIndex!! in monthlyData.indices) {
                            val selX = (selectedIndex!!.toFloat() / lastIdx) * w
                            drawLine(
                                color = purpleColor.copy(alpha = 0.45f),
                                start = Offset(selX, 0f),
                                end = Offset(selX, h),
                                strokeWidth = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                            )
                        }

                        // ─── Total (blue) line ───
                        val totalPoints = monthlyData.mapIndexed { idx, tri ->
                            val x = (idx.toFloat() / lastIdx) * w
                            val y = h - (tri.third.toFloat() / maxVal) * h
                            Offset(x, y)
                        }
                        val totalPath = Path().apply {
                            moveTo(totalPoints[0].x, totalPoints[0].y)
                            for (i in 1 until totalPoints.size) lineTo(totalPoints[i].x, totalPoints[i].y)
                        }
                        val totalFillPath = Path().apply {
                            addPath(totalPath)
                            lineTo(totalPoints.last().x, h)
                            lineTo(totalPoints.first().x, h)
                            close()
                        }
                        drawPath(totalFillPath, brush = Brush.verticalGradient(
                            colors = listOf(blueColor.copy(alpha = 0.18f), Color.Transparent), startY = 0f, endY = h
                        ))
                        drawPath(totalPath, color = blueColor,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                        // ─── Done (purple) line ───
                        val donePoints = monthlyData.mapIndexed { idx, tri ->
                            val x = (idx.toFloat() / lastIdx) * w
                            val y = h - (tri.second.toFloat() / maxVal) * h
                            Offset(x, y)
                        }
                        val donePath = Path().apply {
                            moveTo(donePoints[0].x, donePoints[0].y)
                            for (i in 1 until donePoints.size) lineTo(donePoints[i].x, donePoints[i].y)
                        }
                        val doneFillPath = Path().apply {
                            addPath(donePath)
                            lineTo(donePoints.last().x, h)
                            lineTo(donePoints.first().x, h)
                            close()
                        }
                        drawPath(doneFillPath, brush = Brush.verticalGradient(
                            colors = listOf(purpleColor.copy(alpha = 0.30f), Color.Transparent), startY = 0f, endY = h
                        ))
                        drawPath(donePath, color = purpleColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

                        // ─── Dots ───
                        donePoints.forEachIndexed { idx, pt ->
                            val isSelected = selectedIndex == idx
                            val radius = if (isSelected) 7.dp.toPx() else 4.dp.toPx()
                            if (isSelected) {
                                drawCircle(color = purpleColor.copy(alpha = 0.25f), radius = 13.dp.toPx(), center = pt)
                            }
                            drawCircle(color = purpleColor, radius = radius, center = pt)
                            drawCircle(color = if (isSelected) Color.White else purpleColor.copy(alpha = 0.3f), radius = radius / 2, center = pt)
                        }
                        totalPoints.forEachIndexed { idx, pt ->
                            val isSelected = selectedIndex == idx
                            val radius = if (isSelected) 6.dp.toPx() else 3.dp.toPx()
                            if (isSelected) {
                                drawCircle(color = blueColor.copy(alpha = 0.22f), radius = 11.dp.toPx(), center = pt)
                            }
                            drawCircle(color = blueColor, radius = radius, center = pt)
                            drawCircle(color = Color.White, radius = radius / 2f, center = pt)
                        }
                    }
                }

                // ── Floating Tooltip Badge on Chart Tap ──
                if (selectedIndex != null && selectedIndex!! in monthlyData.indices) {
                    val sel = monthlyData[selectedIndex!!]
                    val pct = if (sel.third > 0) (sel.second * 100 / sel.third) else 0
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF19192C).copy(alpha = 0.95f))
                            .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.65f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(sel.first, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFFA855F7))
                            Text("•", fontSize = 10.sp, color = colors.text3)
                            Text("${sel.second} Tamamlandı", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC084FC))
                            Text("/", fontSize = 10.sp, color = colors.text3)
                            Text("${sel.third} Toplam", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                            Text("(%$pct)", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = if (pct >= 70) Color(0xFF22C55E) else Color(0xFFF97316))
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthlyData.forEachIndexed { idx, (monthLabel, _, _) ->
                    val isSelected = selectedIndex == idx
                    Text(
                        text = monthLabel,
                        fontSize = 8.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) Color(0xFFA855F7) else colors.text3,
                        modifier = Modifier.clickable { selectedIndex = idx }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    iconName: String,
    value: String,
    label: String,
    sub: String,
    accentColor: Color,
    colors: ThemeColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bgCard),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    LucideNavIcon(name = iconName, tint = accentColor, modifier = Modifier.size(18.dp))
                }
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = accentColor)
            }
            Spacer(Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1)
            Text(sub, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.text3)
        }
    }
}

@Composable
private fun WeekTag(label: String, color: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.15f)).padding(horizontal = 10.dp, vertical = 4.dp)
    ) { Text(label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = color) }
}

// ─────────────────────────────────────────────────────────────────
//  SETTINGS SCREEN
// ─────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(viewModel: LookAtMeViewModel, onShowSnackbar: (String) -> Unit) {
    val isHijri by viewModel.isHijriMode.collectAsState()
    val isDark  by viewModel.isDarkMode.collectAsState()
    val notifOn by viewModel.notificationsEnabled.collectAsState()
    val colors  = if (isDark) DarkThemeColors else LightThemeColors
    var showProgramSheet by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colors.bgApp),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LucideNavIcon(name = "settings", tint = Color(0xFF4F8EF7), modifier = Modifier.size(22.dp))
                Text("Ayarlar", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1)
            }
            Spacer(Modifier.height(8.dp))
        }

        item { SectionLabel("Program & Rutinler", colors) }
        item {
            SettingsCard(colors) {
                SettingsRow(
                    iconName = "calendar",
                    title = "Haftalık Program",
                    subtitle = "Ders ve etkinlik rutinlerini görüntüle, düzenle veya yeni ekle",
                    onClick = { showProgramSheet = true },
                    colors = colors
                )
            }
        }


        item { SectionLabel("Bildirimler", colors) }
        item {
            SettingsCard(colors) {
                SettingsToggleRow(
                    iconName = "bell",
                    title = "Etkinlik Bildirimleri",
                    subtitle = if (notifOn) "Görev saati bitiminde hatırlatıcı gönderilir" else "Bildirimler kapalı",
                    checked = notifOn,
                    onCheckedChange = { viewModel.toggleNotifications(it) },
                    colors = colors
                )
            }
        }
    }

    if (showProgramSheet) {
        ScheduleManagerSheet(
            viewModel = viewModel,
            isDark = isDark,
            onDismiss = { showProgramSheet = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  SCHEDULE MANAGER SHEET (HAFTALIK PROGRAM DÜZENLEME)
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleManagerSheet(
    viewModel: LookAtMeViewModel,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val colors = if (isDark) DarkThemeColors else LightThemeColors
    val allSlots by viewModel.allActiveSlots.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    var selectedDayOfWeek by remember { mutableStateOf(0) } // 0=Pazartesi .. 6=Pazar
    var editingSlot by remember { mutableStateOf<SlotEntity?>(null) }
    var showAddSlot by remember { mutableStateOf(false) }

    val dayNames = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
    val fullDayNames = listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.bgApp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Haftalık Program",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.text1
                    )
                    Text(
                        "Rutin ders ve etkinlikleri yönet",
                        fontSize = 12.sp,
                        color = colors.text3
                    )
                }
                FilledTonalButton(
                    onClick = { showAddSlot = true },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF4F8EF7),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("+ Yeni Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Day Selector Tabs (Pzt, Sal, Çar, Per, Cum, Cmt, Paz)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.bgCard)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayNames.forEachIndexed { index, dayName ->
                    val isSelected = selectedDayOfWeek == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (isSelected) Modifier.background(Brush.linearGradient(listOf(Color(0xFF4F8EF7), Color(0xFFA855F7))))
                                else Modifier.background(Color.Transparent)
                            )
                            .clickable { selectedDayOfWeek = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else colors.text3
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "${fullDayNames[selectedDayOfWeek]} Programı",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text2
            )

            Spacer(Modifier.height(8.dp))

            val daySlots = allSlots.filter { it.isRecurring && it.dayOfWeek == selectedDayOfWeek }
                .sortedBy { it.startTime }

            if (daySlots.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📅", fontSize = 36.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${fullDayNames[selectedDayOfWeek]} için kayıtlı etkinlik yok",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text3
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { showAddSlot = true }) {
                            Text("+ Bu güne etkinlik ekle", color = Color(0xFF4F8EF7), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(daySlots, key = { it.id }) { slot ->
                        val accent = parseColorHex(slot.colorHex, categoryAccent(slot.category, slot.colorHex))
                        val slotIconName = slot.emoji.ifBlank {
                            allCategories.firstOrNull { it.id == slot.category }?.iconName ?: slot.category
                        }
                        val bgBitmap = remember(slot.bgStyle) {
                            val s = slot.bgStyle
                            if (s.isNotBlank() && s != "default" && !s.startsWith("gradient_") && s != "solid") {
                                try {
                                    if (File(s).exists()) BitmapFactory.decodeFile(s)?.asImageBitmap() else null
                                } catch (_: Exception) { null }
                            } else null
                        }
                        val textPrimary = if (bgBitmap != null) (if (isDark) Color.White else colors.text1) else colors.text1
                        val textTertiary = if (bgBitmap != null) (if (isDark) Color.White.copy(alpha = 0.72f) else colors.text3) else colors.text3

                        Card(
                            onClick = { editingSlot = slot },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.bgCard),
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (bgBitmap != null) {
                                    Image(
                                        bitmap = bgBitmap,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.matchParentSize()
                                    )
                                    val scrimBrush = if (isDark) {
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Black.copy(alpha = 0.62f),
                                                Color.Black.copy(alpha = 0.88f)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.78f),
                                                Color.White.copy(alpha = 0.94f)
                                            )
                                        )
                                    }
                                    Box(modifier = Modifier.matchParentSize().background(scrimBrush))
                                }
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (bgBitmap != null) {
                                                    if (isDark) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.85f)
                                                } else accent.copy(alpha = 0.15f)
                                            )
                                            .then(if (bgBitmap != null) Modifier.border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp)) else Modifier),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        LucideIcon(title = slot.title, category = slot.category, iconName = slotIconName, tint = accent, modifier = Modifier.size(20.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            slot.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                        Text(
                                            "${slot.startTime} – ${slot.endTime}" + if (slot.subtitle.isNotBlank()) " • ${slot.subtitle}" else "",
                                            fontSize = 11.sp,
                                            color = textTertiary
                                        )
                                    }
                                    Text("✏️ Düzenle", fontSize = 11.sp, color = if (bgBitmap != null && isDark) Color(0xFF93C5FD) else Color(0xFF4F8EF7), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingSlot?.let { slot ->
        EditSlotSheet(
            existingSlot = SlotUiItem(slot, SlotState.FUTURE, null),
            defaultDate = LocalDate.now(),
            categories = allCategories,
            onAddCategory = { id, name, icon, colorHex, bgStyle ->
                viewModel.addCategory(id, name, icon, colorHex, bgStyle)
            },
            isDark = isDark,
            onDismiss = { editingSlot = null },
            onSave = { title, emoji, sub, cat, start, end, recurring, colorHex, bgStyle ->
                viewModel.updateSlot(slot.copy(
                    title = title, emoji = emoji, subtitle = sub,
                    category = cat, startTime = start, endTime = end,
                    dayOfWeek = if (recurring) selectedDayOfWeek else -1,
                    isRecurring = recurring,
                    colorHex = colorHex,
                    bgStyle = bgStyle
                ))
            },
            onDelete = {
                viewModel.deleteSlot(slot.id)
            }
        )
    }

    if (showAddSlot) {
        EditSlotSheet(
            existingSlot = null,
            defaultDate = LocalDate.now(),
            categories = allCategories,
            onAddCategory = { id, name, icon, colorHex, bgStyle ->
                viewModel.addCategory(id, name, icon, colorHex, bgStyle)
            },
            isDark = isDark,
            onDismiss = { showAddSlot = false },
            onSave = { title, emoji, sub, cat, start, end, recurring, colorHex, bgStyle ->
                viewModel.addSlot(
                    title = title, emoji = emoji, subtitle = sub,
                    category = cat, startTime = start, endTime = end,
                    dayOfWeek = selectedDayOfWeek,
                    specificDate = null,
                    isRecurring = true,
                    colorHex = colorHex,
                    bgStyle = bgStyle
                )
            }
        )
    }
}

@Composable
private fun SectionLabel(title: String, colors: ThemeColors) {
    Text(title.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.2.sp, color = colors.text3)
}

@Composable
private fun SettingsCard(colors: ThemeColors, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bgCard),
        border = BorderStroke(1.dp, colors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors == LightThemeColors) 1.dp else 0.dp)
    ) { Column { content() } }
}

@Composable
private fun SettingsRow(iconName: String, title: String, subtitle: String, onClick: () -> Unit, colors: ThemeColors) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LucideNavIcon(name = iconName, tint = Color(0xFF4F8EF7), modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.text1)
            Text(subtitle, fontSize = 11.sp, color = colors.text3)
        }
        Text("›", fontSize = 16.sp, color = colors.text3)
    }
}

@Composable
private fun SettingsToggleRow(
    iconName: String,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: ThemeColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LucideNavIcon(name = iconName, tint = Color(0xFF4F8EF7), modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.text1)
            Text(subtitle, fontSize = 11.sp, color = colors.text3)
        }
        SleekSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            activeColor = Color(0xFFA855F7),
            inactiveColor = colors.border
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  Shared helpers
// ─────────────────────────────────────────────────────────────────

@Composable
fun EmptyState(icon: String, title: String, subtitle: String, colors: ThemeColors = DarkThemeColors) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(icon, fontSize = 40.sp)
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.text2)
        Text(subtitle, fontSize = 12.sp, color = colors.text3, textAlign = TextAlign.Center)
    }
}

fun mondayOf(date: LocalDate): LocalDate {
    val dow = date.dayOfWeek.value  // Mon=1
    return date.minusDays((dow - 1).toLong())
}

fun weekNumber(monday: LocalDate): Int {
    val appStart  = LocalDate.of(2026, 9, 20)
    val appMonday = mondayOf(appStart)
    val diff      = ChronoUnit.WEEKS.between(appMonday, monday)
    return (diff + 1).toInt().coerceAtLeast(1)
}
