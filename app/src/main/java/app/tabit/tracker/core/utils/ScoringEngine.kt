package app.tabit.tracker.core.utils

import app.tabit.tracker.core.db.HabitEntity
import app.tabit.tracker.core.db.RecordEntity
import java.time.LocalTime

object ScoringEngine {
    private const val MORNING_CUTOFF_HOUR = 9
    private const val MORNING_WEIGHT_BONUS = 1.2f
    private const val STREAK_3_DAY_BONUS = 0.1f
    private const val STREAK_7_DAY_BONUS = 0.2f

    fun calculate(
        records: List<RecordEntity>,
        habit: HabitEntity,
        currentStreak: Int
    ): Float {
        if (records.isEmpty()) return 0f
        val completedCount = records.count { it.done }
        val completionRate = completedCount.coerceAtMost(habit.target) / habit.target.toFloat()
        val morningFactor = if (isMorningCompletion()) MORNING_WEIGHT_BONUS else 1f
        val streakBonus = calculateStreakBonus(currentStreak)
        val score = (completionRate * habit.weight * morningFactor) + streakBonus
        return score.coerceIn(0f, 1f)
    }

    fun calculateDailyScore(
        previousScore: Float,
        completed: Boolean,
        habit: HabitEntity
    ): Float {
        if (completed) {
            val increment = (1f / habit.target) * habit.weight
            return (previousScore + increment * 0.3f).coerceIn(0f, 1f)
        } else {
            val decrease = 1f / (habit.target * 7f)
            return (previousScore - decrease).coerceIn(0f, 1f)
        }
    }

    fun scoreToGradientFraction(score: Float): Float = score.coerceIn(0f, 1f)

    private fun isMorningCompletion(): Boolean {
        val now = LocalTime.now()
        return now.hour < MORNING_CUTOFF_HOUR
    }

    private fun calculateStreakBonus(streak: Int): Float = when {
        streak >= 7 -> STREAK_7_DAY_BONUS
        streak >= 3 -> STREAK_3_DAY_BONUS
        else -> 0f
    }
}
