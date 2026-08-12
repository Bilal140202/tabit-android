package app.tabit.tracker.core.utils

import app.tabit.tracker.core.db.HabitEntity
import app.tabit.tracker.core.db.RecordEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object ScoringEngine {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private const val STREAK_3_DAY_BONUS = 0.03f
    private const val STREAK_7_DAY_BONUS = 0.06f
    private const val STREAK_30_DAY_BONUS = 0.10f

    /**
     * Calculate a 0.0-1.0 score for a habit based on all its records.
     *
     * The score reflects the overall completion rate:
     *   unique completed days / total days since creation.
     * Small additive bonuses for active streaks are added on top.
     */
    fun calculate(
        records: List<RecordEntity>,
        habit: HabitEntity,
        currentStreak: Int
    ): Float {
        if (records.isEmpty()) return 0f

        // Unique days on which this habit was completed
        val completedDates = records
            .asSequence()
            .filter { it.done }
            .mapNotNull { runCatching { LocalDate.parse(it.date, formatter) }.getOrNull() }
            .map { it.toEpochDay() }
            .distinct()
            .toList()

        if (completedDates.isEmpty()) return 0f

        // Total days since habit creation (at least 1 to avoid division by zero)
        val createdDate = runCatching {
            LocalDate.ofEpochDay(habit.createdAt / 86_400_000)
        }.getOrNull() ?: LocalDate.now().minusDays(7)
        val today = LocalDate.now()
        val totalDays = maxOf(1L, ChronoUnit.DAYS.between(createdDate, today) + 1)

        // Core completion rate
        val completionRate = (completedDates.size.toFloat() / totalDays)
            .coerceIn(0f, 1f)

        // Apply habit weight as a modifier (clamped so it never exceeds 1.0 on its own)
        val weightedRate = (completionRate * habit.weight.coerceIn(0.5f, 2f))
            .coerceIn(0f, 1f)

        // Additive streak bonus
        val streakBonus = calculateStreakBonus(currentStreak)

        return (weightedRate + streakBonus).coerceIn(0f, 1f)
    }

    fun scoreToGradientFraction(score: Float): Float = score.coerceIn(0f, 1f)

    private fun calculateStreakBonus(streak: Int): Float = when {
        streak >= 30 -> STREAK_30_DAY_BONUS
        streak >= 7 -> STREAK_7_DAY_BONUS
        streak >= 3 -> STREAK_3_DAY_BONUS
        else -> 0f
    }
}
