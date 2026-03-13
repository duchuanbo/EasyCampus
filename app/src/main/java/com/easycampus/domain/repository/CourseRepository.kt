package com.easycampus.domain.repository

import com.easycampus.domain.model.Course
import com.easycampus.domain.model.CourseSchedule
import com.easycampus.domain.model.WeeklySchedule
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate

interface CourseRepository {
    fun getCourses(): Flow<List<Course>>
    fun getCourseById(courseId: String): Flow<Course?>
    fun getCoursesByDay(dayOfWeek: DayOfWeek): Flow<List<Course>>
    fun getCoursesForCurrentWeek(currentWeek: Int): Flow<List<Course>>
    suspend fun getCoursesByWeek(weekNumber: Int): Flow<WeeklySchedule>
    suspend fun getCurrentWeekCourses(): Flow<WeeklySchedule>
    
    suspend fun saveCourse(course: Course)
    suspend fun addCourse(course: Course): Result<Unit>
    suspend fun updateCourse(course: Course): Result<Unit>
    suspend fun deleteCourse(courseId: String): Result<Unit>
    
    suspend fun importFromPlatform(platformId: String): Result<List<Course>>
    suspend fun importFromFile(filePath: String): Result<List<Course>>
    
    suspend fun getCurrentWeek(): Int
    suspend fun getTotalWeeks(): Int
    suspend fun setSemesterInfo(startDate: LocalDate, totalWeeks: Int): Result<Unit>
    
    suspend fun refreshCourses(): Result<Unit>
    suspend fun syncWithPlatform(platformId: String): Result<Unit>
}
