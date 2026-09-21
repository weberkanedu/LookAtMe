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
            ).addMigrations(MIGRATION_1_2).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}

// ─── Default Categories (Empty - user creates their own via UI) ──
val DEFAULT_CATEGORIES: List<CategoryEntity> = emptyList()

val OLD_DEFAULT_CATEGORY_IDS = setOf(
    "finance", "fitness", "health", "study", "mindfulness", "productivity", "code", "rest"
)

// ─── Default weekly template data (empty by default - user creates their own) ───

val DEFAULT_SLOTS: List<SlotEntity> = emptyList()


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

    fun allCompletions(): Flow<List<CompletionEntity>> = db.completionDao().getAllFlow()

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

    suspend fun clearAllSlots() {
        db.slotDao().deleteAll()
    }

    // ─── Categories ───────────────────────────────────────────────

    fun allCategories(): Flow<List<CategoryEntity>> = db.categoryDao().getAllFlow()

    suspend fun getAllCategoriesOnce(): List<CategoryEntity> {
        return db.categoryDao().getAllOnce()
    }

    suspend fun clearDefaultCategories() {
        OLD_DEFAULT_CATEGORY_IDS.forEach { id ->
            db.categoryDao().delete(id)
        }
    }

    suspend fun insertCategory(category: CategoryEntity) {
        db.categoryDao().insert(category)
    }

    suspend fun deleteCategory(id: String) {
        db.categoryDao().delete(id)
    }

    suspend fun seedDefaultData() {
        DEFAULT_SLOTS.forEach { db.slotDao().insert(it) }
    }

    suspend fun resetAllData() {
        db.slotDao().deleteAll()
        seedDefaultData()
    }
}
