package com.easycampus.domain.usecase.course

import com.easycampus.data.remote.platform.PlatformAdapterFactory
import com.easycampus.domain.model.Course
import com.easycampus.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * 同步课程用例
 */
class SyncCoursesUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
    private val platformAdapterFactory: PlatformAdapterFactory
) {
    operator fun invoke(platformType: String? = null): Flow<Result<List<Course>>> = flow {
        try {
            val allCourses = mutableListOf<Course>()
            
            if (platformType != null) {
                // 同步指定平台
                val adapter = platformAdapterFactory.getAdapter(platformType)
                    ?: throw IllegalArgumentException("不支持的平台类型: $platformType")
                
                val result = adapter.getCourses()
                if (result.isSuccess) {
                    val courses = result.getOrNull() ?: emptyList()
                    allCourses.addAll(courses)
                }
            } else {
                // 同步所有平台
                platformAdapterFactory.getAllAdapters().forEach { adapter ->
                    val result = adapter.getCourses()
                    if (result.isSuccess) {
                        val courses = result.getOrNull() ?: emptyList()
                        allCourses.addAll(courses)
                    }
                }
            }
            
            // 保存到本地数据库
            allCourses.forEach { course ->
                courseRepository.saveCourse(course)
            }
            
            emit(Result.success(allCourses))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
