package com.example.util

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mm_tools_preferences", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_FIRST_LAUNCH_COMPLETED = "key_first_launch_completed"
        private const val KEY_DESKTOP_MODE_ENABLED = "key_desktop_mode_enabled"
    }

    var isFirstLaunchCompleted: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH_COMPLETED, value).apply()

    var isDesktopModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DESKTOP_MODE_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_DESKTOP_MODE_ENABLED, value).apply()
}
