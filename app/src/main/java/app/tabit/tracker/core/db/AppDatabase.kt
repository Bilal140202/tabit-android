package app.tabit.tracker.core.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HabitEntity::class, RecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao
}
