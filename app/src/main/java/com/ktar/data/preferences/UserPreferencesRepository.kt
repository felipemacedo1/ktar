package com.ktar.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;
    
    companion object {
        fun fromString(value: String): ThemeMode {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                SYSTEM
            }
        }
    }
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val terminalFontSize: Float = 16f,
    val commandHistoryEnabled: Boolean = true,
    val commandHistorySize: Int = 100,
    val hapticFeedbackEnabled: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {
    
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val TERMINAL_FONT_SIZE_KEY = stringPreferencesKey("terminal_font_size")
        private val COMMAND_HISTORY_ENABLED_KEY = booleanPreferencesKey("command_history_enabled")
        private val COMMAND_HISTORY_SIZE_KEY = stringPreferencesKey("command_history_size")
        private val HAPTIC_FEEDBACK_KEY = booleanPreferencesKey("haptic_feedback_enabled")
        
        @Volatile
        private var instance: UserPreferencesRepository? = null
        
        fun getInstance(context: Context): UserPreferencesRepository {
            return instance ?: synchronized(this) {
                instance ?: UserPreferencesRepository(context.applicationContext).also { instance = it }
            }
        }
    }
    
    val userPreferencesFlow: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { preferences ->
        UserPreferences(
            themeMode = ThemeMode.fromString(preferences[THEME_MODE_KEY] ?: "SYSTEM"),
            terminalFontSize = preferences[TERMINAL_FONT_SIZE_KEY]?.toFloatOrNull() ?: 16f,
            commandHistoryEnabled = preferences[COMMAND_HISTORY_ENABLED_KEY] ?: true,
            commandHistorySize = preferences[COMMAND_HISTORY_SIZE_KEY]?.toIntOrNull() ?: 100,
            hapticFeedbackEnabled = preferences[HAPTIC_FEEDBACK_KEY] ?: true
        )
    }
    
    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }
    
    suspend fun updateTerminalFontSize(fontSize: Float) {
        val validSize = fontSize.coerceIn(12f, 24f)
        context.userPreferencesDataStore.edit { preferences ->
            preferences[TERMINAL_FONT_SIZE_KEY] = validSize.toString()
        }
    }
    
    suspend fun updateCommandHistoryEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[COMMAND_HISTORY_ENABLED_KEY] = enabled
        }
    }
    
    suspend fun updateCommandHistorySize(size: Int) {
        val validSize = size.coerceIn(10, 500)
        context.userPreferencesDataStore.edit { preferences ->
            preferences[COMMAND_HISTORY_SIZE_KEY] = validSize.toString()
        }
    }
    
    suspend fun updateHapticFeedbackEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_KEY] = enabled
        }
    }
}
