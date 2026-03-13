package com.easycampus.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getTheme(): Flow<AppTheme>
    suspend fun setTheme(theme: AppTheme)
    
    suspend fun getDynamicColorEnabled(): Flow<Boolean>
    suspend fun setDynamicColorEnabled(enabled: Boolean)
    
    suspend fun getNotificationEnabled(): Flow<Boolean>
    suspend fun setNotificationEnabled(enabled: Boolean)
    
    suspend fun getCheckInReminderMinutes(): Flow<Int>
    suspend fun setCheckInReminderMinutes(minutes: Int)
    
    suspend fun getCourseReminderEnabled(): Flow<Boolean>
    suspend fun setCourseReminderEnabled(enabled: Boolean)
    
    suspend fun getCourseReminderMinutes(): Flow<Int>
    suspend fun setCourseReminderMinutes(minutes: Int)
    
    suspend fun getAutoCheckInEnabled(): Flow<Boolean>
    suspend fun setAutoCheckInEnabled(enabled: Boolean)
    
    suspend fun getAutoCheckInTypes(): Flow<List<String>>
    suspend fun setAutoCheckInTypes(types: List<String>)
    
    suspend fun clearAllSettings()
}

enum class AppTheme {
    LIGHT,
    DARK,
    SYSTEM
}
