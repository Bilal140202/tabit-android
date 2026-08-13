package app.tabit.tracker.core.utils

import app.tabit.tracker.core.db.RecordEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object StreakCalculator {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun currentStreak(records: List<RecordEntity>, habitType: String = "positive"): Int {
        if (records.isEmpty()) return 0
        val today = LocalDate.now()

        val isNegative = habitType == "negative"

        // Filter out skipped records
        val relevantRecords = records.filter { it.status != "skip" }

        // For positive habits: streak of done days
        // For negative habits: streak of NOT-done days
        val qualifyingDates = relevantRecords
            .filter { if (isNegative) !it.done else it.done }
            .mapNotNull { runCatching { LocalDate.parse(it.date, formatter) }.getOrNull() }
            .toSet()

        if (qualifyingDates.isEmpty()) return 0

        val sortedDates = qualifyingDates.sortedDescending()
        var streak = 0
        var checkDate = today
        val latest = sortedDates.first()

        if (latest.isBefore(today.minusDays(1))) return 0
        checkDate = if (latest == today) today else today.minusDays(1)

        for (date in sortedDates) {
            if (date == checkDate) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else if (date.isBefore(checkDate)) {
                break
            }
        }
        return streak
    }

    fun bestStreak(records: List<RecordEntity>, habitType: String = "positive"): Int {
        if (records.isEmpty()) return 0

        val isNegative = habitType == "negative"
        val relevantRecords = records.filter { it.status != "skip" }

        val qualifyingDates = relevantRecords
            .filter { if (isNegative) !it.done else it.done }
            .mapNotNull { runCatching { LocalDate.parse(it.date, formatter) }.getOrNull() }
            .sorted()

        if (qualifyingDates.isEmpty()) return 0
        var bestStreak = 1
        var currentStreak = 1
        for (i in 1 until qualifyingDates.size) {
            if (qualifyingDates[i] == qualifyingDates[i - 1].plusDays(1)) {
                currentStreak++
                bestStreak = maxOf(bestStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }
        return bestStreak
    }

    fun monthlyStreak(records: List<RecordEntity>, year: Int, month: Int, habitType: String = "positive"): Int {
        val monthStart = LocalDate.of(year, month, 1)
        val monthEnd = monthStart.plusMonths(1).minusDays(1)
        val monthRecords = records.filter { record ->
            runCatching {
                val date = LocalDate.parse(record.date, formatter)
                !date.isBefore(monthStart) && !date.isAfter(monthEnd)
            }.getOrDefault(false)
        }
        return currentStreak(monthRecords, habitType)
    }
}
