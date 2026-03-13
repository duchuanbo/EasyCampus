package com.easycampus.domain.usecase.guet

import com.easycampus.data.repository.GuetRepositoryImpl
import com.easycampus.domain.model.CheckInResult
import com.easycampus.domain.model.CheckInType
import javax.inject.Inject

/**
 * GUET签到用例
 */
class GuetCheckInUseCase @Inject constructor(
    private val repository: GuetRepositoryImpl
) {
    /**
     * 执行签到
     * @param checkInId 签到ID
     * @param type 签到类型
     * @param code 签到码/手势码（如需要）
     * @param latitude 纬度（位置签到需要）
     * @param longitude 经度（位置签到需要）
     * @return 签到结果
     */
    suspend operator fun invoke(
        checkInId: String,
        type: CheckInType,
        code: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<CheckInResult> {
        // 参数验证
        if (checkInId.isBlank()) {
            return Result.failure(IllegalArgumentException("签到ID不能为空"))
        }

        // 根据签到类型验证必要参数
        when (type) {
            CheckInType.CODE, CheckInType.GESTURE, CheckInType.QR_CODE -> {
                if (code.isNullOrBlank()) {
                    return Result.failure(
                        IllegalArgumentException("${getCheckInTypeName(type)}不能为空")
                    )
                }
            }
            CheckInType.LOCATION -> {
                if (latitude == null || longitude == null) {
                    return Result.failure(
                        IllegalArgumentException("位置签到需要获取当前位置信息")
                    )
                }
            }
            else -> { /* 普通签到不需要额外参数 */ }
        }

        val params = mutableMapOf<String, String>()
        code?.let { params["code"] = it }
        latitude?.let { params["latitude"] = it.toString() }
        longitude?.let { params["longitude"] = it.toString() }

        return repository.performCheckIn(checkInId, type, params)
    }

    private fun getCheckInTypeName(type: CheckInType): String {
        return when (type) {
            CheckInType.CODE -> "签到码"
            CheckInType.GESTURE -> "手势码"
            CheckInType.QR_CODE -> "二维码"
            CheckInType.LOCATION -> "位置"
            else -> ""
        }
    }
}
