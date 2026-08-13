package app.tabit.tracker.core.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "tabit_theme")

object ThemePreferences {
    private val THEME_KEY = stringPreferencesKey("theme_mode")

    val VALID_THEMES = listOf("light", "dark", "system")

    fun getThemeMode(context: Context): Flow<String> {
        return context.themeDataStore.data.map { preferences ->
            preferences[THEME_KEY] ?: "system"
        }
    }

    suspend fun setThemeMode(context: Context, mode: String) {
        context.themeDataStore.edit { preferences ->
            preferences[THEME_KEY] = if (mode in VALID_THEMES) mode else "system"
        }
    }
}
