package app.tabit.tracker.feature.table

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

                    for (habit in habits) {
                        val records = monthRecords.filter { it.habitId == habit.id }
                        recordsMap[habit.id] = records
                        val allRecords = habitDao.getAllRecordsForHabit(habit.id).first()
                        val streak = StreakCalculator.currentStreak(allRecords)
                        val score = ScoringEngine.calculate(records, habit, streak)
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
            // Insert if no record exists (IGNORE if one already does)
            habitDao.insertRecordIgnore(RecordEntity(habitId = habitId, date = date, done = newDone, value = newValue))
            // Then directly update by habitId+date — works whether record was just inserted or already existed
            habitDao.updateRecordDoneByHabitAndDate(habitId, date, newDone, newValue)
        }
    }

    // FIX: Race-condition-proof note update using insertIgnore + direct note update.
    // Preserves existing done/value fields — won't destroy toggle state.
    fun updateRecordNote(habitId: Long, date: String, note: String) {
        viewModelScope.launch {
            // Insert a placeholder if no record exists (IGNORE if one already does)
            habitDao.insertRecordIgnore(RecordEntity(habitId = habitId, date = date, note = note))
            // Then directly update only the note field, preserving done/value
            habitDao.updateRecordNoteByHabitAndDate(habitId, date, note)
        }
    }
}
