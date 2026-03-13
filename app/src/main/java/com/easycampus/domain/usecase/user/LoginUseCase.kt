package com.easycampus.domain.usecase.user

import com.easycampus.data.remote.platform.PlatformAdapterFactory
import com.easycampus.domain.model.Account
import com.easycampus.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * 登录用例
 */
class LoginUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val platformAdapterFactory: PlatformAdapterFactory
) {
    operator fun invoke(
        platformType: String,
        username: String,
        password: String
    ): Flow<Result<Account>> = flow {
        try {
            val adapter = platformAdapterFactory.getAdapter(platformType)
                ?: throw IllegalArgumentException("不支持的平台类型: $platformType")
            
            val result = adapter.login(username, password)
            
            if (result.isSuccess) {
                val account = result.getOrNull()
                if (account != null) {
                    userRepository.saveAccount(account, password)
                }
            }
            
            emit(result)
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
