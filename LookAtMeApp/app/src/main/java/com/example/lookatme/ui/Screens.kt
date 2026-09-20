package com.example.lookatme.ui

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
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
    var startDate by remember { mutableStateOf(if (selectedDate < today) today else selectedDate) }
    var markingItem  by remember { mutableStateOf<SlotUiItem?>(null) }
    var editingItem  by remember { mutableStateOf<SlotUiItem?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }
    var summaries by remember { mutableStateOf<List<WeekSummary>>(emptyList()) }
    var expandedWeekIndex by remember { mutableStateOf<Int?>(null) }

    // Open add sheet when triggered from TopBar pencil icon
    LaunchedEffect(triggerAddSheet) {
        if (triggerAddSheet) {
            showAddSheet = true
            onAddSheetTriggered()
        }
    }

    // Reload summaries whenever slots/completions change (fixes stale weekly summary bug)
    LaunchedEffect(allSlots) { summaries = viewModel.weekSummaries(appStart) }

    LaunchedEffect(selectedDate) {
        if (selectedDate < startDate || selectedDate > startDate.plusDays(6)) {
            startDate = if (selectedDate < today) today else selectedDate
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(colors.bgApp)
    ) {
        // Day navigation header starting from Today
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val canGoBack = startDate > today
            IconButton(
                onClick = {
                    val prev = startDate.minusDays(7)
                    val target = if (prev < today) today else prev
                    startDate = target
                    viewModel.selectDate(target)
                },
                enabled = canGoBack
            ) {
                Text(
                    "‹",
                    fontSize = 22.sp,
                    color = if (canGoBack) colors.text2 else colors.text3.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${startDate.dayOfMonth} ${TR_MONTHS[startDate.monthValue-1]} – " +
                    "${startDate.plusDays(6).dayOfMonth} ${TR_MONTHS[startDate.plusDays(6).monthValue-1]}",
                    fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1
                )
                Text(
                    if (startDate == today) "Bugünden İtibaren" else "${startDate.dayOfMonth} ${TR_MONTHS[startDate.monthValue-1]}",
                    fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.text3
                )
            }
            IconButton(onClick = {
                startDate = startDate.plusDays(7)
                viewModel.selectDate(startDate)
            }) {
                Text("›", fontSize = 22.sp, color = colors.text2, fontWeight = FontWeight.Bold)
            }
        }

        // Day strip starting from Today
        Box(modifier = Modifier.padding(horizontal = 14.dp)) {
            DayStrip(
                weekStart = startDate,
                selectedDate = selectedDate,
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
            Text(selectedDate.formatCustom(isHijri), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.text3)
        }

        // Slot list
        val displaySlots = if (isEditMode) allSlots else visibleSlots

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Günlük İlerleme (above tasks) ──
            if (selectedDate <= today && (displaySlots.isNotEmpty() || allSlots.isNotEmpty())) {
                item {
                    ProgressCard(items = if (isEditMode) allSlots else allSlots, isDark = isDark)
                }
            }

            when {
                // Future Date Notification Card in Action Mode
                !isEditMode && selectedDate > today -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.bgCard),
                            border = BorderStroke(1.dp, Color(0xFF4F8EF7).copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.size(48.dp).clip(CircleShape)
                                        .background(Color(0xFF4F8EF7).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⏰", fontSize = 22.sp)
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "${selectedDate.dayOfMonth} ${TR_MONTHS[selectedDate.monthValue-1]} Etkinlikleri",
                                    fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Bu günün etkinlikleri günü ve saati gelince otomatik olarak aksiyon listenizde görüntülenecektir.",
                                    fontSize = 12.sp, color = colors.text2, textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(colors.bgCard2)
                                        .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                                        .clickable { onToggleEditMode() }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text("✏️ Planı Düzenle / Yeni Ekle", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F8EF7))
                                }
                            }
                        }
                    }
                }

                displaySlots.isEmpty() && allSlots.isEmpty() ->
                    item { EmptyState("📭", "Bu gün etkinlik yok", "+ ile etkinlik ekleyebilirsin", colors) }

                displaySlots.isEmpty() ->
                    item { EmptyState("⏳", "Henüz saati gelen etkinlik yok", "Etkinlikler saatleri gelince otomatik görünecek", colors) }

                else -> {
                    items(displaySlots, key = { it.slot.id }) { item ->
                        SlotCard(
                            item = item,
                            isDark = isDark,
                            onClick = { if (isEditMode) editingItem = item else markingItem = item }
                        )
                    }
                    val remaining = allSlots.size - displaySlots.size
                    if (!isEditMode && remaining > 0 && selectedDate == today) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.bgCard)
                                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⏰ $remaining etkinlik daha saati gelince aksiyon listenize eklenecek",
                                    fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.text3)
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
                    val isExpanded = expandedWeekIndex == 0
                    val pctColor = when {
                        summary.pct >= 70 -> Color(0xFF16A34A)
                        summary.pct >= 40 -> Color(0xFFCA8A04)
                        else              -> Color(0xFFDC2626)
                    }
                    val animPct by animateFloatAsState(summary.pct / 100f, tween(600), label = "cal_pct_sel")
                    val trDays = listOf("Pt", "Sa", "Çr", "Pe", "Cu", "Ct", "Pa")

                    Card(
                        onClick = { expandedWeekIndex = if (isExpanded) null else 0 },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.bgCard),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        val weekLabel = "${summary.start.dayOfMonth} ${TR_MONTHS[summary.start.monthValue-1]} – ${summary.end.dayOfMonth} ${TR_MONTHS[summary.end.monthValue-1]}"
                                        Text(weekLabel, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1)
                                        Text(if (isExpanded) "▲" else "▼", fontSize = 10.sp, color = colors.text3)
                                    }
                                    Text(
                                        "${summary.start.formatCustom(isHijri)} – ${summary.end.formatCustom(isHijri)}",
                                        fontSize = 11.sp, color = colors.text3
                                    )
                                }
                                Text("%${summary.pct}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = pctColor)
                            }
                            Spacer(Modifier.height(10.dp))

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

                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { animPct },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(6.dp)),
                                color = pctColor, trackColor = colors.bgCard2,
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                WeekTag("✓ ${summary.done} Yapıldı", Color(0xFF16A34A))
                                WeekTag("✕ ${summary.missed} Yapılmadı", Color(0xFFDC2626))
                                WeekTag("○ ${summary.total - summary.done - summary.missed} Bekliyor", colors.text3)
                            }

                            // Day-by-day expandable detail breakdown
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier.padding(top = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    HorizontalDivider(color = colors.border)
                                    Text("GÜN GÜN DÖKÜM", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, color = colors.text3)

                                    summary.dayDetails.forEach { daySum ->
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                "${daySum.date.dayNameTR()} • ${daySum.date.formatCustom(isHijri)}",
                                                fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4F8EF7)
                                            )
                                            daySum.items.forEach { uiItem ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                                        .background(colors.bgCard2).padding(horizontal = 10.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        LucideIcon(title = uiItem.slot.title, category = uiItem.slot.category, tint = categoryAccent(uiItem.slot.category), modifier = Modifier.size(18.dp))
                                                        Column {
                                                            Text(uiItem.slot.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.text1)
                                                            Text("${uiItem.slot.startTime} – ${uiItem.slot.endTime}", fontSize = 10.sp, color = colors.text3)
                                                            if (uiItem.completion?.note?.isNotBlank() == true) {
                                                                Text("💬 ${uiItem.completion.note}", fontSize = 10.sp, color = colors.text2)
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = when (uiItem.state) {
                                                            SlotState.DONE   -> "✓ Yapıldı"
                                                            SlotState.MISSED -> "✕ Yapılmadı"
                                                            else             -> "○ Bekliyor"
                                                        },
                                                        fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                                                        color = when (uiItem.state) {
                                                            SlotState.DONE   -> Color(0xFF16A34A)
                                                            SlotState.MISSED -> Color(0xFFDC2626)
                                                            else             -> colors.text3
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
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
            existingSlot = item, defaultDate = selectedDate, isDark = isDark,
            onDismiss = { editingItem = null },
            onSave = { title, emoji, sub, cat, start, end, recurring ->
                viewModel.updateSlot(item.slot.copy(title=title, emoji=emoji, subtitle=sub,
                    category=cat, startTime=start, endTime=end, isRecurring=recurring))
            },
            onDelete = { viewModel.deleteSlot(item.slot.id) }
        )
    }
    if (showAddSheet) {
        EditSlotSheet(
            existingSlot = null, defaultDate = selectedDate, isDark = isDark,
            onDismiss = { showAddSheet = false },
            onSave = { title, emoji, sub, cat, start, end, recurring ->
                val dow = selectedDate.dayOfWeek.value - 1
                viewModel.addSlot(title, emoji, sub, cat, start, end, dow,
                    if (!recurring) selectedDate else null, recurring)
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
            item { EmptyState("☀️", "Bugün etkinlik yok", "Takvim sekmesinden etkinlik ekle", colors) }
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
                        SlotCard(item = item, isDark = isDark, onClick = { markingItem = item })
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
    val isHijri  by viewModel.isHijriMode.collectAsState()
    val isDark   by viewModel.isDarkMode.collectAsState()
    val colors   = if (isDark) DarkThemeColors else LightThemeColors

    var selectedYear by remember { mutableStateOf(2026) }
    var summaries by remember { mutableStateOf<List<WeekSummary>>(emptyList()) }
    var expandedWeekIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) { summaries = viewModel.weekSummaries(appStart) }

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
                        label = "Haftalık Oran",
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
                        iconName = "award",
                        value = CategoryLabels[bestCat] ?: bestCat,
                        label = "Zirve Kategori",
                        sub = "%${catStats[bestCat]?.third ?: 0} Başarı",
                        accentColor = Color(0xFFA855F7),
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
                            val accent = categoryAccent(cat)
                            val label = CategoryLabels[cat] ?: cat
                            val animPct by animateFloatAsState(pct / 100f, tween(600), label = "cat_$cat")

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        LucideIcon(category = cat, tint = accent, modifier = Modifier.size(16.dp))
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

            LazyRow(
                state = lazyListState,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(52) { weekIdx ->
                    val weekStart = jan1Mon.plusWeeks(weekIdx.toLong())
                    val monthLabel = if (weekStart.dayOfMonth <= 7) monthsTR[weekStart.monthValue - 1] else ""

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = monthLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text3,
                            modifier = Modifier.height(16.dp)
                        )
                        Spacer(Modifier.height(8.dp)) // Added spacing so text doesn't touch tiles!
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (dayIdx in 0..6) {
                                val date = weekStart.plusDays(dayIdx.toLong())
                                val daySum = dayMap[date]
                                val doneCount = daySum?.items?.count { it.state == SlotState.DONE } ?: 0
                                val totalCount = daySum?.items?.size ?: 0
                                val pct = if (totalCount > 0) (doneCount * 100 / totalCount) else 0

                                // Distinct tile colors for Past vs Completed vs Future
                                val tileColor = when {
                                    date > today -> colors.bgCard2.copy(alpha = 0.35f) // Faded future day
                                    doneCount > 0 -> Color(0xFFA855F7) // Vibrant active purple
                                    else -> colors.bgCard2 // Past empty day (Dark tile)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(11.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(tileColor)
                                )
                            }
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
                    if (selectedIndex != null && selectedIndex!! in monthlyData.indices) {
                        val sel = monthlyData[selectedIndex!!]
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("${sel.first}: ${sel.second} Tamamlandı", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA855F7))
                            Text("/ ${sel.third} Toplam", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4F8EF7))
                        }
                    }
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
                modifier = Modifier.fillMaxWidth().height(140.dp)
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
                            drawCircle(color = purpleColor, radius = radius, center = pt)
                            drawCircle(color = if (isSelected) Color.White else purpleColor.copy(alpha = 0.3f), radius = radius / 2, center = pt)
                        }
                        totalPoints.forEachIndexed { idx, pt ->
                            val isSelected = selectedIndex == idx
                            val radius = if (isSelected) 6.dp.toPx() else 3.dp.toPx()
                            drawCircle(color = blueColor, radius = radius, center = pt)
                            drawCircle(color = Color.White, radius = radius / 2f, center = pt)
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

        item { SectionLabel("Görünüm & Tema", colors) }
        item {
            SettingsCard(colors) {
                SettingsToggleRow(
                    iconName = if (isDark) "moon" else "sun",
                    title = "Karanlık / Aydınlık Mod",
                    subtitle = if (isDark) "Aktif: Karanlık Tema" else "Aktif: Aydınlık (Warm Light)",
                    checked = isDark,
                    onCheckedChange = { viewModel.toggleDarkMode(it) },
                    colors = colors
                )
                HorizontalDivider(color = colors.border)
                SettingsToggleRow(
                    iconName = "calendar",
                    title = "Hicri Takvim Görünümü",
                    subtitle = if (isHijri) "Aktif: Hicri (Umm al-Qura)" else "Aktif: Miladi",
                    checked = isHijri,
                    onCheckedChange = { viewModel.toggleHijriMode(it) },
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

        item {
            Spacer(Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "LookAtMe v1.2 • 2026",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text3
                )
            }
            Spacer(Modifier.height(20.dp))
        }
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
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LucideNavIcon(name = iconName, tint = Color(0xFF4F8EF7), modifier = Modifier.size(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.text1)
            Text(subtitle, fontSize = 11.sp, color = colors.text3)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFA855F7),
                uncheckedThumbColor = colors.text3,
                uncheckedTrackColor = colors.bgCard2
            )
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
