package com.example.lookatme

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
            val prefs = getSharedPreferences("lookatme_prefs", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("cleared_initial_hardcoded_v2", false)) {
                repo.clearAllSlots()
                prefs.edit().putBoolean("cleared_initial_hardcoded_v2", true).apply()
            }
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
            when (currentScreen) {
                Screen.CALENDAR -> CalendarScreen(
                    viewModel = viewModel,
                    isEditMode = isEditMode,
                    onToggleEditMode = { isEditMode = !isEditMode }
                )
                Screen.REPORT   -> HistoryScreen(viewModel)
                Screen.SETTINGS -> SettingsScreen(viewModel) { msg ->
                    scope.launch { snackbarHostState.showSnackbar(msg) }
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
    val view = androidx.compose.ui.platform.LocalView.current

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
            color = Color(0xFF141424),
            shadowElevation = 18.dp,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth(0.90f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isActive = currentScreen == item.screen
                    val iconScale by animateFloatAsState(
                        targetValue = if (isActive) 1.14f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
                        label = "icon_bounce"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (isActive)
                                    Brush.linearGradient(
                                        listOf(
                                            Color(0xFF4F8EF7).copy(alpha = 0.28f),
                                            Color(0xFFA855F7).copy(alpha = 0.28f)
                                        )
                                    )
                                else
                                    Brush.linearGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.Transparent
                                        )
                                    )
                            )
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                if (currentScreen != item.screen) {
                                    try {
                                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                    } catch (_: Exception) {}
                                    onNavigate(item.screen)
                                }
                            }
                            .padding(horizontal = if (isActive) 18.dp else 12.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.graphicsLayer(
                                    scaleX = iconScale,
                                    scaleY = iconScale
                                )
                            ) {
                                LucideNavIcon(
                                    name = item.iconName,
                                    tint = if (isActive) Color(0xFFA855F7) else Color(0xFF9CA3AF),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            AnimatedVisibility(
                                visible = isActive,
                                enter = fadeIn(tween(180)) + expandHorizontally(
                                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f)
                                ),
                                exit = fadeOut(tween(120)) + shrinkHorizontally(
                                    animationSpec = spring(dampingRatio = 0.75f, stiffness = 420f)
                                )
                            ) {
                                Text(
                                    item.label,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFA855F7),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
