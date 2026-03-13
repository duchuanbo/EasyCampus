package com.easycampus.domain.usecase.guet

import com.easycampus.data.repository.GuetRepositoryImpl
import com.easycampus.data.repository.SyncProgress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 同步GUET课程用例
 */
class SyncGuetCoursesUseCase @Inject constructor(
    private val repository: GuetRepositoryImpl
) {
    /**
     * 执行课程同步
     * @param forceRefresh 是否强制刷新（忽略本地缓存）
     * @return 同步进度流
     */
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<SyncProgress> {
        return repository.syncCourses()
    }
}
