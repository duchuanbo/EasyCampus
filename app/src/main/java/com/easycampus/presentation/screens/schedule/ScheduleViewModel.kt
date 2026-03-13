package com.easycampus.presentation.screens.schedule

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycampus.domain.repository.CourseRepository
import com.easycampus.presentation.theme.CourseColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                courseRepository.getCurrentWeekCourses().collect { weeklySchedule ->
                    val coursesByDay = weeklySchedule.coursesByDay.mapValues { (_, courses) ->
                        courses.mapIndexed { index, course ->
                            CourseUiModel(
                                id = course.id,
                                name = course.name,
                                teacher = course.teacher,
                                location = course.location,
                                startTime = course.startTime.format(timeFormatter),
                                endTime = course.endTime.format(timeFormatter),
                                color = CourseColors[index % CourseColors.size],
                                dayOfWeek = course.dayOfWeek
                            )
                        }
                    }

                    _uiState.update {
                        it.copy(
                            currentWeek = weeklySchedule.weekNumber,
                            coursesByDay = coursesByDay,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun selectWeek(week: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                courseRepository.getCoursesByWeek(week).collect { weeklySchedule ->
                    val coursesByDay = weeklySchedule.coursesByDay.mapValues { (_, courses) ->
                        courses.mapIndexed { index, course ->
                            CourseUiModel(
                                id = course.id,
                                name = course.name,
                                teacher = course.teacher,
                                location = course.location,
                                startTime = course.startTime.format(timeFormatter),
                                endTime = course.endTime.format(timeFormatter),
                                color = CourseColors[index % CourseColors.size],
                                dayOfWeek = course.dayOfWeek
                            )
                        }
                    }

                    _uiState.update {
                        it.copy(
                            currentWeek = week,
                            coursesByDay = coursesByDay,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }

    fun refreshSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            courseRepository.refreshCourses()
                .onSuccess {
                    loadSchedule()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class ScheduleUiState(
    val currentWeek: Int = 1,
    val totalWeeks: Int = 20,
    val coursesByDay: Map<DayOfWeek, List<CourseUiModel>> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
