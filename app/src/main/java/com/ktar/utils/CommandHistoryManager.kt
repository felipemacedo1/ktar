package com.ktar.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.commandHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "command_history")

/**
 * Manages command history with persistence and navigation.
 * Supports up/down arrow key navigation and search.
 */
class CommandHistoryManager(private val context: Context) {
    
    private val history = mutableListOf<String>()
    private var currentIndex = -1
    private var maxHistorySize = MAX_HISTORY_SIZE
    
    companion object {
        private const val MAX_HISTORY_SIZE = 100
        private val HISTORY_KEY = stringPreferencesKey("command_history")
        
        @Volatile
        private var instance: CommandHistoryManager? = null
        
        fun getInstance(context: Context): CommandHistoryManager {
            return instance ?: synchronized(this) {
                instance ?: CommandHistoryManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    /**
     * Load history from persistent storage.
     */
    suspend fun loadHistory() {
        context.commandHistoryDataStore.data
            .map { preferences ->
                preferences[HISTORY_KEY] ?: "[]"
            }
            .collect { jsonString ->
                try {
                    val jsonArray = JSONArray(jsonString)
                    history.clear()
                    for (i in 0 until jsonArray.length()) {
                        history.add(jsonArray.getString(i))
                    }
                    currentIndex = history.size
                } catch (e: Exception) {
                    Logger.e("CommandHistory", "Error loading history", e)
                }
            }
    }
    
    /**
     * Save history to persistent storage.
     */
    private suspend fun saveHistory() {
        val jsonArray = JSONArray()
        history.forEach { jsonArray.put(it) }
        
        context.commandHistoryDataStore.edit { preferences ->
            preferences[HISTORY_KEY] = jsonArray.toString()
        }
    }
    
    /**
     * Add a command to history.
     * Duplicates are moved to the end rather than added again.
     */
    suspend fun addCommand(command: String) {
        val trimmed = command.trim()
        
        // Don't add empty commands
        if (trimmed.isBlank()) return
        
        // Remove duplicate if exists
        history.remove(trimmed)
        
        // Add to end
        history.add(trimmed)
        
        // Limit size
        while (history.size > maxHistorySize) {
            history.removeAt(0)
        }
        
        // Reset navigation index
        currentIndex = history.size
        
        // Persist
        saveHistory()
    }
    
    /**
     * Navigate to previous command (Up arrow).
     * Returns null if at the beginning of history.
     */
    fun navigateUp(): String? {
        return if (history.isEmpty()) {
            null
        } else if (currentIndex > 0) {
            currentIndex--
            history[currentIndex]
        } else {
            // Already at the oldest command
            history.firstOrNull()
        }
    }
    
    /**
     * Navigate to next command (Down arrow).
     * Returns empty string if at the end of history.
     */
    fun navigateDown(): String {
        return if (history.isEmpty()) {
            ""
        } else if (currentIndex < history.size - 1) {
            currentIndex++
            history[currentIndex]
        } else {
            // Move past end of history
            currentIndex = history.size
            ""
        }
    }
    
    /**
     * Reset navigation to the end of history.
     */
    fun resetNavigation() {
        currentIndex = history.size
    }
    
    /**
     * Search history for commands containing the query.
     * Returns list of matching commands in reverse chronological order.
     */
    fun search(query: String): List<String> {
        if (query.isBlank()) return emptyList()
        
        return history
            .filter { it.contains(query, ignoreCase = true) }
            .reversed()
    }
    
    /**
     * Get all history items in reverse chronological order.
     */
    fun getHistory(): List<String> {
        return history.reversed()
    }
    
    /**
     * Clear all history.
     */
    suspend fun clear() {
        history.clear()
        currentIndex = -1
        saveHistory()
    }
    
    /**
     * Get current history size.
     */
    fun size(): Int = history.size
    
    /**
     * Check if history is empty.
     */
    fun isEmpty(): Boolean = history.isEmpty()
    
    /**
     * Update max history size.
     */
    fun setMaxSize(size: Int) {
        maxHistorySize = size.coerceIn(10, 500)
        
        // Trim if necessary
        while (history.size > maxHistorySize) {
            history.removeAt(0)
        }
    }
}
