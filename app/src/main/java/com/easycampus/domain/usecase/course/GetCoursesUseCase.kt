package com.easycampus.domain.usecase.course

import com.easycampus.domain.model.Course
import com.easycampus.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import javax.inject.Inject

/**
 * 获取课程列表用例
 */
class GetCoursesUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    operator fun invoke(): Flow<List<Course>> {
        return courseRepository.getCourses()
    }
    
    fun getCoursesByDay(dayOfWeek: DayOfWeek): Flow<List<Course>> {
        return courseRepository.getCoursesByDay(dayOfWeek)
    }
    
    fun getCoursesForCurrentWeek(currentWeek: Int): Flow<List<Course>> {
        return courseRepository.getCoursesForCurrentWeek(currentWeek)
    }
    
    fun getCourseById(courseId: String): Flow<Course?> {
        return courseRepository.getCourseById(courseId)
    }
}
