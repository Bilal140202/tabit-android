package app.tabit.tracker.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tabit.tracker.core.db.HabitDao
import app.tabit.tracker.core.utils.ExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class SettingsState(
    val isExporting: Boolean = false,
    val exportSuccess: Boolean? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val habitDao: HabitDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun exportCsv() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true)
            try {
                val file = File(context.cacheDir, "tabit_export.csv")
                file.outputStream().use { ExportService.exportCsv(habitDao, it) }
                _state.value = _state.value.copy(isExporting = false, exportSuccess = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isExporting = false, exportSuccess = false)
            }
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true)
            try {
                val file = File(context.cacheDir, "tabit_export.json")
                file.outputStream().use { ExportService.exportJson(habitDao, it) }
                _state.value = _state.value.copy(isExporting = false, exportSuccess = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isExporting = false, exportSuccess = false)
            }
        }
    }
}
