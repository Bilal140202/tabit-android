package app.tabit.tracker.core.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "habits",
    indices = [Index(value = ["name"], unique = false)]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Long = 0xFF2196F3,
    val target: Int = 1,
    val weight: Float = 1f,
    val frequency: String = "daily",
    val customFrequencyDays: Int = 1,
    val reminderHour: Int = -1,
    val reminderMinute: Int = -1,
    val isArchived: Boolean = false,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val note: String = ""
)
