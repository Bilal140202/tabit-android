package app.tabit.tracker.feature.charts

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

    init {
        // FIX: Combine habits AND all-records Flows so record changes
        // (toggles, notes) trigger re-calculation of scores/streaks.
        // Previously only habits table was observed.
        viewModelScope.launch {
            val today = LocalDate.now().format(formatter)
            // Observe a wide date range (all records this year) to keep charts responsive
            val yearStart = LocalDate.now().withDayOfYear(1).format(formatter)
            val yearEnd = LocalDate.now().withDayOfYear(1).plusYears(1).minusDays(1).format(formatter)

            habitDao.getAllActiveHabits().combine(
                habitDao.getRecordsForDateRange(yearStart, yearEnd)
            ) { habits, recentRecords ->
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
                ChartsState(
                    habits = habits,
                    scores = scoresMap,
                    streaks = streaksMap,
                    bestStreaks = bestStreaksMap,
                    isLoading = false
                )
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    fun selectHabit(habitId: Long?) { _state.value = _state.value.copy(selectedHabitId = habitId) }
}
