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

    init {
        viewModelScope.launch {
            val today = LocalDate.now().format(formatter)
            habitDao.getAllActiveHabits().combine(
                habitDao.getRecordsForDateRange(today, today)
            ) { habits, records ->
                // Bulk fetch all records for streak calculation (fixes N+1)
                val habitIds = habits.map { it.id }
                val allRecords = if (habitIds.isNotEmpty()) {
                    habitDao.getAllRecordsSync().filter { it.habitId in habitIds }
                } else emptyList()
                val allRecordsByHabit = allRecords.groupBy { it.habitId }

                val streaksMap = mutableMapOf<Long, Int>()
                habits.forEach { habit ->
                    val habitAllRecords = allRecordsByHabit[habit.id] ?: emptyList()
                    streaksMap[habit.id] = StreakCalculator.currentStreak(habitAllRecords)
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

    // FIX: Race-condition-proof toggle using insertIgnore + direct update.
    // Previously used .first() on a Flow which could return stale data,
    // and OnConflictStrategy.REPLACE could silently overwrite existing records
    // losing done/value fields (the "random numbers on home screen" bug).
    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            val today = LocalDate.now().format(formatter)
            val existing = state.value.todayRecords.find { it.habitId == habitId }
            val newDone = existing?.done != true
            val newValue = if (newDone) 1 else 0
            habitDao.toggleRecord(habitId, today, newDone, newValue)
        }
    }
}
