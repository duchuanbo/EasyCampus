package com.easycampus.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.easycampus.domain.repository.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Keys
    private val THEME_KEY = stringPreferencesKey("theme")
    private val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
    private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    private val CHECK_IN_REMINDER_MINUTES_KEY = intPreferencesKey("check_in_reminder_minutes")
    private val COURSE_REMINDER_ENABLED_KEY = booleanPreferencesKey("course_reminder_enabled")
    private val COURSE_REMINDER_MINUTES_KEY = intPreferencesKey("course_reminder_minutes")
    private val AUTO_CHECK_IN_ENABLED_KEY = booleanPreferencesKey("auto_check_in_enabled")
    private val AUTO_CHECK_IN_TYPES_KEY = stringSetPreferencesKey("auto_check_in_types")

    // Theme
    val theme: Flow<AppTheme> = dataStore.data.map { preferences ->
        when (preferences[THEME_KEY]) {
            "LIGHT" -> AppTheme.LIGHT
            "DARK" -> AppTheme.DARK
            else -> AppTheme.SYSTEM
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }

    // Dynamic Color
    val dynamicColorEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DYNAMIC_COLOR_KEY] ?: true
    }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_COLOR_KEY] = enabled
        }
    }

    // Notifications
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[NOTIFICATIONS_ENABLED_KEY] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    // Check-in Reminder
    val checkInReminderMinutes: Flow<Int> = dataStore.data.map { preferences ->
        preferences[CHECK_IN_REMINDER_MINUTES_KEY] ?: 5
    }

    suspend fun setCheckInReminderMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[CHECK_IN_REMINDER_MINUTES_KEY] = minutes
        }
    }

    // Course Reminder
    val courseReminderEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[COURSE_REMINDER_ENABLED_KEY] ?: true
    }

    suspend fun setCourseReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[COURSE_REMINDER_ENABLED_KEY] = enabled
        }
    }

    val courseReminderMinutes: Flow<Int> = dataStore.data.map { preferences ->
        preferences[COURSE_REMINDER_MINUTES_KEY] ?: 15
    }

    suspend fun setCourseReminderMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[COURSE_REMINDER_MINUTES_KEY] = minutes
        }
    }

    // Auto Check-in
    val autoCheckInEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_CHECK_IN_ENABLED_KEY] ?: false
    }

    suspend fun setAutoCheckInEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_CHECK_IN_ENABLED_KEY] = enabled
        }
    }

    val autoCheckInTypes: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[AUTO_CHECK_IN_TYPES_KEY] ?: setOf("NORMAL")
    }

    suspend fun setAutoCheckInTypes(types: Set<String>) {
        dataStore.edit { preferences ->
            preferences[AUTO_CHECK_IN_TYPES_KEY] = types
        }
    }

    // Clear all
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
