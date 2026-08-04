package app.tabit.tracker.core.utils

import app.tabit.tracker.core.db.RecordEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StreakCalculator {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun currentStreak(records: List<RecordEntity>): Int {
        if (records.isEmpty()) return 0
        val today = LocalDate.now()
        val completedDates = records
            .filter { it.done }
            .mapNotNull { runCatching { LocalDate.parse(it.date, formatter) }.getOrNull() }
            .sortedDescending()
            .distinct()
        if (completedDates.isEmpty()) return 0
        var streak = 0
        var checkDate = today
        val latestCompleted = completedDates.first()
        if (latestCompleted.isBefore(today.minusDays(1))) return 0
        checkDate = if (latestCompleted == today) today else today.minusDays(1)
        for (date in completedDates) {
            if (date == checkDate) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else if (date.isBefore(checkDate)) {
                break
            }
        }
        return streak
    }

    fun bestStreak(records: List<RecordEntity>): Int {
        if (records.isEmpty()) return 0
        val completedDates = records
            .filter { it.done }
            .mapNotNull { runCatching { LocalDate.parse(it.date, formatter) }.getOrNull() }
            .sorted()
        if (completedDates.isEmpty()) return 0
        var bestStreak = 1
        var currentStreak = 1
        for (i in 1 until completedDates.size) {
            if (completedDates[i] == completedDates[i - 1].plusDays(1)) {
                currentStreak++
                bestStreak = maxOf(bestStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }
        return bestStreak
    }

    fun monthlyStreak(records: List<RecordEntity>, year: Int, month: Int): Int {
        val monthStart = LocalDate.of(year, month, 1)
        val monthEnd = monthStart.plusMonths(1).minusDays(1)
        val monthRecords = records.filter { record ->
            runCatching {
                val date = LocalDate.parse(record.date, formatter)
                !date.isBefore(monthStart) && !date.isAfter(monthEnd)
            }.getOrDefault(false)
        }
        return currentStreak(monthRecords)
    }
}
