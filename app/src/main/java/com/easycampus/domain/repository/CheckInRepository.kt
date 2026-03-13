package com.easycampus.domain.repository

import com.easycampus.domain.model.CheckIn
import com.easycampus.domain.model.CheckInResult
import com.easycampus.domain.model.CheckInTask
import com.easycampus.domain.model.CheckInType
import kotlinx.coroutines.flow.Flow

interface CheckInRepository {
    fun getCheckIns(): Flow<List<CheckIn>>
    fun getCheckInById(checkInId: String): Flow<CheckIn?>
    fun getPendingCheckIns(): Flow<List<CheckIn>>
    fun getCheckInsByPlatform(platformId: String): Flow<List<CheckIn>>
    fun getCheckInsByCourse(courseId: String): Flow<List<CheckIn>>
    
    suspend fun performCheckIn(
        checkInId: String,
        location: Pair<Double, Double>? = null,
        gesture: String? = null,
        code: String? = null
    ): Result<CheckInResult>
    
    suspend fun saveCheckIn(checkIn: CheckIn)
    suspend fun markCheckInAsCompleted(checkInId: String, timestamp: Long)
    suspend fun checkCheckInStatus(checkInId: String): Result<CheckIn>
    suspend fun refreshCheckIns(): Result<Unit>
    suspend fun scanForNewCheckIns(): Result<List<CheckIn>>
    
    suspend fun scheduleCheckIn(checkIn: CheckIn, autoCheckIn: Boolean): Result<CheckInTask>
    suspend fun cancelScheduledCheckIn(taskId: String): Result<Unit>
    fun getScheduledTasks(): Flow<List<CheckInTask>>
    
    fun getCheckInHistory(startTime: Long, endTime: Long): Flow<List<CheckIn>>
    fun getCheckInStatistics(): Flow<CheckInStatistics>
}

data class CheckInStatistics(
    val totalCheckIns: Int,
    val successfulCheckIns: Int,
    val failedCheckIns: Int,
    val missedCheckIns: Int,
    val successRate: Float,
    val byPlatform: Map<String, PlatformStatistics>
)

data class PlatformStatistics(
    val platformId: String,
    val platformName: String,
    val totalCheckIns: Int,
    val successfulCheckIns: Int,
    val successRate: Float
)
