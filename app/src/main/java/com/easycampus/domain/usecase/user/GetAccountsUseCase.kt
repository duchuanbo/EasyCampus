package com.easycampus.domain.usecase.user

import com.easycampus.domain.model.Account
import com.easycampus.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取账户列表用例
 */
class GetAccountsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<List<Account>> {
        return userRepository.getAccounts()
    }
    
    fun getActiveAccounts(): Flow<List<Account>> {
        return userRepository.getActiveAccounts()
    }
    
    fun getAccountsByPlatform(platformType: String): Flow<List<Account>> {
        return userRepository.getAccountsByPlatform(platformType)
    }
}
