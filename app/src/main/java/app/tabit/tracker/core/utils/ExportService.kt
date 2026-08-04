package app.tabit.tracker.core.utils

import app.tabit.tracker.core.db.HabitDao
import app.tabit.tracker.core.db.HabitEntity
import app.tabit.tracker.core.db.RecordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object ExportService {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun exportCsv(
        dao: HabitDao,
        outputStream: OutputStream
    ) = withContext(Dispatchers.IO) {
        val habits = dao.getAllHabitsSync()
        val records = dao.getAllRecordsSync()
        val buffer = StringBuilder()
        buffer.appendLine("Date,Habit,Done,Note,Score")
        val scores = mutableMapOf<Long, Float>()
        habits.forEach { habit ->
            val habitRecords = records.filter { it.habitId == habit.id }
            val streak = StreakCalculator.currentStreak(habitRecords)
            scores[habit.id] = ScoringEngine.calculate(habitRecords, habit, streak)
        }
        records.sortedBy { it.date }.forEach { record ->
            val habit = habits.find { it.id == record.habitId }
            val score = scores[record.habitId] ?: 0f
            buffer.appendLine(
                "${record.date}," +
                "\"${habit?.name ?: "Unknown"}\"," +
                "${if (record.done) 1 else 0}," +
                "\"${record.note}\"," +
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
        root.put("version", 1)
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
            h.put("isArchived", habit.isArchived)
            h.put("note", habit.note)
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
            recordsArray.put(r)
        }
        root.put("records", recordsArray)
        val data = root.toString(2)
        outputStream.write(data.toByteArray())
        outputStream.flush()
    }
}
