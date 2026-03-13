package com.easycampus.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class Course(
    val id: String,
    val name: String,
    val teacher: String,
    val location: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val startWeek: Int,
    val endWeek: Int,
    val color: Int,
    val platformId: String?,
    val courseCode: String?,
    val credit: Float?,
    val description: String?
)

data class CourseSchedule(
    val courses: List<Course>,
    val currentWeek: Int,
    val totalWeeks: Int,
    val semesterStartDate: Long
)

data class WeeklySchedule(
    val weekNumber: Int,
    val coursesByDay: Map<DayOfWeek, List<Course>>
)

enum class CourseStatus {
    UPCOMING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
