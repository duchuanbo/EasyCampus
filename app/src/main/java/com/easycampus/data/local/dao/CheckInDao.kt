package com.easycampus.data.local.dao

import androidx.room.*
import com.easycampus.data.local.entity.CheckInEntity
import com.easycampus.data.local.entity.CheckInTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("SELECT * FROM check_ins ORDER BY startTime DESC")
    fun getAllCheckIns(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE id = :checkInId LIMIT 1")
    fun getCheckInById(checkInId: String): Flow<CheckInEntity?>

    @Query("SELECT * FROM check_ins WHERE status = 'PENDING' ORDER BY startTime ASC")
    fun getPendingCheckIns(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE platformId = :platformId ORDER BY startTime DESC")
    fun getCheckInsByPlatform(platformId: String): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE courseId = :courseId ORDER BY startTime DESC")
    fun getCheckInsByCourse(courseId: String): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE startTime >= :startTime AND startTime <= :endTime ORDER BY startTime DESC")
    fun getCheckInsByTimeRange(startTime: Long, endTime: Long): Flow<List<CheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIns(checkIns: List<CheckInEntity>)

    @Update
    suspend fun updateCheckIn(checkIn: CheckInEntity)

    @Delete
    suspend fun deleteCheckIn(checkIn: CheckInEntity)

    @Query("DELETE FROM check_ins WHERE id = :checkInId")
    suspend fun deleteCheckInById(checkInId: String)

    // CheckInTask operations
    @Query("SELECT * FROM check_in_tasks ORDER BY scheduledTime ASC")
    fun getAllTasks(): Flow<List<CheckInTaskEntity>>

    @Query("SELECT * FROM check_in_tasks WHERE status = 'SCHEDULED' ORDER BY scheduledTime ASC")
    fun getScheduledTasks(): Flow<List<CheckInTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: CheckInTaskEntity)

    @Update
    suspend fun updateTask(task: CheckInTaskEntity)

    @Delete
    suspend fun deleteTask(task: CheckInTaskEntity)

    @Query("DELETE FROM check_in_tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)
}
