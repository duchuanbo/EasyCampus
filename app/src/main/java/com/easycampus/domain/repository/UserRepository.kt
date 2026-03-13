package com.easycampus.domain.repository

import com.easycampus.domain.model.Account
import com.easycampus.domain.model.Platform
import com.easycampus.domain.model.PlatformType
import com.easycampus.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(platformType: PlatformType, username: String, password: String): Result<User>
    suspend fun logout(platformType: PlatformType): Result<Unit>
    fun getCurrentUser(): Flow<User?>
    
    fun getAccounts(): Flow<List<Account>>
    fun getActiveAccounts(): Flow<List<Account>>
    fun getAccountsByPlatform(platformType: String): Flow<List<Account>>
    suspend fun getAccount(platformType: PlatformType): Flow<Account?>
    suspend fun saveAccount(account: Account, password: String)
    suspend fun addAccount(account: Account): Result<Unit>
    suspend fun removeAccount(platformType: PlatformType): Result<Unit>
    suspend fun updateAccount(account: Account): Result<Unit>
    
    fun getPlatforms(): Flow<List<Platform>>
    fun getEnabledPlatforms(): Flow<List<Platform>>
    suspend fun updatePlatformEnabled(platformId: String, enabled: Boolean): Result<Unit>
    
    suspend fun refreshToken(platformType: PlatformType): Result<String>
    suspend fun isLoggedIn(platformType: PlatformType): Boolean
}
