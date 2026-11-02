package com.example.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mm_tools_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FIRST_LAUNCH_COMPLETED = "key_first_launch_completed"
        private const val KEY_DESKTOP_MODE_ENABLED = "key_desktop_mode_enabled"
        private const val KEY_SAVED_AUTOFILL_PASSWORD = "key_saved_autofill_password"
        private const val KEY_RECENT_FIRST_NAMES = "key_recent_first_names"
        private const val KEY_RECENT_LAST_NAMES = "key_recent_last_names"
        const val DEFAULT_AUTOFILL_PASSWORD = "MmTools@789#"
    }

    var isFirstLaunchCompleted: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH_COMPLETED, value).apply()

    var isDesktopModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DESKTOP_MODE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_DESKTOP_MODE_ENABLED, value).apply()

    /**
     * Permanent saved password for Smart Auto-Fill.
     * Survives Clear Data and app restarts; only changes when the user edits it.
     */
    var savedAutoFillPassword: String
        get() = prefs.getString(KEY_SAVED_AUTOFILL_PASSWORD, DEFAULT_AUTOFILL_PASSWORD)
            ?.takeIf { it.isNotBlank() } ?: DEFAULT_AUTOFILL_PASSWORD
        set(value) = prefs.edit().putString(KEY_SAVED_AUTOFILL_PASSWORD, value.trim()).apply()

    fun getRecentFirstNames(): List<String> {
        val raw = prefs.getString(KEY_RECENT_FIRST_NAMES, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
    }

    fun getRecentLastNames(): List<String> {
        val raw = prefs.getString(KEY_RECENT_LAST_NAMES, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|").filter { it.isNotBlank() }
    }

    fun recordUsedName(firstName: String, lastName: String) {
        val updatedFirsts = (getRecentFirstNames() + firstName).takeLast(120)
        val updatedLasts = (getRecentLastNames() + lastName).takeLast(120)
        prefs.edit()
            .putString(KEY_RECENT_FIRST_NAMES, updatedFirsts.joinToString("|"))
            .putString(KEY_RECENT_LAST_NAMES, updatedLasts.joinToString("|"))
            .apply()
    }
}
