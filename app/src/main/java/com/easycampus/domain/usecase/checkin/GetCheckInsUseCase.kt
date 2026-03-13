package com.easycampus.domain.usecase.checkin

import com.easycampus.domain.model.CheckIn
import com.easycampus.domain.repository.CheckInRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取签到列表用例
 */
class GetCheckInsUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository
) {
    operator fun invoke(): Flow<List<CheckIn>> {
        return checkInRepository.getCheckIns()
    }
    
    fun getPendingCheckIns(): Flow<List<CheckIn>> {
        return checkInRepository.getPendingCheckIns()
    }
    
    fun getCheckInsByPlatform(platformId: String): Flow<List<CheckIn>> {
        return checkInRepository.getCheckInsByPlatform(platformId)
    }
    
    fun getCheckInsByCourse(courseId: String): Flow<List<CheckIn>> {
        return checkInRepository.getCheckInsByCourse(courseId)
    }
}
