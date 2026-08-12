package app.tabit.tracker.core.utils

import app.tabit.tracker.core.db.HabitEntity
import app.tabit.tracker.core.db.RecordEntity
import java.time.LocalTime

object ScoringEngine {
    private const val MORNING_CUTOFF_HOUR = 9
    private const val MORNING_WEIGHT_BONUS = 1.1f
    private const val STREAK_3_DAY_BONUS = 0.03f
    private const val STREAK_7_DAY_BONUS = 0.08f
    private const val STREAK_30_DAY_BONUS = 0.12f

    /**
     * Calculate score for a habit based on its records.
     * Score is a weighted completion rate with small additive bonuses for streaks and morning completion.
     * Range: 0.0 to 1.0
     */
    fun calculate(
        records: List<RecordEntity>,
        habit: HabitEntity,
        currentStreak: Int
    ): Float {
        if (records.isEmpty()) return 0f
        val completedCount = records.count { it.done }
        val completionRate = completedCount.coerceAtMost(habit.target) / habit.target.toFloat()
        // Apply weight as a modifier to the completion rate (clamped to avoid extremes)
        val weightedRate = (completionRate * habit.weight.coerceIn(0.5f, 2f)).coerceIn(0f, 1f)
        // Small additive bonuses for streaks and morning completion
        val morningBonus = if (isMorningCompletion()) 0.02f else 0f
        val streakBonus = calculateStreakBonus(currentStreak)
        return (weightedRate + morningBonus + streakBonus).coerceIn(0f, 1f)
    }

    fun scoreToGradientFraction(score: Float): Float = score.coerceIn(0f, 1f)

    private fun isMorningCompletion(): Boolean {
        val now = LocalTime.now()
        return now.hour < MORNING_CUTOFF_HOUR
    }

    private fun calculateStreakBonus(streak: Int): Float = when {
        streak >= 30 -> STREAK_30_DAY_BONUS
        streak >= 7 -> STREAK_7_DAY_BONUS
        streak >= 3 -> STREAK_3_DAY_BONUS
        else -> 0f
    }
}
