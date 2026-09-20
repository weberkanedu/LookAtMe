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

fun categoryAccent(cat: String)  = CategoryColors[cat]?.first  ?: Color(0xFF64748B)
fun categoryBgTint(cat: String)  = CategoryColors[cat]?.second ?: Color(0x1F64748B)

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
