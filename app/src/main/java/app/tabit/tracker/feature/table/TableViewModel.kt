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

    private val _currentMonth = MutableStateFlow(YearMonth.now())

    private val _allRecordsCache = MutableStateFlow<Map<Long, List<RecordEntity>>>(emptyMap())

    init {
        viewModelScope.launch { refreshCache() }

        @OptIn(ExperimentalCoroutinesApi::class)
        viewModelScope.launch {
            _currentMonth.flatMapLatest { month ->
                val startDate = month.atDay(1).format(formatter)
                val endDate = month.atEndOfMonth().format(formatter)

                habitDao.getAllActiveHabits().combine(
                    habitDao.getRecordsForDateRange(startDate, endDate)
                ) { habits, monthRecords ->
                    habits to monthRecords
                }.combine(_allRecordsCache) { (habits, monthRecords), allRecordsMap ->

                    val recordsMap = mutableMapOf<Long, List<RecordEntity>>()
                    val scoresMap = mutableMapOf<Long, Float>()
                    val streaksMap = mutableMapOf<Long, Int>()

                    for (habit in habits) {
                        val records = monthRecords.filter { it.habitId == habit.id }
                        recordsMap[habit.id] = records
                        val habitAllRecords = allRecordsMap[habit.id] ?: emptyList()
                        val streak = StreakCalculator.currentStreak(habitAllRecords, habit.habitType)
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

    /**
     * 3-state toggle: none → done → skip → none
     */
    fun toggleRecord(habitId: Long, date: String, currentDone: Boolean, currentStatus: String) {
        viewModelScope.launch {
            val existing = habitDao.getRecordForHabitAndDate(habitId, date)
            val status = existing?.status ?: "none"
            val (newDone, newValue, newStatus) = when (status) {
                "none" -> Triple(true, 1, "done")
                "done" -> Triple(false, 0, "skip")
                "skip" -> Triple(false, 0, "none")
                else -> Triple(true, 1, "done")
            }
            habitDao.toggleRecordWithStatus(habitId, date, newDone, newValue, newStatus)
            refreshCache()
        }
    }

    fun updateRecordNote(habitId: Long, date: String, note: String) {
        viewModelScope.launch {
            habitDao.updateRecordNote(habitId, date, note)
            refreshCache()
        }
    }

    private suspend fun refreshCache() {
        val habits = habitDao.getAllHabitsSync()
        val habitIds = habits.map { it.id }
        val all = if (habitIds.isNotEmpty()) {
            habitDao.getAllRecordsSync().filter { it.habitId in habitIds }
        } else emptyList()
        _allRecordsCache.value = all.groupBy { it.habitId }
    }
}