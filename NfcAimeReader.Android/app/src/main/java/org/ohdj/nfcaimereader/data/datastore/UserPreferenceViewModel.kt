package org.ohdj.nfcaimereader.data.datastore

import android.app.Application
import android.os.Build
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ohdj.nfcaimereader.ThemeMode
import org.ohdj.nfcaimereader.ui.screen.setting.SettingsUiState

private val Application.dataStore by preferencesDataStore(name = "settings")

class UserPreferenceViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val DYNAMIC_COLOR_ENABLED = booleanPreferencesKey("dynamic_color_enabled")
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val themeModeString = preferences[THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(themeModeString)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    val dynamicColorEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_ENABLED] ?: false
    }

    val settingsUiState: Flow<SettingsUiState> = combine(
        themeMode,
        dynamicColorEnabled
    ) { themeMode, dynamicColorEnabled ->
        SettingsUiState(
            themeMode = themeMode,
            dynamicColorEnabled = dynamicColorEnabled,
            supportsDynamicTheming = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        )
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[THEME_MODE] = mode.name
            }
        }
    }

    fun updateDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[DYNAMIC_COLOR_ENABLED] = enabled
            }
        }
    }
}