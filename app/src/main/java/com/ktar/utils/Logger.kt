package com.ktar.utils

import android.util.Log

/**
 * Centralized logging utility with support for debug and release builds.
 * Includes log injection prevention and sensitive data filtering.
 */
object Logger {
    
    private const val TAG = Constants.LOG_TAG
    
    // Simple flag to control debug logging (set to false for release)
    private const val DEBUG_ENABLED = true
    
    // Sensitive keywords that should trigger command redaction
    private val SENSITIVE_KEYWORDS = listOf(
        "password", "passwd", "pwd", "secret", "key", "token", 
        "api_key", "apikey", "private", "credential"
    )
    
    /**
     * Sanitizes log input to prevent log injection attacks (CWE-117).
     * Removes control characters and limits message size.
     */
    private fun sanitizeLogInput(message: String): String {
        // Remove control characters and newlines that could forge log entries
        return message
            .replace(Regex("[\\r\\n\\t\\u0000-\\u001F]"), "_")
            .take(2000)  // Limit message size to prevent log flooding
    }
    
    /**
     * Checks if a command contains sensitive information that should be redacted.
     */
    private fun isSensitiveCommand(command: String): Boolean {
        return SENSITIVE_KEYWORDS.any { command.lowercase().contains(it) }
    }
    
    /**
     * Log debug message (only in debug builds).
     */
    fun d(tag: String = TAG, message: String) {
        if (DEBUG_ENABLED) {
            Log.d(tag, sanitizeLogInput(message))
        }
    }
    
    /**
     * Log info message.
     */
    fun i(tag: String = TAG, message: String) {
        Log.i(tag, sanitizeLogInput(message))
    }
    
    /**
     * Log warning message.
     */
    fun w(tag: String = TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(tag, sanitizeLogInput(message), throwable)
        } else {
            Log.w(tag, sanitizeLogInput(message))
        }
    }
    
    /**
     * Log error message.
     */
    fun e(tag: String = TAG, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, sanitizeLogInput(message), throwable)
        } else {
            Log.e(tag, sanitizeLogInput(message))
        }
    }
    
    /**
     * Log connection event with sanitized username.
     */
    fun logConnection(host: String, port: Int, username: String, success: Boolean) {
        val status = if (success) "SUCCESS" else "FAILED"
        i("SSH_CONNECTION", "Connection to ${sanitizeLogInput(username)}@$host:$port - $status")
    }
    
    /**
     * Log command execution with sensitive command redaction.
     */
    fun logCommand(command: String, exitCode: Int, duration: Long) {
        val sanitized = if (isSensitiveCommand(command)) {
            "[REDACTED COMMAND]"
        } else {
            sanitizeLogInput(command)
        }
        d("SSH_COMMAND", "Executed '$sanitized' (exit: $exitCode, duration: ${duration}ms)")
    }
    
    /**
     * Log security event.
     */
    fun logSecurity(event: String, details: String? = null) {
        i("SECURITY", "Event: ${sanitizeLogInput(event)} ${details?.let { "- ${sanitizeLogInput(it)}" } ?: ""}")
    }
}
