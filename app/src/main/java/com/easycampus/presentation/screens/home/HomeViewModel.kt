package com.easycampus.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycampus.domain.model.CheckIn
import com.easycampus.domain.usecase.checkin.CheckForNewCheckInsUseCase
import com.easycampus.domain.usecase.checkin.GetCheckInsUseCase
import com.easycampus.domain.usecase.checkin.PerformCheckInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 首页ViewModel
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCheckInsUseCase: GetCheckInsUseCase,
    private val checkForNewCheckInsUseCase: CheckForNewCheckInsUseCase,
    private val performCheckInUseCase: PerformCheckInUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadCheckIns()
    }

    fun loadCheckIns() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            getCheckInsUseCase.getPendingCheckIns().collect { checkIns ->
                _uiState.update { 
                    it.copy(
                        pendingCheckIns = checkIns,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun checkForNewCheckIns() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            checkForNewCheckInsUseCase().collect { result ->
                _uiState.update { state ->
                    when {
                        result.isSuccess -> {
                            state.copy(
                                isRefreshing = false,
                                newCheckInsCount = result.getOrNull()?.size ?: 0
                            )
                        }
                        result.isFailure -> {
                            state.copy(
                                isRefreshing = false,
                                error = result.exceptionOrNull()?.message
                            )
                        }
                        else -> state.copy(isRefreshing = false)
                    }
                }
            }
        }
    }

    fun performCheckIn(checkInId: String, platformType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            performCheckInUseCase(checkInId, platformType).collect { result ->
                _uiState.update { state ->
                    when {
                        result.isSuccess -> {
                            state.copy(
                                isLoading = false,
                                checkInResult = result.getOrNull()
                            )
                        }
                        result.isFailure -> {
                            state.copy(
                                isLoading = false,
                                error = result.exceptionOrNull()?.message
                            )
                        }
                        else -> state.copy(isLoading = false)
                    }
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun dismissCheckInResult() {
        _uiState.update { it.copy(checkInResult = null) }
    }
}

/**
 * 首页UI状态
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val pendingCheckIns: List<CheckIn> = emptyList(),
    val newCheckInsCount: Int = 0,
    val checkInResult: com.easycampus.domain.model.CheckInResult? = null,
    val error: String? = null
)
