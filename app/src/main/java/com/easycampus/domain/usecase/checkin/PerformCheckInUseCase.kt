package com.easycampus.domain.usecase.checkin

import com.easycampus.data.remote.platform.PlatformAdapterFactory
import com.easycampus.domain.model.CheckInResult
import com.easycampus.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * 执行签到用例
 */
class PerformCheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val platformAdapterFactory: PlatformAdapterFactory
) {
    operator fun invoke(
        checkInId: String,
        platformType: String,
        location: Pair<Double, Double>? = null,
        gesture: String? = null,
        code: String? = null
    ): Flow<Result<CheckInResult>> = flow {
        try {
            val adapter = platformAdapterFactory.getAdapter(platformType)
                ?: throw IllegalArgumentException("不支持的平台类型: $platformType")
            
            val result = adapter.performCheckIn(checkInId, location, gesture, code)
            
            if (result.isSuccess) {
                checkInRepository.markCheckInAsCompleted(checkInId, result.getOrNull()?.timestamp ?: System.currentTimeMillis())
            }
            
            emit(result)
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
