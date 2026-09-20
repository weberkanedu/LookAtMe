package com.example.lookatme

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.lookatme.data.*
import com.example.lookatme.notification.*
import com.example.lookatme.theme.LookAtMeTheme
import com.example.lookatme.ui.*
import kotlinx.coroutines.*
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        createNotificationChannel(this)

        val db   = DatabaseProvider.get(this)
        val repo = Repository(db)

        lifecycleScope.launch(Dispatchers.IO) {
            val existing = repo.slotsForDateOnce(LocalDate.of(2026, 9, 20))
            if (existing.isEmpty()) repo.seedDefaultData()
            NotificationScheduler.scheduleForToday(this@MainActivity, repo)
        }

        val viewModel = LookAtMeViewModel(repo, applicationContext)

        setContent {
            LookAtMeTheme {
                LookAtMeApp(viewModel)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  APP ROOT
// ─────────────────────────────────────────────────────────────────

enum class Screen { CALENDAR, REPORT, SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookAtMeApp(viewModel: LookAtMeViewModel) {
    var currentScreen      by remember { mutableStateOf(Screen.CALENDAR) }
    var isEditMode         by remember { mutableStateOf(false) }
    var triggerAddSheet    by remember { mutableStateOf(false) }
    val isDark             by viewModel.isDarkMode.collectAsState()
    val colors             = if (isDark) DarkThemeColors else LightThemeColors
    val snackbarHostState  = remember { SnackbarHostState() }
    val scope              = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            viewModel.tick()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.bgApp,
        topBar = {
            TopBar(
                viewModel = viewModel,
                currentScreen = currentScreen,
                onAddTask = { triggerAddSheet = true },
                onGoToday = {
                    viewModel.selectDate(LocalDate.now())
                    currentScreen = Screen.CALENDAR
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    Screen.CALENDAR -> CalendarScreen(
                        viewModel = viewModel,
                        isEditMode = isEditMode,
                        onToggleEditMode = { isEditMode = !isEditMode },
                        triggerAddSheet = triggerAddSheet,
                        onAddSheetTriggered = { triggerAddSheet = false }
                    )
                    Screen.REPORT   -> HistoryScreen(viewModel)
                    Screen.SETTINGS -> SettingsScreen(viewModel) { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                }
            }

            // Floating Bottom Nav Bar overlay (NO solid bottom band!)
            BottomNavBar(
                viewModel = viewModel,
                currentScreen = currentScreen,
                onNavigate = { currentScreen = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  TOP BAR (WITH LOOKATME BRAND LOGO EMBLEM)
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    viewModel: LookAtMeViewModel,
    currentScreen: Screen,
    onAddTask: () -> Unit,
    onGoToday: () -> Unit
) {
    val today   = LocalDate.now()
    val isHijri by viewModel.isHijriMode.collectAsState()
    val isDark  by viewModel.isDarkMode.collectAsState()
    val colors  = if (isDark) DarkThemeColors else LightThemeColors
    val dateStr = "${today.formatCustom(isHijri)}, ${today.dayNameTR()}"

    Surface(
        color = colors.bgApp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand Logo Emblem
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF4F8EF7), Color(0xFFA855F7)))),
                contentAlignment = Alignment.Center
            ) {
                Text("L", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "LookAtMe",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = TextStyle(
                        brush = Brush.linearGradient(listOf(Color(0xFF4F8EF7), Color(0xFFA855F7)))
                    )
                )
                Text(dateStr, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.text3)
            }

            if (currentScreen == Screen.CALENDAR) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add task "+" button (Directly opens task creation sheet)
                    IconButton(
                        onClick = onAddTask,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF4F8EF7), Color(0xFFA855F7))))
                    ) {
                        Text("+", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }

                    // Go to today icon button
                    IconButton(
                        onClick = onGoToday,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.bgCard)
                            .border(1.dp, colors.border, CircleShape)
                    ) {
                        LucideNavIcon(
                            name = "today",
                            tint = colors.text1,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  BOTTOM NAV — Floating Pill (selected = icon + label pill, unselected = icon only)
// ─────────────────────────────────────────────────────────────────

data class NavItem(val screen: Screen, val iconName: String, val label: String)

val navItems = listOf(
    NavItem(Screen.CALENDAR, "calendar", "Takvim"),
    NavItem(Screen.REPORT,   "bar-chart",  "Rapor"),
    NavItem(Screen.SETTINGS, "settings", "Ayarlar"),
)

@Composable
fun BottomNavBar(
    viewModel: LookAtMeViewModel,
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark by viewModel.isDarkMode.collectAsState()

    // Floating pill container with bottom navigation padding
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = if (isDark) Color(0xFF1A1A2E) else Color(0xFF1C1C1E),
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = if (isDark) 0.10f else 0.15f)),
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isActive = currentScreen == item.screen
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (isActive) Color(0xFFA855F7).copy(alpha = 0.22f) else Color.Transparent)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) { onNavigate(item.screen) }
                            .padding(horizontal = if (isActive) 16.dp else 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LucideNavIcon(
                                name = item.iconName,
                                tint = if (isActive) Color(0xFFA855F7) else Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                            if (isActive) {
                                Text(
                                    item.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFA855F7)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
