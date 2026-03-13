package com.easycampus.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycampus.domain.repository.AppTheme
import com.easycampus.domain.repository.SettingsRepository
import com.easycampus.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.getTheme(),
                settingsRepository.getDynamicColorEnabled(),
                settingsRepository.getNotificationEnabled(),
                settingsRepository.getCheckInReminderMinutes(),
                settingsRepository.getCourseReminderEnabled(),
                settingsRepository.getCourseReminderMinutes(),
                settingsRepository.getAutoCheckInEnabled(),
                userRepository.getAccounts()
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                SettingsUiState(
                    themeMode = values[0] as AppTheme,
                    dynamicColorEnabled = values[1] as Boolean,
                    notificationsEnabled = values[2] as Boolean,
                    checkInReminderMinutes = values[3] as Int,
                    courseReminderEnabled = values[4] as Boolean,
                    courseReminderMinutes = values[5] as Int,
                    autoCheckInEnabled = values[6] as Boolean,
                    accountCount = (values[7] as List<*>).size
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsRepository.setTheme(theme)
        }
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDynamicColorEnabled(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationEnabled(enabled)
        }
    }

    fun setCheckInReminderMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setCheckInReminderMinutes(minutes)
        }
    }

    fun setCourseReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCourseReminderEnabled(enabled)
        }
    }

    fun setCourseReminderMinutes(minutes: Int) {
        viewModelScope.launch {
            settingsRepository.setCourseReminderMinutes(minutes)
        }
    }

    fun setAutoCheckInEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoCheckInEnabled(enabled)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            settingsRepository.clearAllSettings()
            // Clear all accounts and data
        }
    }
}

data class SettingsUiState(
    val themeMode: AppTheme = AppTheme.SYSTEM,
    val dynamicColorEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val checkInReminderMinutes: Int = 5,
    val courseReminderEnabled: Boolean = true,
    val courseReminderMinutes: Int = 15,
    val autoCheckInEnabled: Boolean = false,
    val accountCount: Int = 0,
    val appVersion: String = "1.0.0"
)
