package app.tabit.tracker.feature.habitform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tabit.tracker.core.db.HabitDao
import app.tabit.tracker.core.db.HabitEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitFormState(
    val name: String = "",
    val color: Long = 0xFF2196F3,
    val target: Int = 1,
    val weight: Float = 1f,
    val frequency: String = "daily",
    val reminderHour: Int = -1,
    val reminderMinute: Int = -1,
    val note: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class HabitFormViewModel @Inject constructor(
    private val habitDao: HabitDao
) : ViewModel() {
    private val _state = MutableStateFlow(HabitFormState())
    val state: StateFlow<HabitFormState> = _state.asStateFlow()

    fun loadHabit(habitId: Long) {
        viewModelScope.launch {
            val habit = habitDao.getHabitById(habitId)
            habit?.let {
                _state.value = _state.value.copy(
                    name = it.name, color = it.color, target = it.target, weight = it.weight,
                    frequency = it.frequency, reminderHour = it.reminderHour, reminderMinute = it.reminderMinute,
                    note = it.note, isEditing = true
                )
            }
        }
    }

    fun updateName(name: String) { _state.value = _state.value.copy(name = name) }
    fun updateColor(color: Long) { _state.value = _state.value.copy(color = color) }
    fun updateTarget(target: Int) { _state.value = _state.value.copy(target = target) }
    fun updateFrequency(frequency: String) { _state.value = _state.value.copy(frequency = frequency) }
    fun updateNote(note: String) { _state.value = _state.value.copy(note = note) }

    fun saveHabit(habitId: Long = 0) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            val s = _state.value
            if (s.isEditing) {
                habitDao.updateHabit(HabitEntity(id = habitId, name = s.name, color = s.color, target = s.target, weight = s.weight, frequency = s.frequency, reminderHour = s.reminderHour, reminderMinute = s.reminderMinute, note = s.note))
            } else {
                habitDao.insertHabit(HabitEntity(name = s.name, color = s.color, target = s.target, weight = s.weight, frequency = s.frequency, reminderHour = s.reminderHour, reminderMinute = s.reminderMinute, note = s.note))
            }
            _state.value = _state.value.copy(isSaving = false)
        }
    }
}
