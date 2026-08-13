package app.tabit.tracker.feature.today

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tabit.tracker.core.db.*
import app.tabit.tracker.core.utils.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TodayState(
    val habits: List<HabitEntity> = emptyList(),
    val todayRecords: List<RecordEntity> = emptyList(),
    val streaks: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val habitDao: HabitDao
) : ViewModel() {
    private val _state = MutableStateFlow(TodayState())
    val state: StateFlow<TodayState> = _state.asStateFlow()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _allRecordsCache = MutableStateFlow<Map<Long, List<RecordEntity>>>(emptyMap())

    init {
        viewModelScope.launch { refreshCache() }

        viewModelScope.launch {
            val today = LocalDate.now().format(formatter)
            habitDao.getAllActiveHabits().combine(
                habitDao.getRecordsForDateRange(today, today)
            ) { habits, records ->
                habits to records
            }.combine(_allRecordsCache) { (habits, records), allRecordsByHabit ->
                val streaksMap = mutableMapOf<Long, Int>()
                habits.forEach { habit ->
                    val habitAllRecords = allRecordsByHabit[habit.id] ?: emptyList()
                    streaksMap[habit.id] = StreakCalculator.currentStreak(habitAllRecords, habit.habitType)
                }
                TodayState(
                    habits = habits,
                    todayRecords = records,
                    streaks = streaksMap,
                    isLoading = false
                )
            }.catch { e ->
                Log.e("TodayViewModel", "Flow error", e)
                _state.value = _state.value.copy(isLoading = false)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    /**
     * 3-state toggle: none → done → skip → none
     */
    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            val today = LocalDate.now().format(formatter)
            val existing = habitDao.getRecordForHabitAndDate(habitId, today)
            val currentStatus = existing?.status ?: "none"

            val (newDone, newValue, newStatus) = when (currentStatus) {
                "none" -> Triple(true, 1, "done")
                "done" -> Triple(false, 0, "skip")
                "skip" -> Triple(false, 0, "none")
                else -> Triple(true, 1, "done")
            }

            habitDao.toggleRecordWithStatus(habitId, today, newDone, newValue, newStatus)
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