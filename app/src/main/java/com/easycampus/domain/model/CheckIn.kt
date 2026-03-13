package com.easycampus.domain.model

data class CheckIn(
    val id: String,
    val courseId: String,
    val courseName: String,
    val platformId: String,
    val type: CheckInType,
    val status: CheckInStatus,
    val startTime: Long,
    val endTime: Long?,
    val location: CheckInLocation?,
    val gesture: String?,
    val code: String?,
    val createdAt: Long,
    val completedAt: Long?
)

enum class CheckInType {
    NORMAL,      // 普通签到
    GESTURE,     // 手势签到
    LOCATION,    // 位置签到
    QR_CODE,     // 二维码签到
    CODE,        // 签到码
    FACE         // 人脸识别
}

enum class CheckInStatus {
    PENDING,     // 待签到
    COMPLETED,   // 已签到
    EXPIRED,     // 已过期
    FAILED       // 签到失败
}

data class CheckInLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val radius: Int? // 签到范围（米）
)

data class CheckInResult(
    val success: Boolean,
    val message: String,
    val checkInId: String?,
    val timestamp: Long
)

data class CheckInTask(
    val id: String,
    val checkInId: String,
    val scheduledTime: Long,
    val isAutoCheckIn: Boolean,
    val status: TaskStatus,
    val createdAt: Long,
    val executedAt: Long?
)

enum class TaskStatus {
    SCHEDULED,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}
