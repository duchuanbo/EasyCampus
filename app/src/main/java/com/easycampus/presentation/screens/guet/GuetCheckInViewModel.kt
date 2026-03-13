package com.easycampus.presentation.screens.guet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycampus.domain.model.CheckInResult
import com.easycampus.domain.model.CheckInType
import com.easycampus.domain.usecase.guet.GuetCheckInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * GUET签到ViewModel
 */
@HiltViewModel
class GuetCheckInViewModel @Inject constructor(
    private val checkInUseCase: GuetCheckInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuetCheckInUiState>(GuetCheckInUiState.Idle)
    val uiState: StateFlow<GuetCheckInUiState> = _uiState.asStateFlow()

    /**
     * 执行签到
     */
    fun performCheckIn(
        checkInId: String,
        type: CheckInType,
        code: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            _uiState.value = GuetCheckInUiState.Loading

            val result = checkInUseCase(checkInId, type, code, latitude, longitude)

            _uiState.value = result.fold(
                onSuccess = { checkInResult ->
                    GuetCheckInUiState.Success(checkInResult)
                },
                onFailure = { error ->
                    GuetCheckInUiState.Error(error.message ?: "签到失败")
                }
            )
        }
    }

    /**
     * 普通签到
     */
    fun normalCheckIn(checkInId: String) {
        performCheckIn(checkInId, CheckInType.NORMAL)
    }

    /**
     * 签到码签到
     */
    fun codeCheckIn(checkInId: String, code: String) {
        performCheckIn(checkInId, CheckInType.CODE, code = code)
    }

    /**
     * 手势签到
     */
    fun gestureCheckIn(checkInId: String, gesture: String) {
        performCheckIn(checkInId, CheckInType.GESTURE, code = gesture)
    }

    /**
     * 二维码签到
     */
    fun qrCodeCheckIn(checkInId: String, qrContent: String) {
        performCheckIn(checkInId, CheckInType.QR_CODE, code = qrContent)
    }

    /**
     * 位置签到
     */
    fun locationCheckIn(
        checkInId: String,
        latitude: Double,
        longitude: Double
    ) {
        performCheckIn(
            checkInId,
            CheckInType.LOCATION,
            latitude = latitude,
            longitude = longitude
        )
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _uiState.value = GuetCheckInUiState.Idle
    }
}

/**
 * GUET签到UI状态
 */
sealed class GuetCheckInUiState {
    object Idle : GuetCheckInUiState()
    object Loading : GuetCheckInUiState()
    data class Success(val result: CheckInResult) : GuetCheckInUiState()
    data class Error(val message: String) : GuetCheckInUiState()
}
