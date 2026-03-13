package com.easycampus.data.remote.platform

import com.easycampus.domain.model.*

/**
 * 平台适配器接口
 * 各平台需要实现此接口来适配自己的API
 */
interface PlatformAdapter {
    
    /**
     * 平台ID
     */
    val platformId: String
    
    /**
     * 平台名称
     */
    val platformName: String
    
    /**
     * 登录
     */
    suspend fun login(username: String, password: String): Result<Account>
    
    /**
     * 获取课程列表
     */
    suspend fun getCourses(): Result<List<Course>>
    
    /**
     * 获取签到列表
     */
    suspend fun getCheckIns(courseId: String? = null): Result<List<CheckIn>>
    
    /**
     * 执行签到
     */
    suspend fun performCheckIn(
        checkInId: String,
        location: Pair<Double, Double>? = null,
        gesture: String? = null,
        code: String? = null
    ): Result<CheckInResult>
    
    /**
     * 获取用户信息
     */
    suspend fun getUserInfo(): Result<User>
    
    /**
     * 检查是否有新的签到
     */
    suspend fun checkForNewCheckIns(): Result<List<CheckIn>>
}

/**
 * 平台适配器工厂
 */
class PlatformAdapterFactory(
    private val adapters: Map<String, PlatformAdapter>
) {
    fun getAdapter(platformType: String): PlatformAdapter? {
        return adapters[platformType]
    }
    
    fun getAllAdapters(): List<PlatformAdapter> {
        return adapters.values.toList()
    }
}
