package app.tabit.tracker.core.utils

import app.tabit.tracker.core.db.HabitDao
import app.tabit.tracker.core.db.HabitEntity
import app.tabit.tracker.core.db.RecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ExportService {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private fun csvEscape(value: String): String {
        return "\"${value.replace("\"", "\"\"")}\""
    }

    suspend fun exportCsv(
        dao: HabitDao,
        outputStream: OutputStream
    ) = withContext(Dispatchers.IO) {
        val habits = dao.getAllHabitsSync()
        val records = dao.getAllRecordsSync()
        val buffer = StringBuilder()
        buffer.appendLine("Date,Habit,Done,Status,Note,Score")
        val scores = mutableMapOf<Long, Float>()
        habits.forEach { habit ->
            val habitRecords = records.filter { it.habitId == habit.id }
            val streak = StreakCalculator.currentStreak(habitRecords, habit.habitType)
            scores[habit.id] = ScoringEngine.calculate(habitRecords, habit, streak)
        }
        records.sortedBy { it.date }.forEach { record ->
            val habit = habits.find { it.id == record.habitId }
            val score = scores[record.habitId] ?: 0f
            buffer.appendLine(
                csvEscape(record.date) + "," +
                csvEscape(habit?.name ?: "Unknown") + "," +
                "${if (record.done) 1 else 0}," +
                csvEscape(record.status) + "," +
                csvEscape(record.note) + "," +
                "${"%.2f".format(score)}"
            )
        }
        val data = buffer.toString()
        outputStream.write(data.toByteArray())
        outputStream.flush()
    }

    suspend fun exportJson(
        dao: HabitDao,
        outputStream: OutputStream
    ) = withContext(Dispatchers.IO) {
        val habits = dao.getAllHabitsSync()
        val records = dao.getAllRecordsSync()
        val root = JSONObject()
        root.put("app", "tabit")
        root.put("version", 2)
        root.put("exportDate", LocalDate.now().format(dateFormatter))
        val habitsArray = JSONArray()
        habits.forEach { habit ->
            val h = JSONObject()
            h.put("id", habit.id)
            h.put("name", habit.name)
            h.put("color", habit.color)
            h.put("target", habit.target)
            h.put("weight", habit.weight)
            h.put("frequency", habit.frequency)
            h.put("customFrequencyDays", habit.customFrequencyDays)
            h.put("reminderHour", habit.reminderHour)
            h.put("reminderMinute", habit.reminderMinute)
            h.put("isArchived", habit.isArchived)
            h.put("position", habit.position)
            h.put("createdAt", habit.createdAt)
            h.put("note", habit.note)
            h.put("description", habit.description)
            h.put("habitType", habit.habitType)
            h.put("dailyGoalUnit", habit.dailyGoalUnit)
            h.put("dailyGoalExtra", habit.dailyGoalExtra)
            habitsArray.put(h)
        }
        root.put("habits", habitsArray)
        val recordsArray = JSONArray()
        records.forEach { record ->
            val r = JSONObject()
            r.put("id", record.id)
            r.put("habitId", record.habitId)
            r.put("date", record.date)
            r.put("done", record.done)
            r.put("value", record.value)
            r.put("note", record.note)
            r.put("status", record.status)
            recordsArray.put(r)
        }
        root.put("records", recordsArray)
        val data = root.toString(2)
        outputStream.write(data.toByteArray())
        outputStream.flush()
    }

    /**
     * Import habits and records from a JSON input stream.
     * Uses REPLACE strategy so existing data with matching IDs is overwritten.
     */
    suspend fun importJson(
        dao: HabitDao,
        inputStream: InputStream
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            val text = inputStream.bufferedReader().readText()
            val root = JSONObject(text)

            if (root.optString("app") != "tabit") {
                return@withContext ImportResult(success = false, message = "Not a valid Tabit export file")
            }

            val habitsArray = root.optJSONArray("habits") ?: JSONArray()
            val recordsArray = root.optJSONArray("records") ?: JSONArray()

            val habitsToInsert = mutableListOf<HabitEntity>()
            for (i in 0 until habitsArray.length()) {
                val h = habitsArray.getJSONObject(i)
                habitsToInsert.add(
                    HabitEntity(
                        id = h.optLong("id", 0),
                        name = h.optString("name", ""),
                        color = h.optLong("color", 0xFF2196F3),
                        target = h.optInt("target", 1),
                        weight = h.optDouble("weight", 1.0).toFloat(),
                        frequency = h.optString("frequency", "daily"),
                        customFrequencyDays = h.optInt("customFrequencyDays", 1),
                        reminderHour = h.optInt("reminderHour", -1),
                        reminderMinute = h.optInt("reminderMinute", -1),
                        isArchived = h.optBoolean("isArchived", false),
                        position = h.optInt("position", 0),
                        createdAt = h.optLong("createdAt", System.currentTimeMillis()),
                        note = h.optString("note", ""),
                        description = h.optString("description", ""),
                        habitType = h.optString("habitType", "positive"),
                        dailyGoalUnit = h.optString("dailyGoalUnit", "times"),
                        dailyGoalExtra = h.optInt("dailyGoalExtra", 0)
                    )
                )
            }

            val recordsToInsert = mutableListOf<RecordEntity>()
            for (i in 0 until recordsArray.length()) {
                val r = recordsArray.getJSONObject(i)
                recordsToInsert.add(
                    RecordEntity(
                        id = r.optLong("id", 0),
                        habitId = r.optLong("habitId", 0),
                        date = r.optString("date", ""),
                        done = r.optBoolean("done", false),
                        value = r.optInt("value", 0),
                        note = r.optString("note", ""),
                        status = r.optString("status", "none")
                    )
                )
            }

            dao.insertHabits(habitsToInsert)
            dao.insertRecords(recordsToInsert)

            ImportResult(
                success = true,
                message = "Imported ${habitsToInsert.size} habits and ${recordsToInsert.size} records"
            )
        } catch (e: Exception) {
            ImportResult(success = false, message = "Import failed: ${e.message}")
        }
    }
}

data class ImportResult(
    val success: Boolean,
    val message: String
)