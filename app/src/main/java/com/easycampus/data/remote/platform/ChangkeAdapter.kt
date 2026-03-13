package com.easycampus.data.remote.platform

import com.easycampus.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 畅课平台适配器
 */
@Singleton
class ChangkeAdapter @Inject constructor() : PlatformAdapter {

    override val platformId: String = "changke"
    override val platformName: String = "畅课"

    override suspend fun login(username: String, password: String): Result<Account> {
        return try {
            val account = Account(
                id = "",
                platformId = platformId,
                platformName = platformName,
                username = username,
                isActive = true,
                lastLoginTime = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
            Result.success(account)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCourses(): Result<List<Course>> {
        return Result.success(emptyList())
    }

    override suspend fun getCheckIns(courseId: String?): Result<List<CheckIn>> {
        return Result.success(emptyList())
    }

    override suspend fun performCheckIn(
        checkInId: String,
        location: Pair<Double, Double>?,
        gesture: String?,
        code: String?
    ): Result<CheckInResult> {
        return Result.success(
            CheckInResult(
                success = true,
                message = "签到成功",
                checkInId = checkInId,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun getUserInfo(): Result<User> {
        return Result.success(
            User(
                id = "",
                username = "",
                email = null,
                avatarUrl = null,
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun checkForNewCheckIns(): Result<List<CheckIn>> {
        return Result.success(emptyList())
    }
}
