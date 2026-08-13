package app.tabit.tracker.feature.charts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tabit.tracker.core.db.*
import app.tabit.tracker.core.utils.ScoringEngine
import app.tabit.tracker.core.utils.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ChartsState(
    val habits: List<HabitEntity> = emptyList(),
    val selectedHabitId: Long? = null,
    val scores: Map<Long, Float> = emptyMap(),
    val streaks: Map<Long, Int> = emptyMap(),
    val bestStreaks: Map<Long, Int> = emptyMap(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val habitDao: HabitDao
) : ViewModel() {
    private val _state = MutableStateFlow(ChartsState())
    val state: StateFlow<ChartsState> = _state.asStateFlow()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _allRecordsCache = MutableStateFlow<Map<Long, List<RecordEntity>>>(emptyMap())

    init {
        viewModelScope.launch { refreshCache() }

        viewModelScope.launch {
            val today = LocalDate.now().format(formatter)
            val yearStart = LocalDate.now().withDayOfYear(1).format(formatter)
            val yearEnd = LocalDate.now().withDayOfYear(1).plusYears(1).minusDays(1).format(formatter)

            habitDao.getAllActiveHabits().combine(
                habitDao.getRecordsForDateRange(yearStart, yearEnd)
            ) { habits, _ ->
                val scoresMap = mutableMapOf<Long, Float>()
                val streaksMap = mutableMapOf<Long, Int>()
                val bestStreaksMap = mutableMapOf<Long, Int>()
                val recordsByHabit = _allRecordsCache.value

                habits.forEach { habit ->
                    val habitRecords = recordsByHabit[habit.id] ?: emptyList()
                    val streak = StreakCalculator.currentStreak(habitRecords, habit.habitType)
                    val bestStreak = StreakCalculator.bestStreak(habitRecords, habit.habitType)
                    val score = ScoringEngine.calculate(habitRecords, habit, streak)
                    scoresMap[habit.id] = score
                    streaksMap[habit.id] = streak
                    bestStreaksMap[habit.id] = bestStreak
                }
                ChartsState(
                    habits = habits,
                    selectedHabitId = _state.value.selectedHabitId,
                    scores = scoresMap,
                    streaks = streaksMap,
                    bestStreaks = bestStreaksMap,
                    isLoading = false
                )
            }.catch { e ->
                Log.e("ChartsViewModel", "Flow error", e)
                _state.value = _state.value.copy(isLoading = false)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun selectHabit(habitId: Long?) { _state.value = _state.value.copy(selectedHabitId = habitId) }

    private suspend fun refreshCache() {
        val habits = habitDao.getAllHabitsSync()
        val habitIds = habits.map { it.id }
        val all = if (habitIds.isNotEmpty()) {
            habitDao.getAllRecordsSync().filter { it.habitId in habitIds }
        } else emptyList()
        _allRecordsCache.value = all.groupBy { it.habitId }
    }
}