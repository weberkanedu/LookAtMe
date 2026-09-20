package com.example.lookatme.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ─── Entities ────────────────────────────────────────────────────

/**
 * A scheduled activity slot (template entry or one-off).
 * dayOfWeek: 0=Monday … 6=Sunday, or -1 for one-off (uses specificDate)
 */
@Entity(tableName = "slots")
data class SlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val emoji: String = "📌",
    val subtitle: String = "",
    val category: String = "study",   // study | sport | bilsem | lang | rest
    val startTime: String,            // "HH:mm"
    val endTime: String,              // "HH:mm"
    val dayOfWeek: Int,               // 0-6 for recurring; -1 for one-off
    val specificDate: String = "",    // "yyyy-MM-dd" when dayOfWeek == -1
    val isRecurring: Boolean = true,
    val isActive: Boolean = true
)

/**
 * A completion record for a slot on a specific date.
 */
@Entity(
    tableName = "completions",
    indices = [Index(value = ["slotId", "date"], unique = true)]
)
data class CompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val slotId: Long,
    val date: String,                 // "yyyy-MM-dd"
    val status: String,               // "done" | "missed"
    val note: String = "",
    val completedAt: Long = System.currentTimeMillis()
)

// ─── DAOs ────────────────────────────────────────────────────────

@Dao
interface SlotDao {
    @Query("SELECT * FROM slots WHERE isActive = 1 AND isRecurring = 1 AND dayOfWeek = :dow ORDER BY startTime ASC")
    fun getRecurringForDay(dow: Int): Flow<List<SlotEntity>>

    @Query("SELECT * FROM slots WHERE isActive = 1 AND isRecurring = 1 AND dayOfWeek = :dow ORDER BY startTime ASC")
    suspend fun getRecurringForDayOnce(dow: Int): List<SlotEntity>

    @Query("SELECT * FROM slots WHERE isActive = 1 AND dayOfWeek = -1 AND specificDate = :date ORDER BY startTime ASC")
    fun getOneOffForDate(date: String): Flow<List<SlotEntity>>

    @Query("SELECT * FROM slots WHERE isActive = 1 AND dayOfWeek = -1 AND specificDate = :date ORDER BY startTime ASC")
    suspend fun getOneOffForDateOnce(date: String): List<SlotEntity>

    @Query("SELECT * FROM slots WHERE isActive = 1 ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllActive(): Flow<List<SlotEntity>>

    @Query("SELECT * FROM slots WHERE id = :id")
    suspend fun getById(id: Long): SlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(slot: SlotEntity): Long

    @Update
    suspend fun update(slot: SlotEntity)

    @Query("UPDATE slots SET isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("DELETE FROM slots")
    suspend fun deleteAll()
}

@Dao
interface CompletionDao {
    @Query("SELECT * FROM completions WHERE date = :date")
    fun getForDate(date: String): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completions WHERE date = :date")
    suspend fun getForDateOnce(date: String): List<CompletionEntity>

    @Query("SELECT * FROM completions WHERE date >= :fromDate AND date <= :toDate ORDER BY date ASC")
    suspend fun getForRange(fromDate: String, toDate: String): List<CompletionEntity>

    @Query("SELECT * FROM completions WHERE slotId = :slotId AND date = :date LIMIT 1")
    suspend fun getForSlotAndDate(slotId: Long, date: String): CompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: CompletionEntity): Long

    @Query("DELETE FROM completions WHERE slotId = :slotId AND date = :date")
    suspend fun delete(slotId: Long, date: String)

    @Query("DELETE FROM completions WHERE date >= :fromDate AND date <= :toDate")
    suspend fun deleteRange(fromDate: String, toDate: String)
}

// ─── Database ────────────────────────────────────────────────────

@Database(
    entities = [SlotEntity::class, CompletionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun slotDao(): SlotDao
    abstract fun completionDao(): CompletionDao
}
