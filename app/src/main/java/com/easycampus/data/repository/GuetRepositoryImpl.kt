package com.easycampus.data.repository

import com.easycampus.data.local.TokenManager
import com.easycampus.data.remote.guet.*
import com.easycampus.domain.model.*
import com.easycampus.domain.repository.CheckInRepository
import com.easycampus.domain.repository.CourseRepository
import com.easycampus.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GUET Repository实现
 * 整合认证、课程、签到功能
 */
@Singleton
class GuetRepositoryImpl @Inject constructor(
    private val authService: GuetAuthService,
    private val courseService: GuetCourseService,
    private val checkInService: GuetCheckInService,
    private val tokenManager: TokenManager
) : UserRepository, CourseRepository, CheckInRepository {

    // ==================== UserRepository 实现 ====================

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val authResult = authService.login(username, password)
            
            authResult.fold(
                onSuccess = { result ->
                    // 保存Token
                    tokenManager.saveToken(
                        accessToken = result.token,
                        userId = result.user.id,
                        platformId = result.account.platformId
                    )
                    Result.success(result.user)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            tokenManager.clearToken()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val tokenInfo = tokenManager.getTokenInfo().first()
                ?: return Result.failure(Exception("用户未登录"))

            // 验证Token有效性
            val isValid = authService.validateToken(tokenInfo.accessToken)
            
            if (isValid.getOrDefault(false)) {
                Result.success(
                    User(
                        id = tokenInfo.userId,
                        username = tokenInfo.userId,
                        email = null,
                        avatarUrl = null,
                        createdAt = System.currentTimeMillis(),
                        lastLoginAt = System.currentTimeMillis()
                    )
                )
            } else {
                // Token过期，尝试刷新
                tokenInfo.refreshToken?.let { refreshToken ->
                    val refreshResult = authService.refreshToken(refreshToken)
                    refreshResult.fold(
                        onSuccess = { newToken ->
                            tokenManager.updateAccessToken(newToken)
                            Result.success(
                                User(
                                    id = tokenInfo.userId,
                                    username = tokenInfo.userId,
                                    email = null,
                                    avatarUrl = null,
                                    createdAt = System.currentTimeMillis(),
                                    lastLoginAt = System.currentTimeMillis()
                                )
                            )
                        },
                        onFailure = {
                            Result.failure(Exception("登录已过期，请重新登录"))
                        }
                    )
                } ?: Result.failure(Exception("登录已过期，请重新登录"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<User> {
        // GUET系统暂不支持更新用户信息
        return Result.success(user)
    }

    override fun isLoggedIn(): Flow<Boolean> = flow {
        emit(tokenManager.isLoggedIn())
    }

    // ==================== CourseRepository 实现 ====================

    override suspend fun getCourses(forceRefresh: Boolean): Result<List<Course>> {
        return try {
            val token = tokenManager.getAccessTokenSync()
                ?: return Result.failure(Exception("用户未登录"))

            val lastSyncTime = if (!forceRefresh) {
                // 从本地获取上次同步时间
                null
            } else null

            val semester = courseService.getCurrentSemester()
            val syncResult = courseService.syncCourses(token, semester, lastSyncTime)

            syncResult.fold(
                onSuccess = { result ->
                    Result.success(result.courses)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCourseById(courseId: String): Result<Course> {
        return try {
            val coursesResult = getCourses()
            coursesResult.fold(
                onSuccess = { courses ->
                    val course = courses.find { it.id == courseId }
                    if (course != null) {
                        Result.success(course)
                    } else {
                        Result.failure(Exception("课程不存在"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncCourses(): Result<SyncProgress> {
        return try {
            val token = tokenManager.getAccessTokenSync()
                ?: return Result.failure(Exception("用户未登录"))

            val semester = courseService.getCurrentSemester()
            val syncResult = courseService.syncCourses(token, semester, null)

            syncResult.fold(
                onSuccess = { result ->
                    Result.success(
                        SyncProgress(
                            totalCourses = result.totalCount,
                            syncedCourses = result.courses.size,
                            isCompleted = true,
                            message = "同步完成"
                        )
                    )
                },
                onFailure = { error ->
                    Result.success(
                        SyncProgress(
                            totalCourses = 0,
                            syncedCourses = 0,
                            isCompleted = false,
                            message = error.message ?: "同步失败"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveCourse(course: Course): Result<Unit> {
        // GUET系统课程为只读，不支持本地保存
        return Result.success(Unit)
    }

    override suspend fun deleteCourse(courseId: String): Result<Unit> {
        // GUET系统课程为只读，不支持删除
        return Result.success(Unit)
    }

    override fun getCoursesFlow(): Flow<List<Course>> = flow {
        val result = getCourses()
        result.getOrNull()?.let { emit(it) }
    }

    // ==================== CheckInRepository 实现 ====================

    override suspend fun getCheckIns(courseId: String?): Result<List<CheckIn>> {
        // 从签到历史构建CheckIn列表
        return try {
            val token = tokenManager.getAccessTokenSync()
                ?: return Result.failure(Exception("用户未登录"))

            val historyResult = checkInService.getCheckInHistory(token, courseId)
            
            historyResult.fold(
                onSuccess = { records ->
                    val checkIns = records.map { record ->
                        CheckIn(
                            id = record.checkInId,
                            courseId = courseId ?: "",
                            courseName = record.courseName,
                            title = "签到",
                            type = CheckInType.NORMAL,
                            status = when (record.status) {
                                "completed" -> CheckInStatus.COMPLETED
                                "pending" -> CheckInStatus.PENDING
                                else -> CheckInStatus.FAILED
                            },
                            startTime = record.time,
                            endTime = record.time + 600000, // 默认10分钟有效期
                            createdAt = record.time
                        )
                    }
                    Result.success(checkIns)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCheckInById(checkInId: String): Result<CheckIn> {
        return try {
            val checkInsResult = getCheckIns()
            checkInsResult.fold(
                onSuccess = { checkIns ->
                    val checkIn = checkIns.find { it.id == checkInId }
                    if (checkIn != null) {
                        Result.success(checkIn)
                    } else {
                        Result.failure(Exception("签到记录不存在"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun performCheckIn(
        checkInId: String,
        type: CheckInType,
        params: Map<String, String>
    ): Result<CheckInResult> {
        return try {
            val token = tokenManager.getAccessTokenSync()
                ?: return Result.failure(Exception("用户未登录"))

            val code = params["code"]
            val expectedLocation = params["latitude"]?.toDoubleOrNull()?.let { lat ->
                params["longitude"]?.toDoubleOrNull()?.let { lng ->
                    Pair(lat, lng)
                }
            }

            checkInService.performCheckIn(
                token = token,
                checkInId = checkInId,
                checkInType = type,
                code = code,
                expectedLocation = expectedLocation
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCheckInHistory(
        startDate: Long,
        endDate: Long
    ): Result<List<CheckIn>> {
        return try {
            val token = tokenManager.getAccessTokenSync()
                ?: return Result.failure(Exception("用户未登录"))

            val historyResult = checkInService.getCheckInHistory(
                token = token,
                startTime = startDate,
                endTime = endDate
            )

            historyResult.fold(
                onSuccess = { records ->
                    val checkIns = records.map { record ->
                        CheckIn(
                            id = record.checkInId,
                            courseId = "",
                            courseName = record.courseName,
                            title = "签到",
                            type = CheckInType.NORMAL,
                            status = when (record.status) {
                                "completed" -> CheckInStatus.COMPLETED
                                "pending" -> CheckInStatus.PENDING
                                else -> CheckInStatus.FAILED
                            },
                            startTime = record.time,
                            endTime = record.time + 600000,
                            createdAt = record.time
                        )
                    }
                    Result.success(checkIns)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveCheckIn(checkIn: CheckIn): Result<Unit> {
        // GUET签到记录为只读
        return Result.success(Unit)
    }

    override suspend fun markCheckInAsCompleted(checkInId: String): Result<Unit> {
        // 签到状态由服务器管理
        return Result.success(Unit)
    }

    override fun getCheckInsFlow(courseId: String?): Flow<List<CheckIn>> = flow {
        val result = getCheckIns(courseId)
        result.getOrNull()?.let { emit(it) }
    }

    override fun getCheckInStats(): Flow<CheckInStats> = flow {
        // 统计签到数据
        val result = getCheckIns()
        result.getOrNull()?.let { checkIns ->
            val total = checkIns.size
            val completed = checkIns.count { it.status == CheckInStatus.COMPLETED }
            val pending = checkIns.count { it.status == CheckInStatus.PENDING }
            val expired = checkIns.count { it.status == CheckInStatus.EXPIRED }
            
            emit(
                CheckInStats(
                    totalCheckIns = total,
                    completedCheckIns = completed,
                    pendingCheckIns = pending,
                    expiredCheckIns = expired,
                    completionRate = if (total > 0) completed.toFloat() / total else 0f
                )
            )
        }
    }
}

/**
 * 同步进度
 */
data class SyncProgress(
    val totalCourses: Int,
    val syncedCourses: Int,
    val isCompleted: Boolean,
    val message: String
)
