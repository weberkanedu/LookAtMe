package com.example.lookatme.ui

import com.example.lookatme.data.*
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalTime

// ─────────────────────────────────────────────────────────────────
//  CATEGORY ICON DEFINITIONS & VECTOR DRAWING HELPERS
// ─────────────────────────────────────────────────────────────────

data class CategoryIconItem(val id: String, val label: String, val emoji: String)

val ALL_CATEGORY_ICONS = listOf(
    CategoryIconItem("target", "Hedef", "🎯"),
    CategoryIconItem("zap", "Şimşek", "⚡"),
    CategoryIconItem("heart", "Sağlık", "❤️"),
    CategoryIconItem("star", "Yıldız", "⭐"),
    CategoryIconItem("brain", "Zihin", "🧠"),
    CategoryIconItem("code", "Yazılım", "💻"),
    CategoryIconItem("book", "Ders", "📚"),
    CategoryIconItem("coffee", "Mola", "☕"),
    CategoryIconItem("music", "Müzik", "🎵"),
    CategoryIconItem("flame", "Motivasyon", "🔥"),
    CategoryIconItem("trophy", "Başarı", "🏆"),
    CategoryIconItem("dumbbell", "Spor", "🏋️"),
    CategoryIconItem("palette", "Sanat", "🎨"),
    CategoryIconItem("bell", "Bildirim", "🔔"),
    CategoryIconItem("clock", "Rutin", "⏰"),
    CategoryIconItem("car", "Yolculuk", "🚗"),
    CategoryIconItem("briefcase", "İş", "💼"),
    CategoryIconItem("home", "Ev", "🏠"),
    CategoryIconItem("shopping", "Market", "🛒"),
    CategoryIconItem("camera", "Fotoğraf", "📷"),
    CategoryIconItem("bulb", "Fikir", "💡"),
    CategoryIconItem("sun", "Sabah", "☀️"),
    CategoryIconItem("moon", "Gece", "🌙"),
    CategoryIconItem("flag", "Hedef", "🚩"),
    CategoryIconItem("check", "Görev", "✅"),
    CategoryIconItem("smile", "Sosyal", "😊")
)

fun saveImageLocally(context: Context, sourceUri: Uri): String {
    return try {
        val destFile = File(context.filesDir, "slot_bg_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath
    } catch (_: Exception) {
        sourceUri.toString()
    }
}

@Composable
fun LucideIcon(
    title: String = "",
    category: String = "",
    iconName: String = "",
    tint: Color,
    modifier: Modifier = Modifier.size(20.dp)
) {
    val t = title.lowercase()
    val c = category.lowercase()
    val ic = iconName.lowercase().trim()

    val isEmoji = ic.any { Character.isSurrogate(it) || Character.getType(it) == Character.OTHER_SYMBOL.toInt() || it.code > 0x2000 }
    if (isEmoji) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(text = ic, fontSize = 14.sp)
        }
        return
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()

        when {
            // 1. Target / Bullseye (Finance / Goal)
            ic == "target" || (ic.isBlank() && (c == "finance" || t.contains("finans") || t.contains("hedef"))) -> {
                drawCircle(color = tint, radius = w * 0.44f, style = Stroke(width = strokeWidth))
                drawCircle(color = tint, radius = w * 0.26f, style = Stroke(width = strokeWidth))
                drawCircle(color = tint, radius = w * 0.08f)
            }
            // 2. Lightning / Zap (Fitness / Energy)
            ic == "zap" || (ic.isBlank() && (c == "fitness" || t.contains("fitness"))) -> {
                val bolt = Path().apply {
                    moveTo(w * 0.58f, h * 0.08f)
                    lineTo(w * 0.20f, h * 0.54f)
                    lineTo(w * 0.52f, h * 0.54f)
                    lineTo(w * 0.44f, h * 0.92f)
                    lineTo(w * 0.82f, h * 0.44f)
                    lineTo(w * 0.50f, h * 0.44f)
                    close()
                }
                drawPath(bolt, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            // 3. Heart (Health)
            ic == "heart" || (ic.isBlank() && (c == "health" || t.contains("sağlık"))) -> {
                val heart = Path().apply {
                    moveTo(w * 0.5f, h * 0.82f)
                    cubicTo(w * 0.15f, h * 0.6f, w * 0.08f, h * 0.25f, w * 0.32f, h * 0.18f)
                    cubicTo(w * 0.44f, h * 0.15f, w * 0.5f, h * 0.28f, w * 0.5f, h * 0.28f)
                    cubicTo(w * 0.5f, h * 0.28f, w * 0.56f, h * 0.15f, w * 0.68f, h * 0.18f)
                    cubicTo(w * 0.92f, h * 0.25f, w * 0.85f, h * 0.6f, w * 0.5f, h * 0.82f)
                    close()
                }
                drawPath(heart, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            // 4. Star (Productivity / Favorites)
            ic == "star" || (ic.isBlank() && (c == "productivity" || t.contains("üretken"))) -> {
                val star = Path().apply {
                    val cx = w * 0.5f; val cy = h * 0.5f
                    val outerR = w * 0.44f; val innerR = w * 0.20f
                    for (i in 0 until 10) {
                        val angle = (i * 36 - 90) * (Math.PI / 180.0)
                        val r = if (i % 2 == 0) outerR else innerR
                        val x = (cx + r * kotlin.math.cos(angle)).toFloat()
                        val y = (cy + r * kotlin.math.sin(angle)).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
                drawPath(star, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            // 5. Brain / Mindfulness
            ic == "brain" || (ic.isBlank() && (c == "mindfulness" || t.contains("farkındalık") || t.contains("meditasyon"))) -> {
                drawCircle(color = tint, center = Offset(w * 0.35f, h * 0.4f), radius = w * 0.22f, style = Stroke(width = strokeWidth))
                drawCircle(color = tint, center = Offset(w * 0.65f, h * 0.4f), radius = w * 0.22f, style = Stroke(width = strokeWidth))
                drawCircle(color = tint, center = Offset(w * 0.38f, h * 0.65f), radius = w * 0.18f, style = Stroke(width = strokeWidth))
                drawCircle(color = tint, center = Offset(w * 0.62f, h * 0.65f), radius = w * 0.18f, style = Stroke(width = strokeWidth))
                drawLine(tint, start = Offset(w * 0.5f, h * 0.25f), end = Offset(w * 0.5f, h * 0.8f), strokeWidth = strokeWidth)
            }
            // 6. Code
            ic == "code" || (ic.isBlank() && (c == "code" || t.contains("yazılım") || t.contains("kod"))) -> {
                val left = Path().apply { moveTo(w * 0.35f, h * 0.3f); lineTo(w * 0.15f, h * 0.5f); lineTo(w * 0.35f, h * 0.7f) }
                val right = Path().apply { moveTo(w * 0.65f, h * 0.3f); lineTo(w * 0.85f, h * 0.5f); lineTo(w * 0.65f, h * 0.7f) }
                drawPath(left, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawPath(right, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawLine(tint, start = Offset(w * 0.58f, h * 0.25f), end = Offset(w * 0.42f, h * 0.75f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            }
            // 7. Book / Study
            ic == "book" || (ic.isBlank() && (c == "study" || t.contains("çalışma") || t.contains("okuma") || t.contains("kitap") || t.contains("ders") || t.contains("ödev") || t.contains("deneme"))) -> {
                val path = Path().apply {
                    moveTo(w * 0.15f, h * 0.25f)
                    cubicTo(w * 0.35f, h * 0.2f, w * 0.5f, h * 0.3f, w * 0.5f, h * 0.8f)
                    cubicTo(w * 0.35f, h * 0.7f, w * 0.15f, h * 0.75f, w * 0.15f, h * 0.25f)

                    moveTo(w * 0.85f, h * 0.25f)
                    cubicTo(w * 0.65f, h * 0.2f, w * 0.5f, h * 0.3f, w * 0.5f, h * 0.8f)
                    cubicTo(w * 0.65f, h * 0.7f, w * 0.85f, h * 0.75f, w * 0.85f, h * 0.25f)
                }
                drawPath(path, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawLine(tint, start = Offset(w * 0.5f, h * 0.3f), end = Offset(w * 0.5f, h * 0.8f), strokeWidth = strokeWidth)
            }
            // 8. Music
            ic == "music" || (ic.isBlank() && (t.contains("müzik") || t.contains("şarkı") || t.contains("enstrüman") || t.contains("piyano") || t.contains("gitar"))) -> {
                drawCircle(color = tint, center = Offset(w * 0.3f, h * 0.72f), radius = w * 0.15f)
                drawCircle(color = tint, center = Offset(w * 0.72f, h * 0.62f), radius = w * 0.15f)
                drawLine(tint, start = Offset(w * 0.42f, h * 0.72f), end = Offset(w * 0.42f, h * 0.24f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.84f, h * 0.62f), end = Offset(w * 0.84f, h * 0.14f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.42f, h * 0.24f), end = Offset(w * 0.84f, h * 0.14f), strokeWidth = strokeWidth * 2f)
            }
            // 9. Flame
            ic == "flame" || (ic.isBlank() && (t.contains("motivasyon") || t.contains("ateş") || t.contains("hedef"))) -> {
                val flame = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    cubicTo(w * 0.75f, h * 0.35f, w * 0.85f, h * 0.65f, w * 0.5f, h * 0.88f)
                    cubicTo(w * 0.15f, h * 0.65f, w * 0.25f, h * 0.35f, w * 0.5f, h * 0.15f)
                    close()
                }
                drawPath(flame, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                val innerFlame = Path().apply {
                    moveTo(w * 0.5f, h * 0.5f)
                    cubicTo(w * 0.62f, h * 0.62f, w * 0.62f, h * 0.75f, w * 0.5f, h * 0.82f)
                    cubicTo(w * 0.38f, h * 0.75f, w * 0.38f, h * 0.62f, w * 0.5f, h * 0.5f)
                }
                drawPath(innerFlame, color = tint)
            }
            // 10. Trophy
            ic == "trophy" || (ic.isBlank() && (t.contains("kupa") || t.contains("başarı") || t.contains("şampiyon") || t.contains("yarışma"))) -> {
                val cup = Path().apply {
                    moveTo(w * 0.25f, h * 0.2f)
                    lineTo(w * 0.75f, h * 0.2f)
                    lineTo(w * 0.7f, h * 0.55f)
                    cubicTo(w * 0.65f, h * 0.7f, w * 0.35f, h * 0.7f, w * 0.3f, h * 0.55f)
                    close()
                }
                drawPath(cup, color = tint, style = Stroke(width = strokeWidth))
                // Handles
                drawArc(tint, startAngle = 130f, sweepAngle = 140f, useCenter = false, topLeft = Offset(w * 0.12f, h * 0.24f), size = Size(w * 0.25f, h * 0.26f), style = Stroke(strokeWidth))
                drawArc(tint, startAngle = -90f, sweepAngle = 140f, useCenter = false, topLeft = Offset(w * 0.63f, h * 0.24f), size = Size(w * 0.25f, h * 0.26f), style = Stroke(strokeWidth))
                // Stem & Base
                drawLine(tint, start = Offset(w * 0.5f, h * 0.68f), end = Offset(w * 0.5f, h * 0.82f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.3f, h * 0.82f), end = Offset(w * 0.7f, h * 0.82f), strokeWidth = strokeWidth + 1f)
            }
            // 11. Dumbbell / Fitness
            ic == "dumbbell" || (ic.isBlank() && (t.contains("spor") || t.contains("antrenman") || t.contains("gym") || t.contains("ağırlık"))) -> {
                drawLine(tint, start = Offset(w * 0.25f, h * 0.5f), end = Offset(w * 0.75f, h * 0.5f), strokeWidth = strokeWidth * 1.5f)
                drawRoundRect(tint, topLeft = Offset(w * 0.18f, h * 0.32f), size = Size(w * 0.08f, h * 0.36f), cornerRadius = CornerRadius(2.dp.toPx()))
                drawRoundRect(tint, topLeft = Offset(w * 0.10f, h * 0.38f), size = Size(w * 0.08f, h * 0.24f), cornerRadius = CornerRadius(2.dp.toPx()))
                drawRoundRect(tint, topLeft = Offset(w * 0.74f, h * 0.32f), size = Size(w * 0.08f, h * 0.36f), cornerRadius = CornerRadius(2.dp.toPx()))
                drawRoundRect(tint, topLeft = Offset(w * 0.82f, h * 0.38f), size = Size(w * 0.08f, h * 0.24f), cornerRadius = CornerRadius(2.dp.toPx()))
            }
            // 12. Palette / Art
            ic == "palette" || (ic.isBlank() && (t.contains("sanat") || t.contains("resim") || t.contains("çizim") || t.contains("tasarım"))) -> {
                val palette = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    cubicTo(w * 0.85f, h * 0.15f, w * 0.9f, h * 0.6f, w * 0.75f, h * 0.85f)
                    cubicTo(w * 0.65f, h * 0.95f, w * 0.45f, h * 0.75f, w * 0.35f, h * 0.85f)
                    cubicTo(w * 0.15f, h * 0.75f, w * 0.15f, h * 0.3f, w * 0.5f, h * 0.15f)
                    close()
                }
                drawPath(palette, color = tint, style = Stroke(width = strokeWidth))
                drawCircle(color = tint, center = Offset(w * 0.4f, h * 0.32f), radius = w * 0.06f)
                drawCircle(color = tint, center = Offset(w * 0.62f, h * 0.32f), radius = w * 0.06f)
                drawCircle(color = tint, center = Offset(w * 0.72f, h * 0.52f), radius = w * 0.06f)
            }
            // 13. Bell / Notification
            ic == "bell" || (ic.isBlank() && (t.contains("bildirim") || t.contains("alarm") || t.contains("hatırla"))) -> {
                val bell = Path().apply {
                    moveTo(w * 0.5f, h * 0.18f)
                    cubicTo(w * 0.3f, h * 0.22f, w * 0.25f, h * 0.55f, w * 0.18f, h * 0.72f)
                    lineTo(w * 0.82f, h * 0.72f)
                    cubicTo(w * 0.75f, h * 0.55f, w * 0.7f, h * 0.22f, w * 0.5f, h * 0.18f)
                }
                drawPath(bell, color = tint, style = Stroke(width = strokeWidth))
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.84f), radius = w * 0.08f)
            }
            // 14. Clock / Time
            ic == "clock" || (ic.isBlank() && (t.contains("rutin") || t.contains("zaman") || t.contains("saat"))) -> {
                drawCircle(color = tint, radius = w * 0.42f, style = Stroke(width = strokeWidth))
                drawLine(tint, start = Offset(w * 0.5f, h * 0.5f), end = Offset(w * 0.5f, h * 0.24f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                drawLine(tint, start = Offset(w * 0.5f, h * 0.5f), end = Offset(w * 0.7f, h * 0.5f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            }
            // 15. Car / Travel
            ic == "car" || (ic.isBlank() && (t.contains("araba") || t.contains("ulaşım") || t.contains("yol") || t.contains("otobüs") || t.contains("servis"))) -> {
                val car = Path().apply {
                    moveTo(w * 0.12f, h * 0.65f)
                    lineTo(w * 0.22f, h * 0.42f)
                    lineTo(w * 0.65f, h * 0.42f)
                    lineTo(w * 0.82f, h * 0.52f)
                    lineTo(w * 0.88f, h * 0.65f)
                    close()
                }
                drawPath(car, color = tint, style = Stroke(width = strokeWidth))
                drawCircle(color = tint, center = Offset(w * 0.32f, h * 0.74f), radius = w * 0.1f)
                drawCircle(color = tint, center = Offset(w * 0.72f, h * 0.74f), radius = w * 0.1f)
            }
            // 16. Briefcase / Work
            ic == "briefcase" || (ic.isBlank() && (t.contains("iş") || t.contains("ofis") || t.contains("toplantı") || t.contains("proje"))) -> {
                drawRoundRect(tint, topLeft = Offset(w * 0.15f, h * 0.32f), size = Size(w * 0.7f, h * 0.52f), cornerRadius = CornerRadius(3.dp.toPx()), style = Stroke(strokeWidth))
                val handle = Path().apply {
                    moveTo(w * 0.36f, h * 0.32f)
                    lineTo(w * 0.36f, h * 0.2f)
                    lineTo(w * 0.64f, h * 0.2f)
                    lineTo(w * 0.64f, h * 0.32f)
                }
                drawPath(handle, color = tint, style = Stroke(width = strokeWidth))
                drawLine(tint, start = Offset(w * 0.15f, h * 0.56f), end = Offset(w * 0.85f, h * 0.56f), strokeWidth = strokeWidth)
            }
            // 17. Home
            ic == "home" || (ic.isBlank() && (t.contains("ev") || t.contains("temizlik") || t.contains("oda"))) -> {
                val roof = Path().apply {
                    moveTo(w * 0.15f, h * 0.48f)
                    lineTo(w * 0.5f, h * 0.18f)
                    lineTo(w * 0.85f, h * 0.48f)
                }
                drawPath(roof, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                drawRect(tint, topLeft = Offset(w * 0.24f, h * 0.48f), size = Size(w * 0.52f, h * 0.38f), style = Stroke(strokeWidth))
                drawRect(tint, topLeft = Offset(w * 0.42f, h * 0.62f), size = Size(w * 0.16f, h * 0.24f))
            }
            // 18. Shopping
            ic == "shopping" || (ic.isBlank() && (t.contains("market") || t.contains("alışveriş") || t.contains("bakkal"))) -> {
                val cart = Path().apply {
                    moveTo(w * 0.12f, h * 0.24f)
                    lineTo(w * 0.25f, h * 0.24f)
                    lineTo(w * 0.35f, h * 0.62f)
                    lineTo(w * 0.78f, h * 0.62f)
                    lineTo(w * 0.85f, h * 0.35f)
                    lineTo(w * 0.28f, h * 0.35f)
                }
                drawPath(cart, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawCircle(color = tint, center = Offset(w * 0.42f, h * 0.76f), radius = w * 0.08f)
                drawCircle(color = tint, center = Offset(w * 0.72f, h * 0.76f), radius = w * 0.08f)
            }
            // 19. Camera
            ic == "camera" || (ic.isBlank() && (t.contains("foto") || t.contains("kamera") || t.contains("video"))) -> {
                drawRoundRect(tint, topLeft = Offset(w * 0.14f, h * 0.32f), size = Size(w * 0.72f, h * 0.52f), cornerRadius = CornerRadius(3.dp.toPx()), style = Stroke(strokeWidth))
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.58f), radius = w * 0.16f, style = Stroke(strokeWidth))
                drawRect(tint, topLeft = Offset(w * 0.35f, h * 0.22f), size = Size(w * 0.3f, h * 0.1f))
            }
            // 20. Bulb / Idea
            ic == "bulb" || (ic.isBlank() && (t.contains("fikir") || t.contains("proje") || t.contains("yaratıcı") || t.contains("plan"))) -> {
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.42f), radius = w * 0.28f, style = Stroke(strokeWidth))
                drawLine(tint, start = Offset(w * 0.38f, h * 0.72f), end = Offset(w * 0.62f, h * 0.72f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.42f, h * 0.82f), end = Offset(w * 0.58f, h * 0.82f), strokeWidth = strokeWidth)
            }
            // 21. Sun
            ic == "sun" || (ic.isBlank() && (t.contains("sabah") || t.contains("gündüz") || t.contains("kahvaltı"))) -> {
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.22f, style = Stroke(strokeWidth))
                for (i in 0 until 8) {
                    val angle = (i * 45) * (Math.PI / 180.0)
                    val r1 = w * 0.32f; val r2 = w * 0.46f
                    val x1 = (w * 0.5f + r1 * kotlin.math.cos(angle)).toFloat()
                    val y1 = (h * 0.5f + r1 * kotlin.math.sin(angle)).toFloat()
                    val x2 = (w * 0.5f + r2 * kotlin.math.cos(angle)).toFloat()
                    val y2 = (h * 0.5f + r2 * kotlin.math.sin(angle)).toFloat()
                    drawLine(tint, start = Offset(x1, y1), end = Offset(x2, y2), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }
            }
            // 22. Moon
            ic == "moon" || (ic.isBlank() && (t.contains("gece") || t.contains("uyku") || t.contains("akşam"))) -> {
                val moon = Path().apply {
                    moveTo(w * 0.65f, h * 0.15f)
                    cubicTo(w * 0.3f, h * 0.25f, w * 0.3f, h * 0.75f, w * 0.65f, h * 0.85f)
                    cubicTo(w * 0.45f, h * 0.75f, w * 0.45f, h * 0.35f, w * 0.65f, h * 0.15f)
                    close()
                }
                drawPath(moon, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }
            // 23. Flag
            ic == "flag" || (ic.isBlank() && (t.contains("bayrak") || t.contains("hedef") || t.contains("milestone"))) -> {
                drawLine(tint, start = Offset(w * 0.22f, h * 0.15f), end = Offset(w * 0.22f, h * 0.88f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                val flag = Path().apply {
                    moveTo(w * 0.22f, h * 0.18f)
                    lineTo(w * 0.80f, h * 0.32f)
                    lineTo(w * 0.22f, h * 0.48f)
                    close()
                }
                drawPath(flag, color = tint)
            }
            // 24. Check / Task
            ic == "check" || (ic.isBlank() && (t.contains("görev") || t.contains("kontrol") || t.contains("yapılacak"))) -> {
                drawRoundRect(tint, topLeft = Offset(w * 0.15f, h * 0.15f), size = Size(w * 0.7f, h * 0.7f), cornerRadius = CornerRadius(4.dp.toPx()), style = Stroke(strokeWidth))
                val check = Path().apply {
                    moveTo(w * 0.3f, h * 0.5f)
                    lineTo(w * 0.45f, h * 0.65f)
                    lineTo(w * 0.72f, h * 0.32f)
                }
                drawPath(check, color = tint, style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            // 25. Smile
            ic == "smile" || (ic.isBlank() && (t.contains("sosyal") || t.contains("arkadaş") || t.contains("eğlence"))) -> {
                drawCircle(color = tint, radius = w * 0.42f, style = Stroke(strokeWidth))
                drawCircle(color = tint, center = Offset(w * 0.35f, h * 0.4f), radius = w * 0.06f)
                drawCircle(color = tint, center = Offset(w * 0.65f, h * 0.4f), radius = w * 0.06f)
                drawArc(tint, startAngle = 20f, sweepAngle = 140f, useCenter = false, topLeft = Offset(w * 0.28f, h * 0.42f), size = Size(w * 0.44f, h * 0.35f), style = Stroke(strokeWidth, cap = StrokeCap.Round))
            }
            // 26. Default: Coffee / Rest
            else -> {
                val cupPath = Path().apply {
                    moveTo(w * 0.2f, h * 0.35f)
                    lineTo(w * 0.8f, h * 0.35f)
                    lineTo(w * 0.75f, h * 0.7f)
                    cubicTo(w * 0.7f, h * 0.85f, w * 0.3f, h * 0.85f, w * 0.25f, h * 0.7f)
                    close()

                    moveTo(w * 0.78f, h * 0.42f)
                    cubicTo(w * 0.92f, h * 0.42f, w * 0.92f, h * 0.65f, w * 0.76f, h * 0.65f)
                }
                drawPath(cupPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawLine(tint, start = Offset(w * 0.15f, h * 0.9f), end = Offset(w * 0.85f, h * 0.9f), strokeWidth = strokeWidth)
            }
        }
    }
}

@Composable
fun LucideNavIcon(
    name: String,
    tint: Color,
    modifier: Modifier = Modifier.size(20.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()

        when (name.lowercase()) {
            "calendar" -> {
                drawRoundRect(color = tint, topLeft = Offset(w * 0.15f, h * 0.25f), size = Size(w * 0.7f, h * 0.65f), cornerRadius = CornerRadius(w * 0.1f), style = Stroke(width = strokeWidth))
                drawLine(tint, start = Offset(w * 0.32f, h * 0.12f), end = Offset(w * 0.32f, h * 0.28f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.68f, h * 0.12f), end = Offset(w * 0.68f, h * 0.28f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.15f, h * 0.45f), end = Offset(w * 0.85f, h * 0.45f), strokeWidth = strokeWidth)
            }
            "today" -> {
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.22f, style = Stroke(width = strokeWidth))
                drawLine(tint, start = Offset(w * 0.5f, h * 0.12f), end = Offset(w * 0.5f, h * 0.24f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.5f, h * 0.76f), end = Offset(w * 0.5f, h * 0.88f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.12f, h * 0.5f), end = Offset(w * 0.24f, h * 0.5f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.76f, h * 0.5f), end = Offset(w * 0.88f, h * 0.5f), strokeWidth = strokeWidth)
            }
            "history" -> {
                val p1 = Path().apply { moveTo(w * 0.2f, h * 0.8f); lineTo(w * 0.2f, h * 0.55f) }
                val p2 = Path().apply { moveTo(w * 0.5f, h * 0.8f); lineTo(w * 0.5f, h * 0.35f) }
                val p3 = Path().apply { moveTo(w * 0.8f, h * 0.8f); lineTo(w * 0.8f, h * 0.2f) }
                drawPath(p1, color = tint, style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round))
                drawPath(p2, color = tint, style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round))
                drawPath(p3, color = tint, style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round))
            }
            "settings" -> {
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.2f, style = Stroke(width = strokeWidth))
                for (angle in 0 until 360 step 60) {
                    val rad = Math.toRadians(angle.toDouble())
                    val x1 = (w * 0.5f + Math.cos(rad) * w * 0.26f).toFloat()
                    val y1 = (h * 0.5f + Math.sin(rad) * h * 0.26f).toFloat()
                    val x2 = (w * 0.5f + Math.cos(rad) * w * 0.42f).toFloat()
                    val y2 = (h * 0.5f + Math.sin(rad) * h * 0.42f).toFloat()
                    drawLine(tint, start = Offset(x1, y1), end = Offset(x2, y2), strokeWidth = strokeWidth)
                }
            }
            "edit" -> {
                val p = Path().apply {
                    moveTo(w * 0.75f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.25f)
                    lineTo(w * 0.35f, h * 0.75f)
                    lineTo(w * 0.2f, h * 0.8f)
                    lineTo(w * 0.25f, h * 0.65f)
                    close()
                }
                drawPath(p, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            "eye" -> {
                val eyePath = Path().apply {
                    moveTo(w * 0.1f, h * 0.5f)
                    cubicTo(w * 0.3f, h * 0.2f, w * 0.7f, h * 0.2f, w * 0.9f, h * 0.5f)
                    cubicTo(w * 0.7f, h * 0.8f, w * 0.3f, h * 0.8f, w * 0.1f, h * 0.5f)
                }
                drawPath(eyePath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.16f, style = Stroke(width = strokeWidth))
            }
            "target" -> { // Bugüne Git
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.38f, style = Stroke(width = strokeWidth))
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.18f, style = Stroke(width = strokeWidth))
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.05f)
            }
            "moon" -> {
                val moonPath = Path().apply {
                    moveTo(w * 0.75f, h * 0.2f)
                    cubicTo(w * 0.45f, h * 0.2f, w * 0.25f, h * 0.4f, w * 0.25f, h * 0.7f)
                    cubicTo(w * 0.25f, h * 0.85f, w * 0.32f, h * 0.95f, w * 0.45f, h * 0.98f)
                    cubicTo(w * 0.2f, h * 0.95f, w * 0.1f, h * 0.75f, w * 0.1f, h * 0.5f)
                    cubicTo(w * 0.1f, h * 0.25f, w * 0.35f, h * 0.05f, w * 0.65f, h * 0.05f)
                    cubicTo(w * 0.72f, h * 0.05f, w * 0.78f, h * 0.1f, w * 0.75f, h * 0.2f)
                }
                drawPath(moonPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }
            "sun" -> {
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.22f, style = Stroke(width = strokeWidth))
                drawLine(tint, start = Offset(w * 0.5f, h * 0.1f), end = Offset(w * 0.5f, h * 0.22f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.5f, h * 0.78f), end = Offset(w * 0.5f, h * 0.9f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.1f, h * 0.5f), end = Offset(w * 0.22f, h * 0.5f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.78f, h * 0.5f), end = Offset(w * 0.9f, h * 0.5f), strokeWidth = strokeWidth)
            }
            "bell" -> {
                val bellPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    cubicTo(w * 0.3f, h * 0.15f, w * 0.25f, h * 0.35f, w * 0.25f, h * 0.55f)
                    lineTo(w * 0.15f, h * 0.75f)
                    lineTo(w * 0.85f, h * 0.75f)
                    lineTo(w * 0.75f, h * 0.55f)
                    cubicTo(w * 0.75f, h * 0.35f, w * 0.7f, h * 0.15f, w * 0.5f, h * 0.15f)
                }
                drawPath(bellPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                drawArc(color = tint, startAngle = 0f, sweepAngle = 180f, useCenter = false,
                    topLeft = Offset(w * 0.4f, h * 0.75f), size = Size(w * 0.2f, h * 0.15f), style = Stroke(width = strokeWidth))
            }
            "template" -> {
                drawRoundRect(color = tint, topLeft = Offset(w * 0.2f, h * 0.2f), size = Size(w * 0.6f, h * 0.7f), cornerRadius = CornerRadius(w * 0.08f), style = Stroke(width = strokeWidth))
                drawLine(tint, start = Offset(w * 0.32f, h * 0.38f), end = Offset(w * 0.68f, h * 0.38f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.32f, h * 0.55f), end = Offset(w * 0.68f, h * 0.55f), strokeWidth = strokeWidth)
            }
            "reset" -> {
                drawArc(color = tint, startAngle = 45f, sweepAngle = 270f, useCenter = false,
                    topLeft = Offset(w * 0.15f, h * 0.15f), size = Size(w * 0.7f, h * 0.7f), style = Stroke(width = strokeWidth))
                val arrow = Path().apply {
                    moveTo(w * 0.65f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.25f)
                    lineTo(w * 0.65f, h * 0.38f)
                }
                drawPath(arrow, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            "flame" -> {
                val flamePath = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    cubicTo(w * 0.7f, h * 0.35f, w * 0.85f, h * 0.55f, w * 0.85f, h * 0.7f)
                    cubicTo(w * 0.85f, h * 0.88f, w * 0.7f, h * 0.95f, w * 0.5f, h * 0.95f)
                    cubicTo(w * 0.3f, h * 0.95f, w * 0.15f, h * 0.88f, w * 0.15f, h * 0.7f)
                    cubicTo(w * 0.15f, h * 0.55f, w * 0.3f, h * 0.35f, w * 0.5f, h * 0.15f)
                }
                drawPath(flamePath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            "award" -> {
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.4f), radius = w * 0.28f, style = Stroke(width = strokeWidth))
                val ribbon = Path().apply {
                    moveTo(w * 0.35f, h * 0.62f)
                    lineTo(w * 0.25f, h * 0.9f)
                    lineTo(w * 0.5f, h * 0.8f)
                    lineTo(w * 0.75f, h * 0.9f)
                    lineTo(w * 0.65f, h * 0.62f)
                }
                drawPath(ribbon, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            "check-circle" -> {
                drawCircle(color = tint, center = Offset(w * 0.5f, h * 0.5f), radius = w * 0.38f, style = Stroke(width = strokeWidth))
                val p = Path().apply {
                    moveTo(w * 0.32f, h * 0.5f)
                    lineTo(w * 0.46f, h * 0.64f)
                    lineTo(w * 0.68f, h * 0.38f)
                }
                drawPath(p, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            "trending-up" -> {
                val p = Path().apply {
                    moveTo(w * 0.15f, h * 0.75f)
                    lineTo(w * 0.4f, h * 0.5f)
                    lineTo(w * 0.6f, h * 0.65f)
                    lineTo(w * 0.85f, h * 0.3f)
                }
                drawPath(p, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                val arrow = Path().apply {
                    moveTo(w * 0.65f, h * 0.3f)
                    lineTo(w * 0.85f, h * 0.3f)
                    lineTo(w * 0.85f, h * 0.5f)
                }
                drawPath(arrow, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            "bar-chart" -> {
                drawLine(tint, start = Offset(w * 0.25f, h * 0.85f), end = Offset(w * 0.25f, h * 0.55f), strokeWidth = strokeWidth * 1.5f)
                drawLine(tint, start = Offset(w * 0.5f, h * 0.85f), end = Offset(w * 0.5f, h * 0.25f), strokeWidth = strokeWidth * 1.5f)
                drawLine(tint, start = Offset(w * 0.75f, h * 0.85f), end = Offset(w * 0.75f, h * 0.45f), strokeWidth = strokeWidth * 1.5f)
            }
        }
    }
}
@Composable
fun StatusBadgeIcon(state: SlotState, colors: ThemeColors, modifier: Modifier = Modifier.size(16.dp)) {
    val strokeColor = when (state) {
        SlotState.DONE   -> Color(0xFF16A34A)
        SlotState.MISSED -> Color(0xFFDC2626)
        SlotState.ACTIVE -> Color(0xFFCA8A04)
        else             -> colors.text3
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sw = 2.dp.toPx()

        when (state) {
            SlotState.DONE -> {
                val p = Path().apply {
                    moveTo(w * 0.2f, h * 0.5f)
                    lineTo(w * 0.45f, h * 0.75f)
                    lineTo(w * 0.85f, h * 0.25f)
                }
                drawPath(p, color = strokeColor, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            SlotState.MISSED -> {
                drawLine(strokeColor, start = Offset(w * 0.25f, h * 0.25f), end = Offset(w * 0.75f, h * 0.75f), strokeWidth = sw, cap = StrokeCap.Round)
                drawLine(strokeColor, start = Offset(w * 0.75f, h * 0.25f), end = Offset(w * 0.25f, h * 0.75f), strokeWidth = sw, cap = StrokeCap.Round)
            }
            SlotState.ACTIVE -> {
                val bolt = Path().apply {
                    moveTo(w * 0.55f, h * 0.1f)
                    lineTo(w * 0.2f, h * 0.55f)
                    lineTo(w * 0.5f, h * 0.55f)
                    lineTo(w * 0.45f, h * 0.9f)
                    lineTo(w * 0.8f, h * 0.45f)
                    lineTo(w * 0.5f, h * 0.45f)
                    close()
                }
                drawPath(bolt, color = strokeColor, style = Stroke(width = sw, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            else -> {
                drawCircle(color = strokeColor, radius = w * 0.35f, style = Stroke(width = sw))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  SlotCard (WITH SMART LUCIDE ICONS)
// ─────────────────────────────────────────────────────────────────

@Composable
fun SlotCard(
    item: SlotUiItem,
    isDark: Boolean = true,
    categories: List<CategoryEntity> = emptyList(),
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = if (isDark) DarkThemeColors else LightThemeColors
    val accent = parseColorHex(item.slot.colorHex, categoryAccent(item.slot.category, item.slot.colorHex))
    val bgTint = categoryBgTint(item.slot.category, item.slot.colorHex)

    val bgBitmap = remember(item.slot.bgStyle) {
        val s = item.slot.bgStyle
        if (s.isNotBlank() && s != "default" && !s.startsWith("gradient_") && s != "solid") {
            try {
                if (File(s).exists()) {
                    BitmapFactory.decodeFile(s)?.asImageBitmap()
                } else null
            } catch (_: Exception) { null }
        } else null
    }

    val coverBrush = when (item.slot.bgStyle) {
        "gradient_sunset"  -> Brush.horizontalGradient(listOf(Color(0xFFFF512F).copy(alpha = if (isDark) 0.35f else 0.22f), Color(0xFFDD2476).copy(alpha = if (isDark) 0.28f else 0.16f)))
        "gradient_ocean"   -> Brush.horizontalGradient(listOf(Color(0xFF2193b0).copy(alpha = if (isDark) 0.35f else 0.22f), Color(0xFF6dd5ed).copy(alpha = if (isDark) 0.28f else 0.16f)))
        "gradient_emerald" -> Brush.horizontalGradient(listOf(Color(0xFF11998e).copy(alpha = if (isDark) 0.35f else 0.22f), Color(0xFF38ef7d).copy(alpha = if (isDark) 0.28f else 0.16f)))
        "gradient_purple"  -> Brush.horizontalGradient(listOf(Color(0xFF8A2387).copy(alpha = if (isDark) 0.35f else 0.22f), Color(0xFFE94057).copy(alpha = if (isDark) 0.28f else 0.16f)))
        "solid"            -> Brush.horizontalGradient(listOf(accent.copy(alpha = if (isDark) 0.25f else 0.15f), accent.copy(alpha = 0.08f)))
        else               -> null
    }

    val hasCustomBg = bgBitmap != null || coverBrush != null

    val borderColor = when (item.state) {
        SlotState.DONE         -> if (isDark) Color(0xFF16A34A).copy(alpha = 0.6f) else Color(0xFF86EFAC)
        SlotState.MISSED       -> if (isDark) Color(0xFFDC2626).copy(alpha = 0.6f) else Color(0xFFFCA5A5)
        SlotState.ACTIVE       -> if (isDark) Color(0xFFCA8A04).copy(alpha = 0.8f) else Color(0xFFFDE047)
        SlotState.PAST_PENDING -> Color(0xFFDC2626).copy(alpha = 0.3f)
        SlotState.FUTURE       -> colors.border
    }
    val cardBg = when (item.state) {
        SlotState.DONE   -> if (isDark) Color(0xFF16A34A).copy(alpha = 0.12f) else Color(0xFFF0FDF4)
        SlotState.MISSED -> if (isDark) Color(0xFFDC2626).copy(alpha = 0.12f) else Color(0xFFFEF2F2)
        SlotState.ACTIVE -> if (isDark) Color(0xFFCA8A04).copy(alpha = 0.12f) else Color(0xFFFEFCE8)
        else             -> colors.bgCard
    }

    val infiniteTransition = rememberInfiniteTransition(label = "active_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "pulse"
    )
    val activeBorder = if (item.state == SlotState.ACTIVE)
        borderColor.copy(alpha = pulseAlpha) else borderColor

    val cardAlpha = if (item.state == SlotState.FUTURE) 0.65f else 1f

    val textPrimary = if (bgBitmap != null) (if (isDark) Color.White else colors.text1) else colors.text1
    val textSecondary = if (bgBitmap != null) (if (isDark) Color.White.copy(alpha = 0.88f) else colors.text2) else colors.text2
    val textTertiary = if (bgBitmap != null) (if (isDark) Color.White.copy(alpha = 0.72f) else colors.text3) else colors.text3

    val slotIconName = item.slot.emoji.ifBlank {
        categories.firstOrNull { it.id == item.slot.category }?.iconName ?: item.slot.category
    }

    Box(modifier = modifier.alpha(cardAlpha)) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.5.dp, activeBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background Photo or Preset Brush
                if (bgBitmap != null) {
                    Image(
                        bitmap = bgBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    // Theme-Adaptive Readability Scrim Overlay
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
                } else if (coverBrush != null) {
                    Box(modifier = Modifier.matchParentSize().background(coverBrush))
                }

                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .width(4.dp)
                            .height(44.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accent)
                    )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (bgBitmap != null) {
                                    if (isDark) Color.Black.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.85f)
                                }
                                else if (isDark) bgTint
                                else accent.copy(alpha = 0.12f)
                            )
                            .then(if (bgBitmap != null) Modifier.border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp)) else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        LucideIcon(
                            title = item.slot.title,
                            category = item.slot.category,
                            iconName = slotIconName,
                            tint = accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.slot.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.slot.startTime} – ${item.slot.endTime}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textTertiary
                        )
                        val noteText = item.completion?.note?.takeIf { it.isNotBlank() }
                            ?: item.slot.subtitle.takeIf { it.isNotBlank() }
                        if (noteText != null) {
                            Text(
                                text = if (item.completion?.note?.isNotBlank() == true) "💬 $noteText" else noteText,
                                fontSize = 11.sp,
                                color = textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                when (item.state) {
                                    SlotState.DONE   -> Color(0xFF16A34A).copy(alpha = 0.25f)
                                    SlotState.MISSED -> Color(0xFFDC2626).copy(alpha = 0.25f)
                                    SlotState.ACTIVE -> Color(0xFFCA8A04).copy(alpha = 0.25f)
                                    else             -> if (bgBitmap != null) {
                                        if (isDark) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.85f)
                                    } else colors.bgCard2
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        StatusBadgeIcon(state = item.state, colors = colors)
                    }
                }
            }
        }

        if (item.state == SlotState.ACTIVE) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFCA8A04).copy(alpha = 0.25f))
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    "AKTİF", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFCA8A04), letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────
//  Day Strip (Water / Liquid Progress Fill Animated)
// ─────────────────────────────────────────────────────────────────

@Composable
fun DayStrip(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    dayProgress: Map<LocalDate, Float> = emptyMap(),
    isDark: Boolean = true,
    onDateSelected: (LocalDate) -> Unit
) {
    val colors = if (isDark) DarkThemeColors else LightThemeColors
    val today  = LocalDate.now()

    val infiniteTransition = rememberInfiniteTransition(label = "water_wave_infinite")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0..6) {
            val date = weekStart.plusDays(i.toLong())
            val isSelected = date == selectedDate
            val isToday    = date == today
            val isPast     = date < today
            val progress   = (dayProgress[date] ?: 0f).coerceIn(0f, 1f)

            val animatedFill by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(700, easing = FastOutSlowInEasing),
                label = "day_water_fill"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(84.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.bgCard)
                    .border(
                        1.5.dp,
                        if (isSelected) Color(0xFF4F8EF7) else colors.border,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onDateSelected(date) },
                contentAlignment = Alignment.Center
            ) {
                // ── Ultra-Realistic Dual-Wave Animated Water / Liquid Fill ──
                if (animatedFill > 0.01f) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val w = size.width
                        val h = size.height
                        val fillRatio = animatedFill.coerceIn(0f, 1f)
                        // At 100% full, keep surface wave dancing right at the top rim (93% height)
                        val targetWaterH = if (fillRatio >= 0.99f) h * 0.93f else h * fillRatio
                        val baseWaterY = h - targetWaterH
                        val waveAmp = if (fillRatio >= 0.99f) 1.5.dp.toPx() else 2.5.dp.toPx()

                        // 1. Back wave (creates optical 3D depth and parallax)
                        val backWavePhase = wavePhase * 0.85f + 1.8f
                        val backWavePath = Path().apply {
                            moveTo(0f, h)
                            lineTo(0f, baseWaterY)
                            val step = w / 16f
                            for (stepIndex in 0..16) {
                                val x = stepIndex * step
                                val waveY = baseWaterY + kotlin.math.sin(backWavePhase + (x / w) * 2 * Math.PI.toFloat()) * (waveAmp * 0.75f)
                                lineTo(x, waveY.toFloat())
                            }
                            lineTo(w, h)
                            close()
                        }
                        val backWaterBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0284C7).copy(alpha = 0.45f),
                                Color(0xFF1D4ED8).copy(alpha = 0.65f)
                            ),
                            startY = baseWaterY,
                            endY = h
                        )
                        drawPath(backWavePath, brush = backWaterBrush)

                        // 2. Front wave (crisp surface liquid)
                        val frontWavePath = Path().apply {
                            moveTo(0f, h)
                            lineTo(0f, baseWaterY)
                            val step = w / 16f
                            for (stepIndex in 0..16) {
                                val x = stepIndex * step
                                val waveY = baseWaterY + kotlin.math.sin(wavePhase + (x / w) * 2 * Math.PI.toFloat()) * waveAmp
                                lineTo(x, waveY.toFloat())
                            }
                            lineTo(w, h)
                            close()
                        }
                        val frontWaterBrush = Brush.verticalGradient(
                            colors = if (isSelected) {
                                listOf(
                                    Color(0xFF38BDF8).copy(alpha = 0.75f),
                                    Color(0xFF6366F1).copy(alpha = 0.85f)
                                )
                            } else {
                                listOf(
                                    Color(0xFF38BDF8).copy(alpha = 0.65f),
                                    Color(0xFF3B82F6).copy(alpha = 0.78f)
                                )
                            },
                            startY = baseWaterY,
                            endY = h
                        )
                        drawPath(frontWavePath, brush = frontWaterBrush)

                        // 3. Water surface crest & foam shimmer line (always clearly visible, even at 100%)
                        val crestPath = Path().apply {
                            val step = w / 16f
                            val startY = baseWaterY + kotlin.math.sin(wavePhase) * waveAmp
                            moveTo(0f, startY.toFloat())
                            for (stepIndex in 1..16) {
                                val x = stepIndex * step
                                val waveY = baseWaterY + kotlin.math.sin(wavePhase + (x / w) * 2 * Math.PI.toFloat()) * waveAmp
                                lineTo(x, waveY.toFloat())
                            }
                        }
                        drawPath(
                            crestPath,
                            color = Color.White.copy(alpha = 0.80f),
                            style = Stroke(width = 1.5.dp.toPx())
                        )

                        // 4. Subtle glass reflection highlight
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                                startX = 0f,
                                endX = w * 0.4f
                            ),
                            topLeft = Offset(0f, 0f),
                            size = Size(w * 0.4f, h)
                        )
                    }
                }

                // Fixed internal layout to keep every day card identical in size with clear percentage visibility
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 6.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = date.dayShortTR(),
                        fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                        color = if (animatedFill > 0.65f) Color.White.copy(alpha = 0.9f) else if (isPast) colors.text3 else colors.text2,
                        letterSpacing = 0.3.sp
                    )
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 15.sp, fontWeight = FontWeight.ExtraBold,
                        color = when {
                            animatedFill > 0.45f -> Color.White
                            isSelected -> Color(0xFF4F8EF7)
                            isToday    -> colors.text1
                            isPast     -> colors.text3
                            else       -> colors.text2
                        }
                    )

                    // Bottom indicator lifted cleanly above the bottom curve with zero clipping
                    Box(
                        modifier = Modifier.height(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (progress > 0f) {
                            Text(
                                text = "%${(progress * 100).toInt()}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (animatedFill > 0.3f) Color.White else Color(0xFF38BDF8),
                                style = TextStyle(
                                    lineHeight = 10.sp,
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        } else if (isToday) {
                            Box(Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFF97316)))
                        } else if (isSelected) {
                            Box(Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF4F8EF7)))
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Mark Slot Bottom Sheet
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkSlotSheet(
    item: SlotUiItem,
    isDark: Boolean = true,
    onDismiss: () -> Unit,
    onMark: (status: String, note: String) -> Unit
) {
    val colors = if (isDark) DarkThemeColors else LightThemeColors
    var selectedStatus by remember(item) { mutableStateOf(item.completion?.status) }
    var note by remember(item) { mutableStateOf(item.completion?.note ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.bgCard,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp).padding(bottom = 32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LucideIcon(title = item.slot.title, category = item.slot.category, tint = categoryAccent(item.slot.category), modifier = Modifier.size(24.dp))
                Text(
                    item.slot.title,
                    fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${item.slot.startTime} – ${item.slot.endTime}",
                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.text3
            )
            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MarkButton(
                    status = "done", label = "Tamamlandı",
                    selected = selectedStatus == "done",
                    selectedColor = Color(0xFF16A34A),
                    colors = colors,
                    modifier = Modifier.weight(1f)
                ) { selectedStatus = if (selectedStatus == "done") null else "done" }

                MarkButton(
                    status = "missed", label = "Yapılmadı",
                    selected = selectedStatus == "missed",
                    selectedColor = Color(0xFFDC2626),
                    colors = colors,
                    modifier = Modifier.weight(1f)
                ) { selectedStatus = if (selectedStatus == "missed") null else "missed" }
            }

            Spacer(Modifier.height(16.dp))
            Text("Not (isteğe bağlı)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.text3)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = note, onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Kısa not...", fontSize = 13.sp, color = colors.text3) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.bgCard2,
                    unfocusedContainerColor = colors.bgCard2,
                    focusedBorderColor = Color(0xFF4F8EF7),
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.text1,
                    unfocusedTextColor = colors.text1
                ),
                shape = RoundedCornerShape(12.dp), maxLines = 3
            )
            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.text2),
                    border = BorderStroke(1.dp, colors.border), shape = RoundedCornerShape(14.dp)
                ) { Text("İptal", fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { selectedStatus?.let { onMark(it, note.trim()) }; onDismiss() },
                    enabled = selectedStatus != null, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F8EF7),
                        disabledContainerColor = colors.bgCard2
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Kaydet", fontWeight = FontWeight.ExtraBold) }
            }
        }
    }
}

@Composable
private fun MarkButton(
    status: String, label: String,
    selected: Boolean, selectedColor: Color,
    colors: ThemeColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) selectedColor.copy(alpha = 0.15f) else colors.bgCard2)
            .border(2.dp, if (selected) selectedColor else colors.border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadgeIcon(
                state = if (status == "done") SlotState.DONE else SlotState.MISSED,
                colors = colors,
                modifier = Modifier.size(24.dp)
            )
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                color = if (selected) selectedColor else colors.text2)
        }
    }
}

// ─────────────────────────────────────────────────────────────────
//  Edit/Add Slot Bottom Sheet
// ─────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────
//  SLEEK MINIMALIST SWITCH
// ─────────────────────────────────────────────────────────────────

@Composable
fun SleekSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFFA855F7),
    inactiveColor: Color = Color(0xFF374151)
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 500f),
        label = "sleek_switch_thumb"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) activeColor else inactiveColor,
        animationSpec = tween(200),
        label = "sleek_switch_track"
    )

    Box(
        modifier = modifier
            .width(44.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(20.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// ─────────────────────────────────────────────────────────────────
// ─────────────────────────────────────────────────────────────────
//  CREATE CATEGORY DIALOG (Matching user's screenshots)
// ─────────────────────────────────────────────────────────────────

@Composable
fun CreateCategoryDialog(
    isDark: Boolean = true,
    onDismiss: () -> Unit,
    onCreated: (id: String, name: String, icon: String, colorHex: String) -> Unit
) {
    val colors = if (isDark) DarkThemeColors else LightThemeColors
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("target") }
    var selectedColor by remember { mutableStateOf(Color(0xFF8B5CF6)) } // default purple
    var showError by remember { mutableStateOf(false) }

    fun colorToHex(color: Color): String {
        val argb = (color.alpha * 255).toInt().shl(24) or
                   (color.red * 255).toInt().shl(16) or
                   (color.green * 255).toInt().shl(8) or
                   (color.blue * 255).toInt()
        return String.format("#%06X", 0xFFFFFF and argb)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = colors.bgCard,
        title = {
            Text("Yeni Kategori Oluştur", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Name Field
                EditField(value = name, label = "Kategori Adı *", colors = colors) {
                    name = it
                    if (it.isNotBlank()) showError = false
                }

                // 26 Icon Selection with Labels
                Text("İkon Seçimi (${ALL_CATEGORY_ICONS.size} İkon)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.text3)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ALL_CATEGORY_ICONS) { item ->
                        val isSel = selectedIcon == item.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) selectedColor.copy(alpha = 0.25f) else colors.bgCard2)
                                    .border(1.5.dp, if (isSel) selectedColor else Color.Transparent, RoundedCornerShape(10.dp))
                                    .clickable { selectedIcon = item.id },
                                contentAlignment = Alignment.Center
                            ) {
                                LucideIcon(iconName = item.id, tint = if (isSel) selectedColor else colors.text2, modifier = Modifier.size(20.dp))
                            }
                            Text(
                                item.label,
                                fontSize = 9.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) selectedColor else colors.text3
                            )
                        }
                    }
                }

                // COLOR Section (40 Colors Palette matching user's request)
                Text("COLOR (RENK)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.text3)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    val rows = PRESET_PALETTE_COLORS.chunked(8)
                    rows.forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowColors.forEach { c ->
                                val isSel = selectedColor == c
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(c)
                                        .border(
                                            if (isSel) 2.5.dp else 0.5.dp,
                                            if (isSel) Color.White else Color.Black.copy(alpha = 0.15f),
                                            RoundedCornerShape(7.dp)
                                        )
                                        .clickable { selectedColor = c },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSel) {
                                        Text(
                                            "✓",
                                            color = if (c.luminance() > 0.6f) Color.Black else Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (showError) {
                    Text("⚠️ Lütfen kategori adını yazınız.", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.text2),
                        border = BorderStroke(1.dp, colors.border),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Vazgeç", fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                showError = true
                            } else {
                                val catId = "cat_" + System.currentTimeMillis()
                                onCreated(catId, name.trim(), selectedIcon, colorToHex(selectedColor))
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Oluştur", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold) }
                }
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────
//  Edit/Add Slot Bottom Sheet
// ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSlotSheet(
    existingSlot: SlotUiItem? = null,
    defaultDate: LocalDate,
    categories: List<CategoryEntity> = emptyList(),
    isDark: Boolean = true,
    onDismiss: () -> Unit,
    onAddCategory: ((id: String, name: String, icon: String, colorHex: String, bgStyle: String) -> Unit)? = null,
    onSave: (title: String, emoji: String, subtitle: String, category: String,
             startTime: String, endTime: String, isRecurring: Boolean,
             colorHex: String, bgStyle: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val colors  = if (isDark) DarkThemeColors else LightThemeColors

    val now = remember { LocalTime.now() }
    val initialStart = remember(existingSlot) {
        existingSlot?.slot?.startTime?.takeIf { it.isNotBlank() }
            ?: String.format("%02d:00", now.hour)
    }
    val initialEnd = remember(existingSlot) {
        existingSlot?.slot?.endTime?.takeIf { it.isNotBlank() }
            ?: String.format("%02d:00", (now.hour + 1) % 24)
    }

    val defaultCatId = remember(categories, existingSlot) {
        existingSlot?.slot?.category
            ?: categories.firstOrNull()?.id
            ?: "study"
    }
    val defaultIcon = remember(categories, existingSlot) {
        existingSlot?.slot?.emoji?.takeIf { it.isNotBlank() }
            ?: categories.firstOrNull { it.id == defaultCatId }?.iconName
            ?: "star"
    }
    val defaultColor = remember(categories, existingSlot) {
        existingSlot?.slot?.colorHex?.takeIf { it.isNotBlank() }
            ?: categories.firstOrNull { it.id == defaultCatId }?.colorHex
            ?: ""
    }
    var title        by remember(existingSlot) { mutableStateOf(existingSlot?.slot?.title ?: "") }
    var subtitle     by remember(existingSlot) { mutableStateOf(existingSlot?.slot?.subtitle ?: "") }
    var category     by remember(existingSlot) { mutableStateOf(defaultCatId) }
    var selectedIcon by remember(existingSlot) { mutableStateOf(defaultIcon) }
    var colorHex     by remember(existingSlot) { mutableStateOf(defaultColor) }
    var bgStyle      by remember(existingSlot) { mutableStateOf(existingSlot?.slot?.bgStyle ?: "default") }
    var startTime    by remember(existingSlot) { mutableStateOf(initialStart) }
    var endTime      by remember(existingSlot) { mutableStateOf(initialEnd) }
    var recurring    by remember(existingSlot) { mutableStateOf(existingSlot?.slot?.isRecurring ?: true) }
    var showError    by remember { mutableStateOf(false) }
    var showCreateCatDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = saveImageLocally(context, uri)
            bgStyle = localPath
        }
    }

    fun showTimePicker(currentVal: String, onSelected: (String) -> Unit) {
        val parsed = try {
            LocalTime.parse(currentVal)
        } catch (_: Exception) {
            LocalTime.now()
        }
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onSelected(String.format("%02d:%02d", hourOfDay, minute))
            },
            parsed.hour,
            parsed.minute,
            true
        ).show()
    }

    fun addMinutesToEnd(minutes: Long) {
        val start = try {
            LocalTime.parse(startTime)
        } catch (_: Exception) {
            LocalTime.now()
        }
        val newEnd = start.plusMinutes(minutes)
        endTime = String.format("%02d:%02d", newEnd.hour, newEnd.minute)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.bgCard,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                if (existingSlot == null) "Etkinlik Ekle" else "Etkinlik Düzenle",
                fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = colors.text1
            )

            EditField(value = title, label = "Başlık *", colors = colors) {
                title = it
                if (it.isNotBlank()) showError = false
            }
            EditField(value = subtitle, label = "Alt başlık (isteğe bağlı)", colors = colors) { subtitle = it }

            // Time Selector Cards (Touch to open native TimePickerDialog)
            Text("Saat Seçimi (Dokunarak Seç)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.text3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Start Time Card
                Card(
                    onClick = { showTimePicker(startTime) { startTime = it } },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.bgCard2),
                    border = BorderStroke(1.dp, Color(0xFF4F8EF7).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⏰", fontSize = 12.sp)
                            Text("Başlangıç", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.text3)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = startTime.ifBlank { "--:--" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.text1
                        )
                    }
                }

                // End Time Card
                Card(
                    onClick = { showTimePicker(endTime) { endTime = it } },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.bgCard2),
                    border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🏁", fontSize = 12.sp)
                            Text("Bitiş", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.text3)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = endTime.ifBlank { "--:--" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.text1
                        )
                    }
                }
            }

            // ── CATEGORY SECTION ──
            Text("KATEGORİ", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = colors.text3, letterSpacing = 0.8.sp)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. "+ Yeni" Button
                item {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFA855F7).copy(alpha = 0.12f))
                            .border(1.5.dp, Color(0xFFA855F7), RoundedCornerShape(12.dp))
                            .clickable { showCreateCatDialog = true }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("+", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFA855F7))
                            Text("Yeni", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFA855F7))
                        }
                    }
                }

                // 2. Categories List (User-created categories)
                items(categories) { cat ->
                    val isSelected = category == cat.id
                    val accent = parseColorHex(cat.colorHex, categoryAccent(cat.id))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) accent.copy(alpha = 0.22f) else colors.bgCard2)
                            .border(1.5.dp, if (isSelected) accent else Color.Transparent, RoundedCornerShape(12.dp))
                            .clickable {
                                category = cat.id
                                selectedIcon = cat.iconName
                                colorHex = cat.colorHex
                            }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LucideIcon(iconName = cat.iconName, category = cat.id, tint = if (isSelected) accent else colors.text3, modifier = Modifier.size(16.dp))
                            Text(
                                cat.name,
                                fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = if (isSelected) accent else colors.text2
                            )
                        }
                    }
                }
            }

            // ── KART ARKA PLAN FOTOĞRAFI ──
            Text("KART ARKA PLANI (FOTOĞRAF)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = colors.text3, letterSpacing = 0.8.sp)

            val isCustomPhoto = bgStyle.isNotBlank() && bgStyle != "default" && !bgStyle.startsWith("gradient_")

            if (isCustomPhoto) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val bmp = remember(bgStyle) {
                            try {
                                if (File(bgStyle).exists()) BitmapFactory.decodeFile(bgStyle)?.asImageBitmap() else null
                            } catch (_: Exception) { null }
                        }
                        if (bmp != null) {
                            Image(bitmap = bmp, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LucideIcon(iconName = "camera", tint = Color.White, modifier = Modifier.size(18.dp))
                                Text("Fotoğraf Eklendi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            FilledTonalButton(
                                onClick = { bgStyle = "default" },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFDC2626).copy(alpha = 0.85f), contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("✕ Kaldır", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Surface(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = colors.bgCard2,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        LucideIcon(iconName = "camera", tint = colors.text1, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Fotoğraf Ekle (Galeri)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.text1)
                    }
                }
            }

            if (showError) {
                Text(
                    "⚠️ Lütfen bir etkinlik başlığı yazınız.",
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.text2),
                    border = BorderStroke(1.dp, colors.border), shape = RoundedCornerShape(14.dp)
                ) { Text("İptal", fontWeight = FontWeight.Bold) }

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            showError = true
                        } else {
                            showError = false
                            val normStart = try {
                                val p = LocalTime.parse(startTime)
                                String.format("%02d:%02d", p.hour, p.minute)
                            } catch (_: Exception) { "09:00" }

                            val normEnd = try {
                                val p = LocalTime.parse(endTime)
                                String.format("%02d:%02d", p.hour, p.minute)
                            } catch (_: Exception) { "10:00" }

                            val finalColor = colorHex.ifBlank {
                                categories.firstOrNull { it.id == category }?.colorHex ?: "#4F8EF7"
                            }

                            onSave(title.trim(), selectedIcon, subtitle.trim(),
                                category, normStart, normEnd, recurring, finalColor, if (bgStyle == "default") "" else bgStyle)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F8EF7)),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Kaydet", fontWeight = FontWeight.ExtraBold) }
            }

            if (onDelete != null) {
                Button(
                    onClick = { onDelete(); onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626).copy(alpha = 0.15f),
                        contentColor = Color(0xFFF87171)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.3f))
                ) { Text("Etkinliği Sil", fontWeight = FontWeight.ExtraBold) }
            }
        }
    }

    if (showCreateCatDialog) {
        CreateCategoryDialog(
            isDark = isDark,
            onDismiss = { showCreateCatDialog = false },
            onCreated = { newId, newName, newIcon, newColor ->
                onAddCategory?.invoke(newId, newName, newIcon, newColor, "default")
                category = newId
                selectedIcon = newIcon
                colorHex = newColor
            }
        )
    }
}

@Composable
private fun EditField(
    value: String, label: String,
    colors: ThemeColors,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (String) -> Unit
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.text3)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.bgCard2, unfocusedContainerColor = colors.bgCard2,
                focusedBorderColor = Color(0xFF4F8EF7), unfocusedBorderColor = colors.border,
                focusedTextColor = colors.text1, unfocusedTextColor = colors.text1
            ),
            shape = RoundedCornerShape(10.dp), singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        )
    }
}

// ─────────────────────────────────────────────────────────────────
//  Progress Bar Card
// ─────────────────────────────────────────────────────────────────

@Composable
fun ProgressCard(items: List<SlotUiItem>, isDark: Boolean = true) {
    val colors  = if (isDark) DarkThemeColors else LightThemeColors
    val done    = items.count { it.state == SlotState.DONE }
    val missed  = items.count { it.state == SlotState.MISSED }
    val pending = items.count { it.state != SlotState.DONE && it.state != SlotState.MISSED }
    val total   = items.size
    val pct     = if (total > 0) (done * 100f / total) else 0f
    val animPct by animateFloatAsState(pct / 100f, tween(600), label = "progress")

    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.bgCard),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Günlük İlerleme", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.text2)
                Text("${pct.toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4F8EF7))
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { animPct },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(8.dp)),
                color = Color(0xFF4F8EF7), trackColor = colors.bgCard2,
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StatChip("✓ $done Yapıldı", Color(0xFF16A34A), colors)
                StatChip("✕ $missed Yapılmadı", Color(0xFFDC2626), colors)
                StatChip("○ $pending Bekliyor", colors.text3, colors)
            }
        }
    }
}

@Composable
private fun StatChip(label: String, color: Color, colors: ThemeColors) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = colors.text2)
    }
}
