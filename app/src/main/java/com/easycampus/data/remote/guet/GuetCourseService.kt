package com.easycampus.data.remote.guet

import com.easycampus.domain.model.Course
import com.easycampus.domain.model.CourseSchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GUET课程同步服务
 * 实现与GUET教务系统的课程数据同步
 */
@Singleton
class GuetCourseService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        const val BASE_URL = "https://guet.yuketang.cn"
        const val API_BASE = "$BASE_URL/api"
        const val MAX_RETRY_COUNT = 3
        const val INITIAL_RETRY_DELAY = 1000L
    }

    /**
     * 同步课程数据
     * @param token 用户Token
     * @param semester 学期（如：2024-2025-1）
     * @param lastSyncTime 上次同步时间（用于增量同步）
     * @return 同步结果
     */
    suspend fun syncCourses(
        token: String,
        semester: String? = null,
        lastSyncTime: Long? = null
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        var retryCount = 0
        var retryDelay = INITIAL_RETRY_DELAY

        while (true) {
            try {
                return@withContext performSync(token, semester, lastSyncTime)
            } catch (e: IOException) {
                retryCount++
                if (retryCount >= MAX_RETRY_COUNT) {
                    return@withContext Result.failure(
                        SyncException("网络错误，已重试$MAX_RETRY_COUNT次", e)
                    )
                }
                delay(retryDelay)
                retryDelay *= 2
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }

    /**
     * 执行实际同步
     */
    private fun performSync(
        token: String,
        semester: String?,
        lastSyncTime: Long?
    ): Result<SyncResult> {
        // 1. 获取课程列表
        val coursesUrl = buildString {
            append("$API_BASE/courses")
            semester?.let { append("?semester=$it") }
        }

        val coursesRequest = Request.Builder()
            .url(coursesUrl)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .get()
            .build()

        client.newCall(coursesRequest).execute().use { response ->
            if (!response.isSuccessful) {
                return Result.failure(SyncException("获取课程列表失败: ${response.code}"))
            }

            val responseBody = response.body?.string()
                ?: return Result.failure(SyncException("响应体为空"))

            return parseCoursesResponse(responseBody, lastSyncTime)
        }
    }

    /**
     * 解析课程响应
     */
    private fun parseCoursesResponse(
        responseBody: String,
        lastSyncTime: Long?
    ): Result<SyncResult> {
        return try {
            val json = JSONObject(responseBody)
            val success = json.optBoolean("success", false)

            if (!success) {
                val message = json.optString("message", "同步失败")
                return Result.failure(SyncException(message))
            }

            val data = json.optJSONObject("data")
                ?: return Result.failure(SyncException("响应数据为空"))

            val coursesArray = data.optJSONArray("courses") ?: JSONArray()
            val syncTime = System.currentTimeMillis()

            val courses = mutableListOf<Course>()
            val changedCourses = mutableListOf<Course>()

            for (i in 0 until coursesArray.length()) {
                val courseJson = coursesArray.getJSONObject(i)
                val course = parseCourse(courseJson)
                courses.add(course)

                // 检查是否为变更数据（增量同步）
                lastSyncTime?.let {
                    val updateTime = courseJson.optLong("update_time", 0)
                    if (updateTime > it) {
                        changedCourses.add(course)
                    }
                }
            }

            // 如果没有上次同步时间，表示全量同步
            val isIncremental = lastSyncTime != null
            val resultCourses = if (isIncremental && changedCourses.isNotEmpty()) {
                changedCourses
            } else {
                courses
            }

            Result.success(
                SyncResult(
                    courses = resultCourses,
                    syncTime = syncTime,
                    isIncremental = isIncremental,
                    totalCount = courses.size,
                    changedCount = changedCourses.size
                )
            )
        } catch (e: Exception) {
            Result.failure(SyncException("解析课程数据失败", e))
        }
    }

    /**
     * 解析单个课程
     */
    private fun parseCourse(courseJson: JSONObject): Course {
        val id = courseJson.optString("id", "")
        val name = courseJson.optString("name", "")
        val teacher = courseJson.optString("teacher", "")
        val location = courseJson.optString("location", "")
        val courseCode = courseJson.optString("course_code", "")
        val credits = courseJson.optDouble("credits", 0.0)
        val description = courseJson.optString("description", "")

        // 解析课程安排
        val schedules = parseSchedules(courseJson.optJSONArray("schedules"))

        return Course(
            id = id,
            name = name,
            teacher = teacher,
            location = location,
            courseCode = courseCode,
            credits = credits,
            description = description,
            platformId = "guet_yuketang",
            platformName = "GUET雨课堂",
            schedules = schedules,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * 解析课程安排
     */
    private fun parseSchedules(schedulesArray: JSONArray?): List<CourseSchedule> {
        if (schedulesArray == null) return emptyList()

        val schedules = mutableListOf<CourseSchedule>()

        for (i in 0 until schedulesArray.length()) {
            val scheduleJson = schedulesArray.getJSONObject(i)
            
            val dayOfWeek = scheduleJson.optInt("day_of_week", 1)
            val startSection = scheduleJson.optInt("start_section", 1)
            val endSection = scheduleJson.optInt("end_section", 2)
            val startTime = scheduleJson.optString("start_time", "08:00")
            val endTime = scheduleJson.optString("end_time", "09:40")
            val location = scheduleJson.optString("location", "")
            val weeks = parseWeeks(scheduleJson.optString("weeks", ""))

            schedules.add(
                CourseSchedule(
                    dayOfWeek = dayOfWeek,
                    startSection = startSection,
                    endSection = endSection,
                    startTime = startTime,
                    endTime = endTime,
                    location = location,
                    weeks = weeks
                )
            )
        }

        return schedules
    }

    /**
     * 解析周次
     * 格式："1-16" 或 "1,3,5,7,9,11,13,15"
     */
    private fun parseWeeks(weeksStr: String): List<Int> {
        if (weeksStr.isBlank()) return emptyList()

        val weeks = mutableListOf<Int>()
        val parts = weeksStr.split(",")

        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.contains("-")) {
                val range = trimmed.split("-")
                if (range.size == 2) {
                    val start = range[0].toIntOrNull() ?: continue
                    val end = range[1].toIntOrNull() ?: continue
                    weeks.addAll(start..end)
                }
            } else {
                trimmed.toIntOrNull()?.let { weeks.add(it) }
            }
        }

        return weeks.sorted()
    }

    /**
     * 获取当前学期
     */
    fun getCurrentSemester(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1

        // 学年判断：9月-次年1月为上学期，2月-7月为下学期
        return if (month >= 9 || month <= 1) {
            "$year-${year + 1}-1"
        } else {
            "${year - 1}-$year-2"
        }
    }
}

/**
 * 同步结果
 */
data class SyncResult(
    val courses: List<Course>,
    val syncTime: Long,
    val isIncremental: Boolean,
    val totalCount: Int,
    val changedCount: Int
)

/**
 * 同步异常
 */
class SyncException(message: String, cause: Throwable? = null) : Exception(message, cause)
