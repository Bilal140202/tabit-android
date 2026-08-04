package app.tabit.tracker.feature.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tabit.tracker.core.db.*
import app.tabit.tracker.core.utils.ScoringEngine
import app.tabit.tracker.core.utils.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    init { loadChartsData() }

    private fun loadChartsData() {
        viewModelScope.launch {
            habitDao.getAllActiveHabits().collect { habits ->
                val scoresMap = mutableMapOf<Long, Float>()
                val streaksMap = mutableMapOf<Long, Int>()
                val bestStreaksMap = mutableMapOf<Long, Int>()
                habits.forEach { habit ->
                    val allRecords = habitDao.getAllRecordsForHabit(habit.id).first()
                    val streak = StreakCalculator.currentStreak(allRecords)
                    val bestStreak = StreakCalculator.bestStreak(allRecords)
                    val score = ScoringEngine.calculate(allRecords, habit, streak)
                    scoresMap[habit.id] = score
                    streaksMap[habit.id] = streak
                    bestStreaksMap[habit.id] = bestStreak
                }
                _state.value = ChartsState(habits = habits, scores = scoresMap, streaks = streaksMap, bestStreaks = bestStreaksMap, isLoading = false)
            }
        }
    }

    fun selectHabit(habitId: Long?) { _state.value = _state.value.copy(selectedHabitId = habitId) }
}
