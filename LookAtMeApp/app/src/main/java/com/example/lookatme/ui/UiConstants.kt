package com.example.lookatme.ui

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── Theme Colors ────────────────────────────────────────────────

data class ThemeColors(
    val bgApp: Color,
    val bgCard: Color,
    val bgCard2: Color,
    val text1: Color,
    val text2: Color,
    val text3: Color,
    val border: Color
)

val DarkThemeColors = ThemeColors(
    bgApp = Color(0xFF0F1117),
    bgCard = Color(0xFF1A1D27),
    bgCard2 = Color(0xFF20243A),
    text1 = Color(0xFFF0F2FF),
    text2 = Color(0xFFA0A8C8),
    text3 = Color(0xFF606882),
    border = Color.White.copy(alpha = 0.08f)
)

val LightThemeColors = ThemeColors(
    bgApp = Color(0xFFF4F5F7),     // Eye-friendly warm off-white
    bgCard = Color(0xFFFFFFFF),    // Crisp white cards
    bgCard2 = Color(0xFFEBEEF2),   // Soft secondary background
    text1 = Color(0xFF1E293B),     // Dark slate primary text
    text2 = Color(0xFF475569),     // Slate secondary text
    text3 = Color(0xFF94A3B8),     // Muted text
    border = Color(0xFFE2E8F0)     // Soft border
)

// ─── Category colors ─────────────────────────────────────────────

val CategoryColors = mapOf(
    "study"    to Triple(Color(0xFF4F8EF7), Color(0x1F4F8EF7), Color(0xFFE0ECFE)),
    "sport"    to Triple(Color(0xFFF97316), Color(0x1FF97316), Color(0xFFFFEDD5)),
    "code"     to Triple(Color(0xFF06B6D4), Color(0x1F06B6D4), Color(0xFFCFFAFE)),
    "reading"  to Triple(Color(0xFF3B82F6), Color(0x1F3B82F6), Color(0xFFDBEAFE)),
    "art"      to Triple(Color(0xFFEC4899), Color(0x1FEC4899), Color(0xFFFCE7F3)),
    "music"    to Triple(Color(0xFF8B5CF6), Color(0x1F8B5CF6), Color(0xFFEDE9FE)),
    "game"     to Triple(Color(0xFF10B981), Color(0x1F10B981), Color(0xD1FAE5)),
    "travel"   to Triple(Color(0xFFF59E0B), Color(0x1FF59E0B), Color(0xFFFEF3C7)),
    "shopping" to Triple(Color(0xFFE11D48), Color(0x1FE11D48), Color(0xFFFFE4E6)),
    "health"   to Triple(Color(0xFFEF4444), Color(0x1FEF4444), Color(0xFEE2E2)),
    "meeting"  to Triple(Color(0xFF6366F1), Color(0x1F6366F1), Color(0xFFE0E7FF)),
    "food"     to Triple(Color(0xFFD97706), Color(0x1FD97706), Color(0xFFFEF3C7)),
    "rest"     to Triple(Color(0xFF64748B), Color(0x1F64748B), Color(0xFFF1F5F9)),
    "bilsem"   to Triple(Color(0xFFA855F7), Color(0x1FA855F7), Color(0xFFF3E8FF)),
    "lang"     to Triple(Color(0xFF22C55E), Color(0x1F22C55E), Color(0xFFDCFCE7)),
)

fun parseColorHex(hex: String, defaultColor: Color = Color(0xFF4F8EF7)): Color {
    if (hex.isBlank()) return defaultColor
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> (0xFF000000 or clean.toLong(16)).toInt()
            8 -> clean.toLong(16).toInt()
            else -> return defaultColor
        }
        Color(colorInt)
    } catch (_: Exception) {
        defaultColor
    }
}

fun categoryAccent(cat: String, customColorHex: String = ""): Color {
    if (customColorHex.isNotBlank()) {
        return parseColorHex(customColorHex, CategoryColors[cat]?.first ?: Color(0xFF64748B))
    }
    return CategoryColors[cat]?.first ?: Color(0xFF64748B)
}

fun categoryBgTint(cat: String, customColorHex: String = ""): Color {
    if (customColorHex.isNotBlank()) {
        return parseColorHex(customColorHex, Color(0xFF64748B)).copy(alpha = 0.15f)
    }
    return CategoryColors[cat]?.second ?: Color(0x1F64748B)
}

// 40-color palette matching the screenshot!
val PRESET_PALETTE_COLORS = listOf(
    // Row 1: Reds, corals, oranges
    Color(0xFFFF3B30), Color(0xFFFF5252), Color(0xFFD32F2F), Color(0xFFFF4081), Color(0xFFFF1493), Color(0xFFFF8C00), Color(0xFFFF6D00), Color(0xFFFF5722),
    // Row 2: Ambers, yellows, limes, greens
    Color(0xFFF57C00), Color(0xFFFFB300), Color(0xFFFFD600), Color(0xFFFFEB3B), Color(0xFFCDDC39), Color(0xFFAEEA00), Color(0xFF76FF03), Color(0xFF4CAF50),
    // Row 3: Emeralds, mint, teals, cyans
    Color(0xFF00C853), Color(0xFF10B981), Color(0xFF26A69A), Color(0xFF4DD0E1), Color(0xFF00838F), Color(0xFF00ACC1), Color(0xFF00B4D8), Color(0xFF48CAE4),
    // Row 4: Sky, blues, indigos, purples
    Color(0xFF007AFF), Color(0xFF1E88E5), Color(0xFF2196F3), Color(0xFF4F8EF7), Color(0xFF5C6BC0), Color(0xFF6366F1), Color(0xFF7E57C2), Color(0xFF673AB7),
    // Row 5: Purples, fuchsias, pinks, browns, grays, white
    Color(0xFFAB47BC), Color(0xFFE040FB), Color(0xFFEC4899), Color(0xFF8D6E63), Color(0xFF795548), Color(0xFF9E9E9E), Color(0xFF607D8B), Color(0xFFFFFFFF)
)

data class CoverStyle(
    val id: String,
    val name: String,
    val gradientColors: List<Color>
)

val PRESET_COVER_STYLES = listOf(
    CoverStyle("default", "Standart", emptyList()),
    CoverStyle("gradient_sunset", "Günbatımı", listOf(Color(0xFFFF512F), Color(0xFFDD2476))),
    CoverStyle("gradient_ocean", "Okyanus", listOf(Color(0xFF2193b0), Color(0xFF6dd5ed))),
    CoverStyle("gradient_emerald", "Zümrüt", listOf(Color(0xFF11998e), Color(0xFF38ef7d))),
    CoverStyle("gradient_purple", "Neon Mor", listOf(Color(0xFF8A2387), Color(0xFFE94057))),
    CoverStyle("solid", "Renk Tonu", emptyList())
)

val CategoryLabels = mapOf(
    "study"    to "Ders",
    "sport"    to "Spor",
    "code"     to "Yazılım",
    "reading"  to "Okuma",
    "art"      to "Sanat",
    "music"    to "Müzik",
    "game"     to "Oyun",
    "travel"   to "Seyahat",
    "shopping" to "Alışveriş",
    "health"   to "Sağlık",
    "meeting"  to "Toplantı",
    "food"     to "Yemek",
    "rest"     to "Dinlenme",
    "bilsem"   to "BİLSEM",
    "lang"     to "Dil",
)

val TR_DAYS_SHORT  = listOf("Pzt","Sal","Çar","Per","Cum","Cmt","Paz")
val TR_DAYS_LONG   = listOf("Pazartesi","Salı","Çarşamba","Perşembe","Cuma","Cumartesi","Pazar")
val TR_MONTHS      = listOf("Ocak","Şubat","Mart","Nisan","Mayıs","Haziran",
                             "Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık")

val HIJRI_MONTHS   = listOf("Muharrem", "Sefer", "Rebiülevvel", "Rebiülahir",
                             "Cemaziyelevvel", "Cemaziyelahir", "Recep", "Şaban",
                             "Ramazan", "Şevval", "Zilkade", "Zilhicce")

private val ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun LocalDate.formatTR(): String    = "$dayOfMonth ${TR_MONTHS[monthValue - 1]} $year"
fun LocalDate.dayNameTR(): String   = TR_DAYS_LONG[dayOfWeek.value - 1]
fun LocalDate.dayShortTR(): String  = TR_DAYS_SHORT[dayOfWeek.value - 1]
fun LocalDate.toISO(): String       = format(ISO_FMT)

fun LocalDate.formatCustom(isHijri: Boolean): String {
    if (!isHijri) return formatTR()
    return try {
        val hijrahDate = java.time.chrono.HijrahDate.from(this)
        val day = hijrahDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
        val month = hijrahDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
        val year = hijrahDate.get(java.time.temporal.ChronoField.YEAR)
        val mName = if (month in 1..12) HIJRI_MONTHS[month - 1] else "Ay $month"
        "$day $mName $year"
    } catch (e: Exception) {
        formatTR()
    }
}
