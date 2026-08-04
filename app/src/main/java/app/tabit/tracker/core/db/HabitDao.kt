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

    @Query("SELECT * FROM records WHERE habitId = :habitId ORDER BY date DESC")
    fun getAllRecordsForHabit(habitId: Long): Flow<List<RecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity): Long

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)

    @Query("SELECT * FROM records WHERE habitId = :habitId AND done = 1 ORDER BY date DESC")
    suspend fun getCompletedRecords(habitId: Long): List<RecordEntity>

    @Query("SELECT COUNT(*) FROM habits WHERE isArchived = 0")
    suspend fun getActiveHabitCount(): Int

    @Query("SELECT * FROM records WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, habitId ASC")
    fun getRecordsForDateRange(startDate: String, endDate: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM habits")
    suspend fun getAllHabitsSync(): List<HabitEntity>

    @Query("SELECT * FROM records")
    suspend fun getAllRecordsSync(): List<RecordEntity>
}
