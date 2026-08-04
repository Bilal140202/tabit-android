package app.tabit.tracker.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tabit.tracker.core.db.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class TodayState(
    val habits: List<HabitEntity> = emptyList(),
    val todayRecords: List<RecordEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val habitDao: HabitDao
) : ViewModel() {
    private val _state = MutableStateFlow(TodayState())
    val state: StateFlow<TodayState> = _state.asStateFlow()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    init { loadTodayData() }

    private fun loadTodayData() {
        viewModelScope.launch {
            val today = LocalDate.now().format(formatter)
            habitDao.getAllActiveHabits().collect { habits ->
                val records = habitDao.getRecordsForDate(today)
                _state.value = TodayState(habits = habits, todayRecords = records, isLoading = false)
            }
        }
    }

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            val today = LocalDate.now().format(formatter)
            val existing = habitDao.getRecordsForHabit(habitId, today, today).first()
            if (existing.isEmpty()) {
                habitDao.insertRecord(RecordEntity(habitId = habitId, date = today, done = true, value = 1))
            } else {
                val record = existing.first()
                habitDao.updateRecord(record.copy(done = !record.done, value = if (!record.done) 1 else 0))
            }
        }
    }
}
