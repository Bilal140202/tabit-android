package app.tabit.tracker.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tabit.tracker.core.db.HabitDao
import app.tabit.tracker.core.db.HabitEntity
import app.tabit.tracker.core.theme.ThemePreferences
import app.tabit.tracker.core.utils.ExportService
import app.tabit.tracker.core.utils.ImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

data class SettingsState(
    val isExporting: Boolean = false,
    val exportSuccess: Boolean? = null,
    val lastExportPath: String? = null,
    val themeMode: String = "system",
    val archivedHabits: List<HabitEntity> = emptyList(),
    val importResult: ImportResult? = null,
    val isImporting: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val habitDao: HabitDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        // Observe theme mode
        viewModelScope.launch {
            ThemePreferences.getThemeMode(context).collect { mode ->
                _state.value = _state.value.copy(themeMode = mode)
            }
        }
        // Observe archived habits
        viewModelScope.launch {
            habitDao.getArchivedHabits().collect { archived ->
                _state.value = _state.value.copy(archivedHabits = archived)
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            ThemePreferences.setThemeMode(context, mode)
        }
    }

    fun restoreHabit(id: Long) {
        viewModelScope.launch {
            habitDao.restoreHabit(id)
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true)
            try {
                val file = File(context.getExternalFilesDir(null), "tabit_export.csv")
                file.outputStream().use { ExportService.exportCsv(habitDao, it) }
                _state.value = _state.value.copy(isExporting = false, exportSuccess = true, lastExportPath = file.absolutePath)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isExporting = false, exportSuccess = false)
            }
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isExporting = true)
            try {
                val file = File(context.getExternalFilesDir(null), "tabit_export.json")
                file.outputStream().use { ExportService.exportJson(habitDao, it) }
                _state.value = _state.value.copy(isExporting = false, exportSuccess = true, lastExportPath = file.absolutePath)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isExporting = false, exportSuccess = false)
            }
        }
    }

    fun importJson(file: File) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isImporting = true, importResult = null)
            val result = FileInputStream(file).use { inputStream ->
                ExportService.importJson(habitDao, inputStream)
            }
            _state.value = _state.value.copy(isImporting = false, importResult = result)
        }
    }

    fun dismissImport() {
        _state.value = _state.value.copy(importResult = null)
    }

    fun dismissExport() {
        _state.value = _state.value.copy(exportSuccess = null, lastExportPath = null)
    }
}