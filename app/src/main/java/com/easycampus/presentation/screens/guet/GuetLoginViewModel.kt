package com.easycampus.presentation.screens.guet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycampus.domain.model.User
import com.easycampus.domain.usecase.guet.GuetLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * GUET登录ViewModel
 */
@HiltViewModel
class GuetLoginViewModel @Inject constructor(
    private val loginUseCase: GuetLoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuetLoginUiState>(GuetLoginUiState.Idle)
    val uiState: StateFlow<GuetLoginUiState> = _uiState.asStateFlow()

    /**
     * 执行登录
     */
    fun login(studentId: String, password: String) {
        viewModelScope.launch {
            _uiState.value = GuetLoginUiState.Loading

            val result = loginUseCase(studentId, password)

            _uiState.value = result.fold(
                onSuccess = { user ->
                    GuetLoginUiState.Success(user)
                },
                onFailure = { error ->
                    GuetLoginUiState.Error(error.message ?: "登录失败")
                }
            )
        }
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _uiState.value = GuetLoginUiState.Idle
    }
}

/**
 * GUET登录UI状态
 */
sealed class GuetLoginUiState {
    object Idle : GuetLoginUiState()
    object Loading : GuetLoginUiState()
    data class Success(val user: User) : GuetLoginUiState()
    data class Error(val message: String) : GuetLoginUiState()
}
