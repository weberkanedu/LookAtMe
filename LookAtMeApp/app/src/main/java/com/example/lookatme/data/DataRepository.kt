package com.example.lookatme.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DatabaseProvider {
    @Volatile private var INSTANCE: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "lookatme.db"
            ).build().also { INSTANCE = it }
        }
    }
}

// ─── Default weekly template data ────────────────────────────────

val DEFAULT_SLOTS: List<SlotEntity> = listOf(
    // Monday (0)
    SlotEntity(title="Gün Tekrarı",            emoji="🔁", subtitle="Okul dersleri tarama",        category="study",  startTime="16:15", endTime="16:45", dayOfWeek=0),
    SlotEntity(title="1. Çalışma Bloku",       emoji="📐", subtitle="Günün konuları soru çözümü",  category="study",  startTime="17:00", endTime="18:00", dayOfWeek=0),
    SlotEntity(title="2. Çalışma Bloku",       emoji="🔬", subtitle="Soru çözümü & okul ödevleri",category="study",  startTime="18:15", endTime="19:15", dayOfWeek=0),
    SlotEntity(title="Akşam Yemeği",           emoji="🍽️", subtitle="",                            category="rest",   startTime="19:15", endTime="20:15", dayOfWeek=0),
    SlotEntity(title="Hızlı Okuma & Paragraf", emoji="📖", subtitle="",                            category="study",  startTime="20:15", endTime="21:00", dayOfWeek=0),
    // Tuesday (1)
    SlotEntity(title="Gün Tekrarı",            emoji="🔁", subtitle="",                            category="study",  startTime="16:15", endTime="16:45", dayOfWeek=1),
    SlotEntity(title="Soru Çözümü & Ödev",    emoji="✍️", subtitle="",                            category="study",  startTime="17:00", endTime="18:15", dayOfWeek=1),
    SlotEntity(title="Çıkış & Ulaşım",         emoji="🎒", subtitle="",                            category="sport",  startTime="18:30", endTime="19:00", dayOfWeek=1),
    SlotEntity(title="Basketbol Antrenmanı",   emoji="🏀", subtitle="",                            category="sport",  startTime="19:00", endTime="20:15", dayOfWeek=1),
    SlotEntity(title="Dönüş & Duş",            emoji="🚿", subtitle="",                            category="rest",   startTime="20:15", endTime="21:00", dayOfWeek=1),
    SlotEntity(title="Hafif Okuma / Paragraf", emoji="📑", subtitle="",                            category="study",  startTime="21:00", endTime="21:30", dayOfWeek=1),
    // Wednesday (2)
    SlotEntity(title="Gün Tekrarı",            emoji="🔁", subtitle="",                            category="study",  startTime="16:15", endTime="16:45", dayOfWeek=2),
    SlotEntity(title="1. Çalışma Bloku",       emoji="📖", subtitle="Günün konuları soru çözümü",  category="study",  startTime="17:00", endTime="18:00", dayOfWeek=2),
    SlotEntity(title="2. Çalışma Bloku",       emoji="📐", subtitle="Soru çözümü & ödevler",       category="study",  startTime="18:15", endTime="19:15", dayOfWeek=2),
    SlotEntity(title="Akşam Yemeği",           emoji="🍽️", subtitle="",                            category="rest",   startTime="19:15", endTime="20:15", dayOfWeek=2),
    SlotEntity(title="Eksik Tamamlama",        emoji="✨", subtitle="",                            category="study",  startTime="20:15", endTime="21:00", dayOfWeek=2),
    // Thursday (3)
    SlotEntity(title="Gün Tekrarı",            emoji="🔁", subtitle="",                            category="study",  startTime="16:15", endTime="16:45", dayOfWeek=3),
    SlotEntity(title="Soru Çözümü & Ödev",    emoji="✍️", subtitle="",                            category="study",  startTime="17:00", endTime="18:15", dayOfWeek=3),
    SlotEntity(title="Çıkış & Ulaşım",         emoji="🎒", subtitle="",                            category="sport",  startTime="18:30", endTime="19:00", dayOfWeek=3),
    SlotEntity(title="Basketbol Antrenmanı",   emoji="🏀", subtitle="",                            category="sport",  startTime="19:00", endTime="20:15", dayOfWeek=3),
    SlotEntity(title="Dönüş & Duş",            emoji="🚿", subtitle="",                            category="rest",   startTime="20:15", endTime="21:00", dayOfWeek=3),
    SlotEntity(title="İngilizce Kelime",       emoji="🔤", subtitle="",                            category="study",  startTime="21:00", endTime="21:30", dayOfWeek=3),
    // Friday (4)
    SlotEntity(title="Ara Öğün & Hazırlık",   emoji="🥪", subtitle="",                            category="rest",   startTime="16:00", endTime="16:30", dayOfWeek=4),
    SlotEntity(title="BİLSEM Ulaşım",          emoji="🚶", subtitle="",                            category="bilsem", startTime="16:30", endTime="16:50", dayOfWeek=4),
    SlotEntity(title="BİLSEM Dersi",           emoji="🏫", subtitle="",                            category="bilsem", startTime="16:50", endTime="20:00", dayOfWeek=4),
    SlotEntity(title="Eve Dönüş",              emoji="🏠", subtitle="",                            category="rest",   startTime="20:00", endTime="20:30", dayOfWeek=4),
    SlotEntity(title="Serbest Zaman",          emoji="🛋️", subtitle="",                            category="rest",   startTime="20:30", endTime="21:30", dayOfWeek=4),
    // Saturday (5)
    SlotEntity(title="İngilizce Kursu",        emoji="🇬🇧", subtitle="09:30 varış",               category="lang",   startTime="09:30", endTime="13:10", dayOfWeek=5),
    SlotEntity(title="Dönüş & Öğle Yemeği",   emoji="🍝", subtitle="",                            category="rest",   startTime="13:10", endTime="14:00", dayOfWeek=5),
    SlotEntity(title="Cuma Tekrarı & Soru",   emoji="📝", subtitle="",                            category="study",  startTime="14:00", endTime="15:15", dayOfWeek=5),
    SlotEntity(title="Çıkış & Ulaşım",         emoji="🎒", subtitle="",                            category="sport",  startTime="15:30", endTime="16:00", dayOfWeek=5),
    SlotEntity(title="Basketbol Antrenmanı",   emoji="🏀", subtitle="",                            category="sport",  startTime="16:00", endTime="17:15", dayOfWeek=5),
    SlotEntity(title="Dönüş & Dinlenme",       emoji="🚿", subtitle="",                            category="rest",   startTime="17:15", endTime="18:00", dayOfWeek=5),
    SlotEntity(title="Ödev & Soru Çözümü",    emoji="📚", subtitle="",                            category="study",  startTime="18:30", endTime="19:30", dayOfWeek=5),
    // Sunday (6)
    SlotEntity(title="1. Blok: Deneme/Tekrar",emoji="📊", subtitle="",                            category="study",  startTime="10:30", endTime="12:00", dayOfWeek=6),
    SlotEntity(title="Öğle Yemeği & Dinlenme",emoji="🥗", subtitle="",                            category="rest",   startTime="12:00", endTime="13:30", dayOfWeek=6),
    SlotEntity(title="2. Blok: Soru Çözümü",  emoji="📐", subtitle="",                            category="study",  startTime="13:30", endTime="15:00", dayOfWeek=6),
    SlotEntity(title="Çıkış & Ulaşım",         emoji="🎒", subtitle="",                            category="sport",  startTime="15:30", endTime="16:00", dayOfWeek=6),
    SlotEntity(title="Basketbol Antrenmanı",   emoji="🏀", subtitle="",                            category="sport",  startTime="16:00", endTime="17:15", dayOfWeek=6),
    SlotEntity(title="Dönüş & Duş",            emoji="🚿", subtitle="",                            category="rest",   startTime="17:15", endTime="18:00", dayOfWeek=6),
    SlotEntity(title="Hafta Kapanışı",         emoji="🎒", subtitle="",                            category="study",  startTime="18:30", endTime="19:30", dayOfWeek=6),
)

// ─── Repository ──────────────────────────────────────────────────

class Repository(private val db: AppDatabase) {

    private val ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun slotsForDate(date: LocalDate): Flow<List<SlotEntity>> {
        val dow = date.dayOfWeek.value - 1  // Mon=0..Sun=6
        val dateStr = date.format(ISO)
        return combine(
            db.slotDao().getRecurringForDay(dow),
            db.slotDao().getOneOffForDate(dateStr)
        ) { recurring, oneOff ->
            (recurring + oneOff).sortedBy { it.startTime }
        }
    }

    suspend fun slotsForDateOnce(date: LocalDate): List<SlotEntity> {
        val dow = date.dayOfWeek.value - 1
        val dateStr = date.format(ISO)
        return (db.slotDao().getRecurringForDayOnce(dow) + db.slotDao().getOneOffForDateOnce(dateStr))
            .sortedBy { it.startTime }
    }

    fun completionsForDate(date: LocalDate): Flow<List<CompletionEntity>> {
        return db.completionDao().getForDate(date.format(ISO))
    }

    suspend fun completionsForDateOnce(date: LocalDate): List<CompletionEntity> {
        return db.completionDao().getForDateOnce(date.format(ISO))
    }

    suspend fun completionsForRange(from: LocalDate, to: LocalDate): List<CompletionEntity> {
        return db.completionDao().getForRange(from.format(ISO), to.format(ISO))
    }

    suspend fun upsertCompletion(slotId: Long, date: LocalDate, status: String, note: String) {
        db.completionDao().insert(
            CompletionEntity(slotId = slotId, date = date.format(ISO), status = status, note = note)
        )
    }

    suspend fun deleteCompletion(slotId: Long, date: LocalDate) {
        db.completionDao().delete(slotId, date.format(ISO))
    }

    suspend fun insertSlot(slot: SlotEntity): Long {
        return db.slotDao().insert(slot)
    }

    suspend fun updateSlot(slot: SlotEntity) {
        db.slotDao().update(slot)
    }

    suspend fun deleteSlot(id: Long) {
        db.slotDao().softDelete(id)
    }

    fun allActiveSlots(): Flow<List<SlotEntity>> = db.slotDao().getAllActive()

    suspend fun getSlotById(id: Long): SlotEntity? = db.slotDao().getById(id)

    suspend fun seedDefaultData() {
        DEFAULT_SLOTS.forEach { db.slotDao().insert(it) }
    }

    suspend fun resetAllData() {
        db.slotDao().deleteAll()
        seedDefaultData()
    }
}
