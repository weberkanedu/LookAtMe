package com.example.lookatme.ui

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
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import java.time.LocalDate

// ─────────────────────────────────────────────────────────────────
//  LUCIDE VECTOR ICON DRAWING HELPERS (SMART ACTIVITY MATCHING)
// ─────────────────────────────────────────────────────────────────

@Composable
fun LucideIcon(
    title: String = "",
    category: String = "",
    tint: Color,
    modifier: Modifier = Modifier.size(20.dp)
) {
    val t = title.lowercase()
    val c = category.lowercase()

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()

        when {
            // 1. Basketball (Basketbol / Antrenman)
            t.contains("basket") || t.contains("antrenman") -> {
                drawCircle(color = tint, radius = w * 0.42f, style = Stroke(width = strokeWidth))
                drawLine(tint, start = Offset(w * 0.08f, h * 0.5f), end = Offset(w * 0.92f, h * 0.5f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.5f, h * 0.08f), end = Offset(w * 0.5f, h * 0.92f), strokeWidth = strokeWidth)
                drawArc(
                    color = tint, startAngle = -45f, sweepAngle = 90f, useCenter = false,
                    topLeft = Offset(w * 0.2f, h * 0.1f), size = Size(w * 0.6f, h * 0.8f),
                    style = Stroke(width = strokeWidth)
                )
            }
            // 2. Shower / Clean (Duş / Banyo)
            t.contains("duş") || t.contains("banyo") -> {
                val showerPath = Path().apply {
                    moveTo(w * 0.2f, h * 0.85f)
                    lineTo(w * 0.2f, h * 0.3f)
                    cubicTo(w * 0.2f, h * 0.15f, w * 0.6f, h * 0.15f, w * 0.6f, h * 0.3f)
                    lineTo(w * 0.6f, h * 0.4f)
                }
                drawPath(showerPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                drawLine(tint, start = Offset(w * 0.5f, h * 0.4f), end = Offset(w * 0.7f, h * 0.4f), strokeWidth = strokeWidth + 1f)
                drawCircle(color = tint, center = Offset(w * 0.55f, h * 0.6f), radius = w * 0.04f)
                drawCircle(color = tint, center = Offset(w * 0.65f, h * 0.65f), radius = w * 0.04f)
                drawCircle(color = tint, center = Offset(w * 0.58f, h * 0.78f), radius = w * 0.04f)
            }
            // 3. Transport / Bus / Navigation (Ulaşım / Çıkış / Dönüş / Eve / Otobüs / Varış)
            t.contains("ulaşım") || t.contains("çıkış") || t.contains("otobüs") || t.contains("varış") || t.contains("eve") || t.contains("dönüş") -> {
                val busPath = Path().apply {
                    addRoundRect(androidx.compose.ui.geometry.RoundRect(w * 0.2f, h * 0.2f, w * 0.8f, h * 0.75f, CornerRadius(w * 0.1f)))
                }
                drawPath(busPath, color = tint, style = Stroke(width = strokeWidth))
                drawLine(tint, start = Offset(w * 0.2f, h * 0.45f), end = Offset(w * 0.8f, h * 0.45f), strokeWidth = strokeWidth)
                drawCircle(color = tint, center = Offset(w * 0.35f, h * 0.62f), radius = w * 0.06f)
                drawCircle(color = tint, center = Offset(w * 0.65f, h * 0.62f), radius = w * 0.06f)
                drawLine(tint, start = Offset(w * 0.3f, h * 0.75f), end = Offset(w * 0.3f, h * 0.88f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.7f, h * 0.75f), end = Offset(w * 0.7f, h * 0.88f), strokeWidth = strokeWidth)
            }
            // 4. Food / Meal (Yemek / Öğün / Öğle / Akşam Yemeği)
            t.contains("yemek") || t.contains("öğün") || t.contains("makarna") || t.contains("kahvaltı") -> {
                drawLine(tint, start = Offset(w * 0.35f, h * 0.15f), end = Offset(w * 0.35f, h * 0.85f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.25f, h * 0.15f), end = Offset(w * 0.25f, h * 0.45f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.45f, h * 0.15f), end = Offset(w * 0.45f, h * 0.45f), strokeWidth = strokeWidth)
                drawLine(tint, start = Offset(w * 0.25f, h * 0.45f), end = Offset(w * 0.45f, h * 0.45f), strokeWidth = strokeWidth)

                drawLine(tint, start = Offset(w * 0.7f, h * 0.15f), end = Offset(w * 0.7f, h * 0.85f), strokeWidth = strokeWidth)
                val knifeArc = Path().apply {
                    moveTo(w * 0.7f, h * 0.15f)
                    cubicTo(w * 0.88f, h * 0.25f, w * 0.88f, h * 0.45f, w * 0.7f, h * 0.5f)
                }
                drawPath(knifeArc, color = tint, style = Stroke(width = strokeWidth))
            }
            // 5. Bilsem / School (BİLSEM / Okul)
            c == "bilsem" || t.contains("bilsem") || t.contains("okul") -> {
                val capPath = Path().apply {
                    moveTo(w * 0.5f, h * 0.2f)
                    lineTo(w * 0.9f, h * 0.4f)
                    lineTo(w * 0.5f, h * 0.6f)
                    lineTo(w * 0.1f, h * 0.4f)
                    close()

                    moveTo(w * 0.25f, h * 0.48f)
                    lineTo(w * 0.25f, h * 0.72f)
                    cubicTo(w * 0.35f, h * 0.82f, w * 0.65f, h * 0.82f, w * 0.75f, h * 0.72f)
                    lineTo(w * 0.75f, h * 0.48f)
                }
                drawPath(capPath, color = tint, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            // 6. Language / English (Dil / İngilizce / Kurs)
            c == "lang" || t.contains("ingilizce") || t.contains("dil") || t.contains("kelime") -> {
                drawCircle(color = tint, radius = w * 0.4f, style = Stroke(width = strokeWidth))
                drawLine(tint, start = Offset(w * 0.1f, h * 0.5f), end = Offset(w * 0.9f, h * 0.5f), strokeWidth = strokeWidth)
                drawOval(
                    color = tint,
                    topLeft = Offset(w * 0.28f, h * 0.1f),
                    size = Size(w * 0.44f, h * 0.8f),
                    style = Stroke(width = strokeWidth)
                )
            }
            // 7. Study / Reading / Math / Exam (Çalışma / Blok / Paragraf / Soru / Tekrar / Deneme)
            c == "study" || t.contains("çalışma") || t.contains("okuma") || t.contains("paragraf") || t.contains("soru") || t.contains("tekrar") || t.contains("deneme") || t.contains("ödev") -> {
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
            // 8. Default: Coffee / Rest
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = if (isDark) DarkThemeColors else LightThemeColors
    val accent = categoryAccent(item.slot.category)
    val bgTint = categoryBgTint(item.slot.category)

    val borderColor = when (item.state) {
        SlotState.DONE         -> if (isDark) Color(0xFF16A34A).copy(alpha = 0.5f) else Color(0xFF86EFAC)
        SlotState.MISSED       -> if (isDark) Color(0xFFDC2626).copy(alpha = 0.5f) else Color(0xFFFCA5A5)
        SlotState.ACTIVE       -> if (isDark) Color(0xFFCA8A04).copy(alpha = 0.7f) else Color(0xFFFDE047)
        SlotState.PAST_PENDING -> Color(0xFFDC2626).copy(alpha = 0.25f)
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

    Box(modifier = modifier.alpha(cardAlpha)) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.5.dp, activeBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
        ) {
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
                        .background(if (isDark) bgTint else accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    LucideIcon(title = item.slot.title, category = item.slot.category, tint = accent, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.slot.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.slot.startTime} – ${item.slot.endTime}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text3
                    )
                    val noteText = item.completion?.note?.takeIf { it.isNotBlank() }
                        ?: item.slot.subtitle.takeIf { it.isNotBlank() }
                    if (noteText != null) {
                        Text(
                            text = if (item.completion?.note?.isNotBlank() == true) "💬 $noteText" else noteText,
                            fontSize = 11.sp,
                            color = colors.text2,
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
                                SlotState.DONE   -> Color(0xFF16A34A).copy(alpha = 0.2f)
                                SlotState.MISSED -> Color(0xFFDC2626).copy(alpha = 0.2f)
                                SlotState.ACTIVE -> Color(0xFFCA8A04).copy(alpha = 0.2f)
                                else             -> colors.bgCard2
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    StatusBadgeIcon(state = item.state, colors = colors)
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
//  Day Strip
// ─────────────────────────────────────────────────────────────────

@Composable
fun DayStrip(
    weekStart: LocalDate,
    selectedDate: LocalDate,
    isDark: Boolean = true,
    onDateSelected: (LocalDate) -> Unit
) {
    val colors = if (isDark) DarkThemeColors else LightThemeColors
    val today  = LocalDate.now()

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(7) { i ->
            val date = weekStart.plusDays(i.toLong())
            val isSelected = date == selectedDate
            val isToday    = date == today
            val isPast     = date < today

            Box(
                modifier = Modifier
                    .width(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected)
                            Brush.linearGradient(listOf(Color(0x334F8EF7), Color(0x33A855F7)))
                        else
                            Brush.linearGradient(listOf(colors.bgCard, colors.bgCard))
                    )
                    .border(
                        1.5.dp,
                        if (isSelected) Color(0xFF4F8EF7) else colors.border,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = date.dayShortTR(),
                        fontSize = 9.sp, fontWeight = FontWeight.ExtraBold,
                        color = if (isPast) colors.text3 else colors.text2,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = date.dayOfMonth.toString(),
                        fontSize = 17.sp, fontWeight = FontWeight.ExtraBold,
                        color = when {
                            isSelected -> Color(0xFF4F8EF7)
                            isToday    -> colors.text1
                            isPast     -> colors.text3
                            else       -> colors.text2
                        }
                    )
                    Box(
                        Modifier.size(5.dp).clip(CircleShape).background(
                            if (isToday) Color(0xFFF97316)
                            else if (isSelected) Color(0xFF4F8EF7)
                            else Color.Transparent
                        )
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSlotSheet(
    existingSlot: SlotUiItem? = null,
    defaultDate: LocalDate,
    isDark: Boolean = true,
    onDismiss: () -> Unit,
    onSave: (title: String, emoji: String, subtitle: String, category: String,
             startTime: String, endTime: String, isRecurring: Boolean) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val colors    = if (isDark) DarkThemeColors else LightThemeColors
    var title     by remember(existingSlot) { mutableStateOf(existingSlot?.slot?.title ?: "") }
    var subtitle  by remember(existingSlot) { mutableStateOf(existingSlot?.slot?.subtitle ?: "") }
    var category  by remember(existingSlot) { mutableStateOf(existingSlot?.slot?.category ?: "study") }
    var startTime by remember(existingSlot) { mutableStateOf(existingSlot?.slot?.startTime ?: "") }
    var endTime   by remember(existingSlot) { mutableStateOf(existingSlot?.slot?.endTime ?: "") }
    var recurring by remember(existingSlot) { mutableStateOf(existingSlot?.slot?.isRecurring ?: true) }

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

            EditField(value = title, label = "Başlık", colors = colors) { title = it }
            EditField(value = subtitle, label = "Alt başlık (isteğe bağlı)", colors = colors) { subtitle = it }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                EditField(value = startTime, label = "Başlangıç (HH:mm)", colors = colors, modifier = Modifier.weight(1f)) { startTime = it }
                EditField(value = endTime,   label = "Bitiş (HH:mm)",     colors = colors, modifier = Modifier.weight(1f)) { endTime = it }
            }

            Text("İkon & Kategori Kataloğu", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.text3)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(CategoryLabels.keys.toList()) { cat ->
                    val isSelected = category == cat
                    val accent = categoryAccent(cat)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) accent.copy(alpha = 0.2f) else colors.bgCard2)
                            .border(1.5.dp, if (isSelected) accent else Color.Transparent, RoundedCornerShape(20.dp))
                            .clickable { category = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            LucideIcon(category = cat, tint = if (isSelected) accent else colors.text3, modifier = Modifier.size(16.dp))
                            Text(
                                CategoryLabels[cat] ?: cat,
                                fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = if (isSelected) accent else colors.text3
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(colors.bgCard2).padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Her hafta tekrarla", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.text1)
                    Text("Tüm haftalar için geçerli", fontSize = 10.sp, color = colors.text3)
                }
                Switch(
                    checked = recurring, onCheckedChange = { recurring = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF4F8EF7))
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
                        if (title.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()) {
                            onSave(title.trim(), "📌", subtitle.trim(),
                                category, startTime.trim(), endTime.trim(), recurring)
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
