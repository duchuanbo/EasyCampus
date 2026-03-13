package com.easycampus.data.remote.api

import com.easycampus.domain.model.CheckIn
import com.easycampus.domain.model.CheckInResult
import com.easycampus.domain.model.Course
import retrofit2.Response
import retrofit2.http.*

/**
 * 平台API服务接口
 * 定义各教学平台的通用API接口
 */
interface PlatformApiService {
    
    /**
     * 用户登录
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
    
    /**
     * 获取课程列表
     */
    @GET("api/courses")
    suspend fun getCourses(): Response<List<CourseResponse>>
    
    /**
     * 获取签到列表
     */
    @GET("api/checkins")
    suspend fun getCheckIns(
        @Query("courseId") courseId: String? = null
    ): Response<List<CheckInResponse>>
    
    /**
     * 执行签到
     */
    @POST("api/checkins/{checkInId}/submit")
    suspend fun submitCheckIn(
        @Path("checkInId") checkInId: String,
        @Body request: CheckInSubmitRequest
    ): Response<CheckInResultResponse>
    
    /**
     * 获取用户信息
     */
    @GET("api/user/info")
    suspend fun getUserInfo(): Response<UserInfoResponse>
}

/**
 * 登录请求
 */
data class LoginRequest(
    val username: String,
    val password: String,
    val platformType: String
)

/**
 * 登录响应
 */
data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val userId: String,
    val username: String,
    val expiresIn: Long
)

/**
 * 课程响应
 */
data class CourseResponse(
    val id: String,
    val name: String,
    val teacher: String,
    val classroom: String?,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val startWeek: Int,
    val endWeek: Int,
    val color: String?
)

/**
 * 签到响应
 */
data class CheckInResponse(
    val id: String,
    val courseId: String,
    val courseName: String,
    val type: String,
    val status: String,
    val startTime: Long,
    val endTime: Long,
    val location: LocationResponse?,
    val gesture: String?,
    val code: String?
)

/**
 * 位置信息响应
 */
data class LocationResponse(
    val latitude: Double,
    val longitude: Double,
    val radius: Int
)

/**
 * 签到提交请求
 */
data class CheckInSubmitRequest(
    val location: LocationRequest? = null,
    val gesture: String? = null,
    val code: String? = null
)

/**
 * 位置请求
 */
data class LocationRequest(
    val latitude: Double,
    val longitude: Double
)

/**
 * 签到结果响应
 */
data class CheckInResultResponse(
    val success: Boolean,
    val message: String,
    val checkInTime: Long? = null
)

/**
 * 用户信息响应
 */
data class UserInfoResponse(
    val id: String,
    val username: String,
    val name: String,
    val avatar: String?,
    val email: String?,
    val phone: String?
)
