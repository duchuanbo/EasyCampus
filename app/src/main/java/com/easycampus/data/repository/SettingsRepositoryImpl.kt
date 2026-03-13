package com.easycampus.data.repository

import com.easycampus.data.local.datastore.SettingsDataStore
import com.easycampus.domain.repository.AppTheme
import com.easycampus.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override suspend fun getTheme(): Flow<AppTheme> = settingsDataStore.theme

    override suspend fun setTheme(theme: AppTheme) {
        settingsDataStore.setTheme(theme)
    }

    override suspend fun getDynamicColorEnabled(): Flow<Boolean> = settingsDataStore.dynamicColorEnabled

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        settingsDataStore.setDynamicColorEnabled(enabled)
    }

    override suspend fun getNotificationEnabled(): Flow<Boolean> = settingsDataStore.notificationsEnabled

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        settingsDataStore.setNotificationsEnabled(enabled)
    }

    override suspend fun getCheckInReminderMinutes(): Flow<Int> = settingsDataStore.checkInReminderMinutes

    override suspend fun setCheckInReminderMinutes(minutes: Int) {
        settingsDataStore.setCheckInReminderMinutes(minutes)
    }

    override suspend fun getCourseReminderEnabled(): Flow<Boolean> = settingsDataStore.courseReminderEnabled

    override suspend fun setCourseReminderEnabled(enabled: Boolean) {
        settingsDataStore.setCourseReminderEnabled(enabled)
    }

    override suspend fun getCourseReminderMinutes(): Flow<Int> = settingsDataStore.courseReminderMinutes

    override suspend fun setCourseReminderMinutes(minutes: Int) {
        settingsDataStore.setCourseReminderMinutes(minutes)
    }

    override suspend fun getAutoCheckInEnabled(): Flow<Boolean> = settingsDataStore.autoCheckInEnabled

    override suspend fun setAutoCheckInEnabled(enabled: Boolean) {
        settingsDataStore.setAutoCheckInEnabled(enabled)
    }

    override suspend fun getAutoCheckInTypes(): Flow<List<String>> = 
        settingsDataStore.autoCheckInTypes.map { it.toList() }

    override suspend fun setAutoCheckInTypes(types: List<String>) {
        settingsDataStore.setAutoCheckInTypes(types.toSet())
    }

    override suspend fun clearAllSettings() {
        settingsDataStore.clearAll()
    }
}
