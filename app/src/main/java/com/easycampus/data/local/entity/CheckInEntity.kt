package com.easycampus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "check_ins",
    indices = [
        Index(value = ["courseId"]),
        Index(value = ["platformId"]),
        Index(value = ["status"])
    ]
)
data class CheckInEntity(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val courseName: String,
    val platformId: String,
    val type: String,
    val status: String,
    val startTime: Long,
    val endTime: Long?,
    val latitude: Double?,
    val longitude: Double?,
    val address: String?,
    val radius: Int?,
    val gesture: String?,
    val code: String?,
    val createdAt: Long,
    val completedAt: Long?,
    val resultMessage: String?
)

@Entity(
    tableName = "check_in_tasks",
    indices = [Index(value = ["checkInId"])]
)
data class CheckInTaskEntity(
    @PrimaryKey
    val id: String,
    val checkInId: String,
    val scheduledTime: Long,
    val isAutoCheckIn: Boolean,
    val status: String,
    val createdAt: Long,
    val executedAt: Long?
)
