package com.easycampus.data.remote.guet

import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.easycampus.domain.model.CheckInResult
import com.easycampus.domain.model.CheckInStatus
import com.easycampus.domain.model.CheckInType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GUET签到服务
 * 实现完整的签到业务流程
 */
@Singleton
class GuetCheckInService @Inject constructor(
    private val context: Context
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        const val BASE_URL = "https://guet.yuketang.cn"
        const val API_BASE = "$BASE_URL/api"
        const val MAX_RETRY_COUNT = 3
        const val INITIAL_RETRY_DELAY = 1000L
        // 位置验证半径（米）
        const val LOCATION_VALID_RADIUS = 500.0
    }

    /**
     * 执行签到
     * @param token 用户Token
     * @param checkInId 签到ID
     * @param checkInType 签到类型
     * @param code 签到码/手势码（如需要）
     * @param expectedLocation 期望位置（用于位置签到验证）
     * @return 签到结果
     */
    suspend fun performCheckIn(
        token: String,
        checkInId: String,
        checkInType: CheckInType,
        code: String? = null,
        expectedLocation: Pair<Double, Double>? = null
    ): Result<CheckInResult> = withContext(Dispatchers.IO) {
        var retryCount = 0
        var retryDelay = INITIAL_RETRY_DELAY

        while (true) {
            try {
                // 1. 验证签到状态
                val statusResult = validateCheckInStatus(token, checkInId)
                if (statusResult.isFailure) {
                    return@withContext Result.failure(
                        CheckInException("验证签到状态失败: ${statusResult.exceptionOrNull()?.message}")
                    )
                }

                val status = statusResult.getOrNull()
                    ?: return@withContext Result.failure(CheckInException("无法获取签到状态"))

                if (status == CheckInStatus.COMPLETED) {
                    return@withContext Result.failure(CheckInException("您已完成签到，无需重复签到"))
                }

                // 2. 根据签到类型执行不同逻辑
                return@withContext when (checkInType) {
                    CheckInType.LOCATION -> performLocationCheckIn(
                        token, checkInId, expectedLocation
                    )
                    CheckInType.QR_CODE -> performQRCodeCheckIn(token, checkInId, code)
                    CheckInType.CODE -> performCodeCheckIn(token, checkInId, code)
                    CheckInType.GESTURE -> performGestureCheckIn(token, checkInId, code)
                    else -> performNormalCheckIn(token, checkInId)
                }
            } catch (e: IOException) {
                retryCount++
                if (retryCount >= MAX_RETRY_COUNT) {
                    return@withContext Result.failure(
                        CheckInException("网络错误，已重试$MAX_RETRY_COUNT次", e)
                    )
                }
                delay(retryDelay)
                retryDelay *= 2 // 指数退避
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }

    /**
     * 验证签到状态
     */
    private fun validateCheckInStatus(token: String, checkInId: String): Result<CheckInStatus> {
        val request = Request.Builder()
            .url("$API_BASE/checkin/$checkInId/status")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return Result.failure(CheckInException("获取签到状态失败: ${response.code}"))
            }

            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            val statusStr = json.optJSONObject("data")?.optString("status", "")

            val status = when (statusStr) {
                "completed" -> CheckInStatus.COMPLETED
                "pending" -> CheckInStatus.PENDING
                "expired" -> CheckInStatus.EXPIRED
                else -> CheckInStatus.FAILED
            }

            return Result.success(status)
        }
    }

    /**
     * 普通签到
     */
    private fun performNormalCheckIn(token: String, checkInId: String): Result<CheckInResult> {
        return executeCheckInRequest(token, checkInId, CheckInType.NORMAL)
    }

    /**
     * 位置签到
     */
    private fun performLocationCheckIn(
        token: String,
        checkInId: String,
        expectedLocation: Pair<Double, Double>?
    ): Result<CheckInResult> {
        // 获取当前位置
        val currentLocation = getCurrentLocation()
            ?: return Result.failure(CheckInException("无法获取当前位置，请检查定位权限"))

        // 验证位置是否在允许范围内
        expectedLocation?.let { (expectedLat, expectedLng) ->
            val distance = calculateDistance(
                currentLocation.first, currentLocation.second,
                expectedLat, expectedLng
            )

            if (distance > LOCATION_VALID_RADIUS) {
                return Result.failure(
                    CheckInException("您当前位置距离签到地点 ${distance.toInt()} 米，超出允许范围(${LOCATION_VALID_RADIUS.toInt()}米)")
                )
            }
        }

        return executeCheckInRequest(
            token, checkInId, CheckInType.LOCATION,
            extraParams = mapOf(
                "latitude" to currentLocation.first.toString(),
                "longitude" to currentLocation.second.toString()
            )
        )
    }

    /**
     * 二维码签到
     */
    private fun performQRCodeCheckIn(
        token: String,
        checkInId: String,
        code: String?
    ): Result<CheckInResult> {
        if (code.isNullOrBlank()) {
            return Result.failure(CheckInException("二维码内容不能为空"))
        }

        return executeCheckInRequest(
            token, checkInId, CheckInType.QR_CODE,
            extraParams = mapOf("qr_code" to code)
        )
    }

    /**
     * 签到码签到
     */
    private fun performCodeCheckIn(
        token: String,
        checkInId: String,
        code: String?
    ): Result<CheckInResult> {
        if (code.isNullOrBlank()) {
            return Result.failure(CheckInException("签到码不能为空"))
        }

        return executeCheckInRequest(
            token, checkInId, CheckInType.CODE,
            extraParams = mapOf("checkin_code" to code)
        )
    }

    /**
     * 手势签到
     */
    private fun performGestureCheckIn(
        token: String,
        checkInId: String,
        code: String?
    ): Result<CheckInResult> {
        if (code.isNullOrBlank()) {
            return Result.failure(CheckInException("手势码不能为空"))
        }

        return executeCheckInRequest(
            token, checkInId, CheckInType.GESTURE,
            extraParams = mapOf("gesture" to code)
        )
    }

    /**
     * 执行签到请求
     */
    private fun executeCheckInRequest(
        token: String,
        checkInId: String,
        checkInType: CheckInType,
        extraParams: Map<String, String> = emptyMap()
    ): Result<CheckInResult> {
        val formBodyBuilder = FormBody.Builder()
            .add("checkin_id", checkInId)
            .add("type", checkInType.name.lowercase())

        extraParams.forEach { (key, value) ->
            formBodyBuilder.add(key, value)
        }

        val request = Request.Builder()
            .url("$API_BASE/checkin/submit")
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(formBodyBuilder.build())
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(body).optString("message", "签到失败")
                } catch (e: Exception) {
                    "签到失败: ${response.code}"
                }
                return Result.failure(CheckInException(errorMsg))
            }

            return parseCheckInResponse(body, checkInId)
        }
    }

    /**
     * 解析签到响应
     */
    private fun parseCheckInResponse(responseBody: String, checkInId: String): Result<CheckInResult> {
        return try {
            val json = JSONObject(responseBody)
            val success = json.optBoolean("success", false)

            if (!success) {
                val message = json.optString("message", "签到失败")
                return Result.failure(CheckInException(message))
            }

            val data = json.optJSONObject("data")

            Result.success(
                CheckInResult(
                    checkInId = checkInId,
                    success = true,
                    message = "签到成功",
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(CheckInException("解析签到响应失败", e))
        }
    }

    /**
     * 获取当前位置
     */
    @Suppress("MissingPermission")
    private fun getCurrentLocation(): Pair<Double, Double>? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // 尝试获取GPS位置
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            // 尝试获取网络位置
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            val location = gpsLocation ?: networkLocation
            location?.let { Pair(it.latitude, it.longitude) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 计算两点间距离（米）
     */
    private fun calculateDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val earthRadius = 6371000.0 // 地球半径（米）

        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }

    /**
     * 获取签到历史
     */
    suspend fun getCheckInHistory(
        token: String,
        courseId: String? = null,
        startTime: Long? = null,
        endTime: Long? = null
    ): Result<List<CheckInRecord>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = StringBuilder("$API_BASE/checkin/history")
            urlBuilder.append("?")

            courseId?.let { urlBuilder.append("course_id=$it&") }
            startTime?.let { urlBuilder.append("start_time=$it&") }
            endTime?.let { urlBuilder.append("end_time=$it&") }

            val request = Request.Builder()
                .url(urlBuilder.toString().trimEnd('&'))
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(CheckInException("获取签到历史失败"))
                }

                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val data = json.optJSONObject("data")
                val recordsArray = data?.optJSONArray("records") ?: JSONArray()

                val records = mutableListOf<CheckInRecord>()
                for (i in 0 until recordsArray.length()) {
                    val recordJson = recordsArray.getJSONObject(i)
                    records.add(
                        CheckInRecord(
                            checkInId = recordJson.optString("checkin_id", ""),
                            courseName = recordJson.optString("course_name", ""),
                            status = recordJson.optString("status", ""),
                            time = recordJson.optLong("time", 0)
                        )
                    )
                }

                Result.success(records)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * 签到记录
 */
data class CheckInRecord(
    val checkInId: String,
    val courseName: String,
    val status: String,
    val time: Long
)

/**
 * 签到异常
 */
class CheckInException(message: String, cause: Throwable? = null) : Exception(message, cause)
