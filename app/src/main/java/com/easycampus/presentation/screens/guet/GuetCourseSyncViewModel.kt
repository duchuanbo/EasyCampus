package com.easycampus.presentation.screens.guet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycampus.data.repository.SyncProgress
import com.easycampus.domain.model.Course
import com.easycampus.domain.usecase.guet.SyncGuetCoursesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * GUET课程同步ViewModel
 */
@HiltViewModel
class GuetCourseSyncViewModel @Inject constructor(
    private val syncCoursesUseCase: SyncGuetCoursesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<GuetSyncUiState>(GuetSyncUiState.Idle)
    val uiState: StateFlow<GuetSyncUiState> = _uiState.asStateFlow()

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    /**
     * 同步课程
     */
    fun syncCourses(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = GuetSyncUiState.Loading(0, 0)

            val result = syncCoursesUseCase(forceRefresh)

            result.fold(
                onSuccess = { progress ->
                    _uiState.value = GuetSyncUiState.Success(
                        progress.totalCourses,
                        progress.syncedCourses
                    )
                    // 重新获取课程列表
                    loadCourses()
                },
                onFailure = { error ->
                    _uiState.value = GuetSyncUiState.Error(
                        error.message ?: "同步失败"
                    )
                }
            )
        }
    }

    /**
     * 加载课程列表
     */
    private fun loadCourses() {
        viewModelScope.launch {
            // 这里可以从本地数据库加载已同步的课程
            // 暂时留空，等待实现
        }
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _uiState.value = GuetSyncUiState.Idle
    }
}

/**
 * GUET同步UI状态
 */
sealed class GuetSyncUiState {
    object Idle : GuetSyncUiState()
    data class Loading(val current: Int, val total: Int) : GuetSyncUiState()
    data class Success(val totalCourses: Int, val syncedCourses: Int) : GuetSyncUiState()
    data class Error(val message: String) : GuetSyncUiState()
}
