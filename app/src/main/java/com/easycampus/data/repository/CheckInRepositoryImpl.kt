package com.easycampus.data.repository

import com.easycampus.data.local.dao.CheckInDao
import com.easycampus.data.local.entity.CheckInEntity
import com.easycampus.data.local.entity.CheckInTaskEntity
import com.easycampus.domain.model.*
import com.easycampus.domain.repository.CheckInRepository
import com.easycampus.domain.repository.CheckInStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInRepositoryImpl @Inject constructor(
    private val checkInDao: CheckInDao
) : CheckInRepository {

    override fun getCheckIns(): Flow<List<CheckIn>> {
        return checkInDao.getAllCheckIns().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCheckInById(checkInId: String): Flow<CheckIn?> {
        return checkInDao.getCheckInById(checkInId).map { it?.toDomainModel() }
    }

    override fun getPendingCheckIns(): Flow<List<CheckIn>> {
        return checkInDao.getPendingCheckIns().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCheckInsByPlatform(platformId: String): Flow<List<CheckIn>> {
        return checkInDao.getCheckInsByPlatform(platformId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCheckInsByCourse(courseId: String): Flow<List<CheckIn>> {
        return checkInDao.getCheckInsByCourse(courseId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCheckInHistory(startTime: Long, endTime: Long): Flow<List<CheckIn>> {
        return checkInDao.getCheckInsByTimeRange(startTime, endTime).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun saveCheckIn(checkIn: CheckIn) {
        checkInDao.insertCheckIn(checkIn.toEntity())
    }

    override suspend fun markCheckInAsCompleted(checkInId: String, timestamp: Long) {
        // TODO: 更新签到状态为已完成
    }

    override suspend fun performCheckIn(
        checkInId: String,
        location: Pair<Double, Double>?,
        gesture: String?,
        code: String?
    ): Result<CheckInResult> {
        return try {
            val currentTime = System.currentTimeMillis()
            
            val result = CheckInResult(
                success = true,
                message = "签到成功",
                checkInId = checkInId,
                timestamp = currentTime
            )
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkCheckInStatus(checkInId: String): Result<CheckIn> {
        return try {
            val checkIn = checkInDao.getCheckInById(checkInId).map { 
                it?.toDomainModel() ?: throw IllegalStateException("Check-in not found")
            }
            Result.success(checkIn as CheckIn)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshCheckIns(): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scanForNewCheckIns(): Result<List<CheckIn>> {
        return try {
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scheduleCheckIn(checkIn: CheckIn, autoCheckIn: Boolean): Result<CheckInTask> {
        return try {
            val task = CheckInTaskEntity(
                id = UUID.randomUUID().toString(),
                checkInId = checkIn.id,
                scheduledTime = checkIn.startTime,
                isAutoCheckIn = autoCheckIn,
                status = TaskStatus.SCHEDULED.name,
                createdAt = System.currentTimeMillis(),
                executedAt = null
            )
            checkInDao.insertTask(task)
            Result.success(task.toDomainModel())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelScheduledCheckIn(taskId: String): Result<Unit> {
        return try {
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getScheduledTasks(): Flow<List<CheckInTask>> {
        return checkInDao.getScheduledTasks().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getCheckInStatistics(): Flow<CheckInStatistics> {
        return checkInDao.getAllCheckIns().map { entities ->
            val total = entities.size
            val successful = entities.count { it.status == CheckInStatus.COMPLETED.name }
            val failed = entities.count { it.status == CheckInStatus.FAILED.name }
            val missed = entities.count { it.status == CheckInStatus.EXPIRED.name }
            
            CheckInStatistics(
                totalCheckIns = total,
                successfulCheckIns = successful,
                failedCheckIns = failed,
                missedCheckIns = missed,
                successRate = if (total > 0) successful.toFloat() / total else 0f,
                byPlatform = emptyMap()
            )
        }
    }

    private fun CheckInEntity.toDomainModel(): CheckIn {
        return CheckIn(
            id = id,
            courseId = courseId,
            courseName = courseName,
            platformId = platformId,
            type = CheckInType.valueOf(type),
            status = CheckInStatus.valueOf(status),
            startTime = startTime,
            endTime = endTime,
            location = if (latitude != null && longitude != null) {
                CheckInLocation(
                    latitude = latitude,
                    longitude = longitude,
                    address = address,
                    radius = radius
                )
            } else null,
            gesture = gesture,
            code = code,
            createdAt = createdAt,
            completedAt = completedAt
        )
    }

    private fun CheckInTaskEntity.toDomainModel(): CheckInTask {
        return CheckInTask(
            id = id,
            checkInId = checkInId,
            scheduledTime = scheduledTime,
            isAutoCheckIn = isAutoCheckIn,
            status = TaskStatus.valueOf(status),
            createdAt = createdAt,
            executedAt = executedAt
        )
    }

    private fun CheckIn.toEntity(): CheckInEntity {
        return CheckInEntity(
            id = id,
            courseId = courseId,
            courseName = courseName,
            platformId = platformId,
            type = type.name,
            status = status.name,
            startTime = startTime,
            endTime = endTime,
            latitude = location?.latitude,
            longitude = location?.longitude,
            address = location?.address,
            radius = location?.radius,
            gesture = gesture,
            code = code,
            createdAt = createdAt,
            completedAt = completedAt,
            resultMessage = null
        )
    }
}
