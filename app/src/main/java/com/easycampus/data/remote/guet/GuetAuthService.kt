package com.easycampus.data.remote.guet

import com.easycampus.domain.model.Account
import com.easycampus.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GUET（桂林电子科技大学）认证服务
 * 参考雨课堂GUET版本实现登录逻辑
 */
@Singleton
class GuetAuthService @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    companion object {
        // GUET雨课堂地址
        const val BASE_URL = "https://guet.yuketang.cn"
        const val LOGIN_URL = "$BASE_URL/pc/login"
        const val API_BASE = "$BASE_URL/api"
        
        // 最大重试次数
        const val MAX_RETRY_COUNT = 3
        // 初始重试延迟（毫秒）
        const val INITIAL_RETRY_DELAY = 1000L
    }

    /**
     * 用户登录
     * @param studentId 学号
     * @param password 密码
     * @return 登录结果
     */
    suspend fun login(studentId: String, password: String): Result<GuetAuthResult> = 
        withContext(Dispatchers.IO) {
            var retryCount = 0
            var retryDelay = INITIAL_RETRY_DELAY
            
            while (true) {
                try {
                    return@withContext performLogin(studentId, password)
                } catch (e: IOException) {
                    retryCount++
                    if (retryCount >= MAX_RETRY_COUNT) {
                        return@withContext Result.failure(
                            GuetAuthException("网络错误，已重试$MAX_RETRY_COUNT次", e)
                        )
                    }
                    // 指数退避
                    delay(retryDelay)
                    retryDelay *= 2
                } catch (e: Exception) {
                    return@withContext Result.failure(e)
                }
            }
        }

    /**
     * 执行实际登录请求
     */
    private fun performLogin(studentId: String, password: String): Result<GuetAuthResult> {
        // 1. 获取登录页面，提取必要参数
        val loginPageRequest = Request.Builder()
            .url(LOGIN_URL)
            .get()
            .build()

        client.newCall(loginPageRequest).execute().use { response ->
            if (!response.isSuccessful) {
                return Result.failure(GuetAuthException("获取登录页面失败: ${response.code}"))
            }
        }

        // 2. 提交登录表单
        val formBody = FormBody.Builder()
            .add("username", studentId)
            .add("password", password)
            .add("remember", "true")
            .build()

        val loginRequest = Request.Builder()
            .url("$BASE_URL/pc/login/verify")
            .post(formBody)
            .header("Referer", LOGIN_URL)
            .header("X-Requested-With", "XMLHttpRequest")
            .build()

        client.newCall(loginRequest).execute().use { response ->
            if (!response.isSuccessful) {
                return Result.failure(GuetAuthException("登录请求失败: ${response.code}"))
            }

            val responseBody = response.body?.string() 
                ?: return Result.failure(GuetAuthException("响应体为空"))

            return parseLoginResponse(responseBody, studentId)
        }
    }

    /**
     * 解析登录响应
     */
    private fun parseLoginResponse(responseBody: String, studentId: String): Result<GuetAuthResult> {
        return try {
            val json = JSONObject(responseBody)
            val success = json.optBoolean("success", false)
            
            if (!success) {
                val message = json.optString("message", "登录失败")
                return Result.failure(GuetAuthException(message))
            }

            val data = json.optJSONObject("data")
                ?: return Result.failure(GuetAuthException("响应数据为空"))

            val token = data.optString("token", "")
            val userId = data.optString("user_id", "")
            val name = data.optString("name", "")
            val avatar = data.optString("avatar", "")

            if (token.isBlank()) {
                return Result.failure(GuetAuthException("获取Token失败"))
            }

            val account = Account(
                id = userId,
                platformId = "guet_yuketang",
                platformName = "GUET雨课堂",
                username = studentId,
                isActive = true,
                lastLoginTime = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )

            val user = User(
                id = userId,
                username = name.ifBlank { studentId },
                email = null,
                avatarUrl = avatar.ifBlank { null },
                createdAt = System.currentTimeMillis(),
                lastLoginAt = System.currentTimeMillis()
            )

            Result.success(GuetAuthResult(account, user, token))
        } catch (e: Exception) {
            Result.failure(GuetAuthException("解析响应失败", e))
        }
    }

    /**
     * 验证Token是否有效
     */
    suspend fun validateToken(token: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$API_BASE/user/profile")
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                Result.success(response.isSuccessful)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 刷新Token
     */
    suspend fun refreshToken(refreshToken: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formBody = FormBody.Builder()
                .add("refresh_token", refreshToken)
                .build()

            val request = Request.Builder()
                .url("$BASE_URL/api/refresh-token")
                .post(formBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(GuetAuthException("刷新Token失败"))
                }

                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val newToken = json.optJSONObject("data")?.optString("token", "")
                
                if (newToken.isNotBlank()) {
                    Result.success(newToken)
                } else {
                    Result.failure(GuetAuthException("获取新Token失败"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * GUET认证结果
 */
data class GuetAuthResult(
    val account: Account,
    val user: User,
    val token: String
)

/**
 * GUET认证异常
 */
class GuetAuthException(message: String, cause: Throwable? = null) : Exception(message, cause)
