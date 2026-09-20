package com.example.lookatme.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary          = Color(0xFF4F8EF7),
    onPrimary        = Color.White,
    secondary        = Color(0xFFA855F7),
    onSecondary      = Color.White,
    background       = Color(0xFF0F1117),
    onBackground     = Color(0xFFF0F2FF),
    surface          = Color(0xFF1A1D27),
    onSurface        = Color(0xFFF0F2FF),
    surfaceVariant   = Color(0xFF20243A),
    onSurfaceVariant = Color(0xFFA0A8C8),
    outline          = Color(0xFF252940),
    error            = Color(0xFFDC2626),
    onError          = Color.White,
)

@Composable
fun LookAtMeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
