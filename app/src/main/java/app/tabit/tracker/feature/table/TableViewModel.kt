package app.tabit.tracker.feature.table

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tabit.tracker.core.db.HabitDao
import app.tabit.tracker.core.db.HabitEntity
import app.tabit.tracker.core.db.RecordEntity
import app.tabit.tracker.core.utils.ScoringEngine
import app.tabit.tracker.core.utils.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TableState(
    val habits: List<HabitEntity> = emptyList(),
    val records: Map<Long, List<RecordEntity>> = emptyMap(),
    val scores: Map<Long, Float> = emptyMap(),
    val streaks: Map<Long, Int> = emptyMap(),
    val currentMonth: YearMonth = YearMonth.now(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TableViewModel @Inject constructor(
    private val habitDao: HabitDao
) : ViewModel() {
    private val _state = MutableStateFlow(TableState())
    val state: StateFlow<TableState> = _state.asStateFlow()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    init { loadTableData() }

    private fun loadTableData() {
        viewModelScope.launch {
            habitDao.getAllActiveHabits().collect { habits ->
                val recordsMap = mutableMapOf<Long, List<RecordEntity>>()
                val scoresMap = mutableMapOf<Long, Float>()
                val streaksMap = mutableMapOf<Long, Int>()
                val month = _state.value.currentMonth
                val startDate = month.atDay(1).format(formatter)
                val endDate = month.atEndOfMonth().format(formatter)
                for (habit in habits) {
                    val records = habitDao.getRecordsForHabit(habit.id, startDate, endDate).first()
                    recordsMap[habit.id] = records
                    val allRecords = habitDao.getAllRecordsForHabit(habit.id).first()
                    val streak = StreakCalculator.currentStreak(allRecords)
                    val score = ScoringEngine.calculate(records, habit, streak)
                    scoresMap[habit.id] = score
                    streaksMap[habit.id] = streak
                }
                _state.value = TableState(
                    habits = habits,
                    records = recordsMap,
                    scores = scoresMap,
                    streaks = streaksMap,
                    currentMonth = month,
                    isLoading = false
                )
            }
        }
    }

    fun changeMonth(yearMonth: YearMonth) {
        _state.value = _state.value.copy(currentMonth = yearMonth, isLoading = true)
        loadTableData()
    }

    fun toggleRecord(habitId: Long, date: String, currentDone: Boolean) {
        viewModelScope.launch {
            val existing = habitDao.getRecordsForHabit(habitId, date, date).first()
            if (existing.isEmpty()) {
                habitDao.insertRecord(RecordEntity(habitId = habitId, date = date, done = !currentDone, value = if (!currentDone) 1 else 0))
            } else {
                val record = existing.first()
                habitDao.updateRecord(record.copy(done = !currentDone, value = if (!currentDone) 1 else 0))
            }
        }
    }

    fun updateRecordNote(habitId: Long, date: String, note: String) {
        viewModelScope.launch {
            val existing = habitDao.getRecordsForHabit(habitId, date, date).first()
            if (existing.isEmpty()) {
                habitDao.insertRecord(RecordEntity(habitId = habitId, date = date, note = note))
            } else {
                habitDao.updateRecord(existing.first().copy(note = note))
            }
        }
    }
}
