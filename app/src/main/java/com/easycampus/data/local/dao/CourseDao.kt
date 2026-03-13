package com.easycampus.data.local.dao

import androidx.room.*
import com.easycampus.data.local.entity.CourseEntity
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY dayOfWeek, startTime")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    fun getCourseById(courseId: String): Flow<CourseEntity?>

    @Query("SELECT * FROM courses WHERE dayOfWeek = :dayOfWeek ORDER BY startTime")
    fun getCoursesByDay(dayOfWeek: DayOfWeek): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE platformId = :platformId ORDER BY dayOfWeek, startTime")
    fun getCoursesByPlatform(platformId: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE startWeek <= :week AND endWeek >= :week ORDER BY dayOfWeek, startTime")
    fun getCoursesByWeek(week: Int): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Delete
    suspend fun deleteCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourseById(courseId: String)

    @Query("DELETE FROM courses WHERE platformId = :platformId")
    suspend fun deleteCoursesByPlatform(platformId: String)

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCourseCount(): Int
}
