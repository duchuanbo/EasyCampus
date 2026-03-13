package com.easycampus.domain.usecase.checkin

import com.easycampus.data.remote.platform.PlatformAdapterFactory
import com.easycampus.domain.model.CheckIn
import com.easycampus.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * 检查新签到用例
 */
class CheckForNewCheckInsUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val platformAdapterFactory: PlatformAdapterFactory
) {
    operator fun invoke(): Flow<Result<List<CheckIn>>> = flow {
        try {
            val allNewCheckIns = mutableListOf<CheckIn>()
            
            platformAdapterFactory.getAllAdapters().forEach { adapter ->
                val result = adapter.checkForNewCheckIns()
                if (result.isSuccess) {
                    val newCheckIns = result.getOrNull() ?: emptyList()
                    allNewCheckIns.addAll(newCheckIns)
                    
                    // 保存到本地数据库
                    newCheckIns.forEach { checkIn ->
                        checkInRepository.saveCheckIn(checkIn)
                    }
                }
            }
            
            emit(Result.success(allNewCheckIns))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
