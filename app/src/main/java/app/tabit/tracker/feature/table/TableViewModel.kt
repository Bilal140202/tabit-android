package app.tabit.tracker.feature.table

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tabit.tracker.core.db.HabitDao
import app.tabit.tracker.core.db.HabitEntity
import app.tabit.tracker.core.db.RecordEntity
import app.tabit.tracker.core.utils.ScoringEngine
import app.tabit.tracker.core.utils.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    // Single source of truth for the current month
    private val _currentMonth = MutableStateFlow(YearMonth.now())

    init {
        // FIX: Combine habits AND records Flows so record changes (note, toggle)
        // trigger a re-emission. Previously only habits table was observed,
        // so adding a note or toggling a cell never updated the UI.
        @OptIn(ExperimentalCoroutinesApi::class)
        viewModelScope.launch {
            _currentMonth.flatMapLatest { month ->
                val startDate = month.atDay(1).format(formatter)
                val endDate = month.atEndOfMonth().format(formatter)

                habitDao.getAllActiveHabits().combine(
                    habitDao.getRecordsForDateRange(startDate, endDate)
                ) { habits, monthRecords ->
                    val recordsMap = mutableMapOf<Long, List<RecordEntity>>()
                    val scoresMap = mutableMapOf<Long, Float>()
                    val streaksMap = mutableMapOf<Long, Int>()

                    // Bulk fetch all records for streak/score calculation (fixes N+1)
                    val habitIds = habits.map { it.id }
                    val allRecords = if (habitIds.isNotEmpty()) {
                        habitDao.getAllRecordsSync().filter { it.habitId in habitIds }
                    } else emptyList()
                    val allRecordsByHabit = allRecords.groupBy { it.habitId }

                    for (habit in habits) {
                        val records = monthRecords.filter { it.habitId == habit.id }
                        recordsMap[habit.id] = records
                        val habitAllRecords = allRecordsByHabit[habit.id] ?: emptyList()
                        val streak = StreakCalculator.currentStreak(habitAllRecords)
                        // Use all-time records for consistent scoring across Table & Charts
                        val score = ScoringEngine.calculate(habitAllRecords, habit, streak)
                        scoresMap[habit.id] = score
                        streaksMap[habit.id] = streak
                    }
                    TableState(
                        habits = habits,
                        records = recordsMap,
                        scores = scoresMap,
                        streaks = streaksMap,
                        currentMonth = month,
                        isLoading = false
                    )
                }
            }.catch { e ->
                Log.e("TableViewModel", "Flow error", e)
                _state.value = _state.value.copy(isLoading = false)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun changeMonth(yearMonth: YearMonth) {
        _currentMonth.value = yearMonth
    }

    // FIX: Race-condition-proof toggle using insertIgnore + direct update.
    // Previously used .first() on a Flow which could return stale data,
    // and OnConflictStrategy.REPLACE could silently overwrite existing records.
    fun toggleRecord(habitId: Long, date: String, currentDone: Boolean) {
        viewModelScope.launch {
            val newDone = !currentDone
            val newValue = if (newDone) 1 else 0
            habitDao.toggleRecord(habitId, date, newDone, newValue)
        }
    }

    // FIX: Race-condition-proof note update using insertIgnore + direct note update.
    // Preserves existing done/value fields — won't destroy toggle state.
    fun updateRecordNote(habitId: Long, date: String, note: String) {
        viewModelScope.launch {
            habitDao.updateRecordNote(habitId, date, note)
        }
    }
}
