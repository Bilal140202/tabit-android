package app.tabit.tracker.core.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY position ASC")
    fun getAllActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY position ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("SELECT * FROM records WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRecordsForHabit(habitId: Long, startDate: String, endDate: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE date = :date")
    suspend fun getRecordsForDate(date: String): List<RecordEntity>

    @Transaction
    suspend fun toggleRecord(habitId: Long, date: String, done: Boolean, value: Int) {
        insertRecordIgnore(RecordEntity(habitId = habitId, date = date, done = done, value = value))
        updateRecordDoneByHabitAndDate(habitId, date, done, value)
    }

    @Transaction
    suspend fun toggleRecordWithStatus(habitId: Long, date: String, done: Boolean, value: Int, status: String) {
        insertRecordIgnore(RecordEntity(habitId = habitId, date = date, done = done, value = value, status = status))
        updateRecordStatusByHabitAndDate(habitId, date, done, value, status)
    }

    @Transaction
    suspend fun updateRecordNote(habitId: Long, date: String, note: String) {
        insertRecordIgnore(RecordEntity(habitId = habitId, date = date, note = note))
        updateRecordNoteByHabitAndDate(habitId, date, note)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecordIgnore(record: RecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<RecordEntity>): List<Long>

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)

    /** Safe upsert: insert if not exists, then update by habitId+date key. Race-condition proof. */
    @Query("UPDATE records SET done = :done, value = :value, note = :note WHERE habitId = :habitId AND date = :date")
    suspend fun updateRecordByHabitAndDate(habitId: Long, date: String, done: Boolean, value: Int, note: String)

    /** Update only the note field for a specific habit+date, preserving done/value. */
    @Query("UPDATE records SET note = :note WHERE habitId = :habitId AND date = :date")
    suspend fun updateRecordNoteByHabitAndDate(habitId: Long, date: String, note: String)

    /** Update only the done/value fields for a specific habit+date, preserving note. */
    @Query("UPDATE records SET done = :done, value = :value WHERE habitId = :habitId AND date = :date")
    suspend fun updateRecordDoneByHabitAndDate(habitId: Long, date: String, done: Boolean, value: Int)

    /** Update done/value/status fields for a specific habit+date, preserving note. */
    @Query("UPDATE records SET done = :done, value = :value, status = :status WHERE habitId = :habitId AND date = :date")
    suspend fun updateRecordStatusByHabitAndDate(habitId: Long, date: String, done: Boolean, value: Int, status: String)

    @Query("SELECT * FROM records WHERE habitId = :habitId AND done = 1 ORDER BY date DESC")
    suspend fun getCompletedRecords(habitId: Long): List<RecordEntity>

    @Query("SELECT COUNT(*) FROM habits WHERE isArchived = 0")
    suspend fun getActiveHabitCount(): Int

    @Query("SELECT * FROM records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, habitId ASC")
    fun getRecordsForDateRange(startDate: String, endDate: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, habitId ASC")
    suspend fun getRecordsForDateRangeSync(startDate: String, endDate: String): List<RecordEntity>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsSync(): List<HabitEntity>

    @Query("SELECT * FROM records")
    suspend fun getAllRecordsSync(): List<RecordEntity>

    @Query("SELECT MAX(position) FROM habits")
    suspend fun getMaxPosition(): Int?

    @Query("SELECT * FROM habits WHERE isArchived = 1 ORDER BY position ASC")
    fun getArchivedHabits(): Flow<List<HabitEntity>>

    @Query("UPDATE habits SET isArchived = 0 WHERE id = :id")
    suspend fun restoreHabit(id: Long)

    @Query("UPDATE habits SET isArchived = 1 WHERE id = :id")
    suspend fun archiveHabit(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<HabitEntity>): List<Long>
}