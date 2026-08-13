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
     * For **positive** habits: completion rate = completed non-skipped days / total non-skipped days.
     * For **negative** habits ("do less"): rate is INVERTED —
     *   (non-completed days - completed days) / total non-skipped days, clamped to 0-1.
     * Records with status == "skip" are excluded from both numerator and denominator.
     */
    fun calculate(
        records: List<RecordEntity>,
        habit: HabitEntity,
        currentStreak: Int
    ): Float {
        if (records.isEmpty()) return 0f

        // Filter out skipped records
        val relevantRecords = records.filter { it.status != "skip" }
        if (relevantRecords.isEmpty()) return 0f

        val isNegative = habit.habitType == "negative"

        // Unique non-skipped days
        val uniqueDates = relevantRecords
            .asSequence()
            .mapNotNull { runCatching { LocalDate.parse(it.date, formatter) }.getOrNull() }
            .distinct()
            .toSet()

        if (uniqueDates.isEmpty()) return 0f

        // Unique completed non-skipped days
        val completedDates = relevantRecords
            .asSequence()
            .filter { it.done }
            .mapNotNull { runCatching { LocalDate.parse(it.date, formatter) }.getOrNull() }
            .distinct()
            .toSet()

        val totalNonSkippedDays = uniqueDates.size

        // Core completion rate
        val completionRate = if (isNegative) {
            // For negative habits: (not-done days - done days) / total days
            val notDoneDays = totalNonSkippedDays - completedDates.size
            ((notDoneDays - completedDates.size).toFloat() / totalNonSkippedDays).coerceIn(0f, 1f)
        } else {
            (completedDates.size.toFloat() / totalNonSkippedDays).coerceIn(0f, 1f)
        }

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
