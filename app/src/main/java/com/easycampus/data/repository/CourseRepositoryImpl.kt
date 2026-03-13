package com.easycampus.data.repository

import com.easycampus.data.local.dao.CourseDao
import com.easycampus.data.local.entity.CourseEntity
import com.easycampus.domain.model.Course
import com.easycampus.domain.model.WeeklySchedule
import com.easycampus.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepositoryImpl @Inject constructor(
    private val courseDao: CourseDao
) : CourseRepository {

    private var semesterStartDate: LocalDate = LocalDate.now()
    private var totalWeeks: Int = 20

    override fun getCourses(): Flow<List<Course>> {
        return courseDao.getAllCourses().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCourseById(courseId: String): Flow<Course?> {
        return courseDao.getCourseById(courseId).map { it?.toDomainModel() }
    }

    override fun getCoursesByDay(dayOfWeek: DayOfWeek): Flow<List<Course>> {
        return courseDao.getCoursesByDay(dayOfWeek).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCoursesForCurrentWeek(currentWeek: Int): Flow<List<Course>> {
        return courseDao.getAllCourses().map { entities ->
            entities.map { it.toDomainModel() }
                .filter { currentWeek in it.startWeek..it.endWeek }
        }
    }

    override suspend fun getCoursesByWeek(weekNumber: Int): Flow<WeeklySchedule> {
        return courseDao.getCoursesByWeek(weekNumber).map { entities ->
            val courses = entities.map { it.toDomainModel() }
            WeeklySchedule(
                weekNumber = weekNumber,
                coursesByDay = courses.groupBy { it.dayOfWeek }
            )
        }
    }

    override suspend fun getCurrentWeekCourses(): Flow<WeeklySchedule> {
        val currentWeek = getCurrentWeek()
        return getCoursesByWeek(currentWeek)
    }

    override suspend fun saveCourse(course: Course) {
        courseDao.insertCourse(course.toEntity())
    }

    override suspend fun addCourse(course: Course): Result<Unit> {
        return try {
            courseDao.insertCourse(course.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCourse(course: Course): Result<Unit> {
        return try {
            courseDao.updateCourse(course.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCourse(courseId: String): Result<Unit> {
        return try {
            courseDao.deleteCourseById(courseId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importFromPlatform(platformId: String): Result<List<Course>> {
        return Result.success(emptyList())
    }

    override suspend fun importFromFile(filePath: String): Result<List<Course>> {
        return Result.success(emptyList())
    }

    override suspend fun getCurrentWeek(): Int {
        val daysSinceStart = ChronoUnit.DAYS.between(semesterStartDate, LocalDate.now())
        return (daysSinceStart / 7 + 1).toInt().coerceIn(1, totalWeeks)
    }

    override suspend fun getTotalWeeks(): Int = totalWeeks

    override suspend fun setSemesterInfo(startDate: LocalDate, weeks: Int): Result<Unit> {
        return try {
            semesterStartDate = startDate
            totalWeeks = weeks
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshCourses(): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun syncWithPlatform(platformId: String): Result<Unit> {
        return try {
            val courses = importFromPlatform(platformId).getOrThrow()
            courseDao.insertCourses(courses.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun CourseEntity.toDomainModel(): Course {
        return Course(
            id = id,
            name = name,
            teacher = teacher,
            location = location,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            startWeek = startWeek,
            endWeek = endWeek,
            color = color,
            platformId = platformId,
            courseCode = courseCode,
            credit = credit,
            description = description
        )
    }

    private fun Course.toEntity(): CourseEntity {
        val currentTime = System.currentTimeMillis()
        return CourseEntity(
            id = id,
            name = name,
            teacher = teacher,
            location = location,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            startWeek = startWeek,
            endWeek = endWeek,
            color = color,
            platformId = platformId,
            courseCode = courseCode,
            credit = credit,
            description = description,
            createdAt = currentTime,
            updatedAt = currentTime
        )
    }
}
