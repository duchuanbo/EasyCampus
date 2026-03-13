package com.easycampus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime

@Entity(
    tableName = "courses",
    indices = [
        Index(value = ["platformId"]),
        Index(value = ["dayOfWeek"])
    ]
)
data class CourseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val teacher: String,
    val location: String,
    val dayOfWeek: DayOfWeek,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val startWeek: Int,
    val endWeek: Int,
    val color: Int,
    val platformId: String?,
    val courseCode: String?,
    val credit: Float?,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long
)
