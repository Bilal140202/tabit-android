package app.tabit.tracker.feature.today

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
                val streaksMap = mutableMapOf<Long, Int>()
                habits.forEach { habit ->
                    val allRecords = habitDao.getAllRecordsForHabit(habit.id).first()
                    streaksMap[habit.id] = StreakCalculator.currentStreak(allRecords)
                }
                TodayState(
                    habits = habits,
                    todayRecords = records,
                    streaks = streaksMap,
                    isLoading = false
                )
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
            // Insert if no record exists (IGNORE if one already does)
            habitDao.insertRecordIgnore(RecordEntity(habitId = habitId, date = today, done = newDone, value = newValue))
            // Then directly update by habitId+date — works whether record was just inserted or already existed
            habitDao.updateRecordDoneByHabitAndDate(habitId, today, newDone, newValue)
        }
    }
}
