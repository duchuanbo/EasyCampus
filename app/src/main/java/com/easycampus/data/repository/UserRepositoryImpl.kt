package com.easycampus.data.repository

import android.content.Context
import com.easycampus.data.local.dao.AccountDao
import com.easycampus.data.local.entity.AccountEntity
import com.easycampus.domain.model.Account
import com.easycampus.domain.model.Platform
import com.easycampus.domain.model.PlatformType
import com.easycampus.domain.model.User
import com.easycampus.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao,
    @ApplicationContext private val context: Context
) : UserRepository {

    private val platforms = listOf(
        Platform(
            id = "ketangpai",
            type = PlatformType.KETANGPAI,
            name = "课堂派",
            iconRes = 0,
            baseUrl = "https://www.ketangpai.com",
            isEnabled = true
        ),
        Platform(
            id = "yuketang",
            type = PlatformType.YUKETANG,
            name = "雨课堂",
            iconRes = 0,
            baseUrl = "https://www.yuketang.cn",
            isEnabled = true
        ),
        Platform(
            id = "changjiang",
            type = PlatformType.CHANGJIANG,
            name = "长江雨课堂",
            iconRes = 0,
            baseUrl = "https://changjiang.yuketang.cn",
            isEnabled = true
        ),
        Platform(
            id = "changke",
            type = PlatformType.CHANGKE,
            name = "畅课",
            iconRes = 0,
            baseUrl = "",
            isEnabled = false
        )
    )

    override suspend fun login(platformType: PlatformType, username: String, password: String): Result<User> {
        return try {
            val platformId = getPlatformId(platformType)
            val currentTime = System.currentTimeMillis()
            
            val account = AccountEntity(
                id = UUID.randomUUID().toString(),
                platformId = platformId,
                platformType = platformType.name,
                username = username,
                encryptedPassword = encryptPassword(password),
                authToken = "simulated_token_${System.currentTimeMillis()}",
                refreshToken = null,
                isActive = true,
                lastLoginTime = currentTime,
                createdAt = currentTime
            )
            
            accountDao.insertAccount(account)
            
            Result.success(
                User(
                    id = account.id,
                    username = username,
                    email = null,
                    avatarUrl = null,
                    createdAt = currentTime,
                    lastLoginAt = currentTime
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(platformType: PlatformType): Result<Unit> {
        return try {
            val platformId = getPlatformId(platformType)
            accountDao.deleteAccountByPlatform(platformId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return accountDao.getAllAccounts().map { accounts ->
            accounts.maxByOrNull { it.lastLoginTime }?.let { account ->
                User(
                    id = account.id,
                    username = account.username,
                    email = null,
                    avatarUrl = null,
                    createdAt = account.createdAt,
                    lastLoginAt = account.lastLoginTime
                )
            }
        }
    }

    override fun getAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getActiveAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.filter { it.isActive }.map { it.toDomainModel() }
        }
    }

    override fun getAccountsByPlatform(platformType: String): Flow<List<Account>> {
        return accountDao.getAllAccounts().map { entities ->
            entities.filter { it.platformType == platformType }.map { it.toDomainModel() }
        }
    }

    override suspend fun getAccount(platformType: PlatformType): Flow<Account?> {
        val platformId = getPlatformId(platformType)
        return accountDao.getAccountByPlatform(platformId).map { it?.toDomainModel() }
    }

    override suspend fun saveAccount(account: Account, password: String) {
        accountDao.insertAccount(account.toEntity())
    }

    override suspend fun addAccount(account: Account): Result<Unit> {
        return try {
            accountDao.insertAccount(account.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeAccount(platformType: PlatformType): Result<Unit> {
        return logout(platformType)
    }

    override suspend fun updateAccount(account: Account): Result<Unit> {
        return try {
            accountDao.updateAccount(account.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPlatforms(): Flow<List<Platform>> {
        return kotlinx.coroutines.flow.flowOf(platforms)
    }

    override fun getEnabledPlatforms(): Flow<List<Platform>> {
        return kotlinx.coroutines.flow.flowOf(platforms.filter { it.isEnabled })
    }

    override suspend fun updatePlatformEnabled(platformId: String, enabled: Boolean): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun refreshToken(platformType: PlatformType): Result<String> {
        return Result.success("refreshed_token")
    }

    override suspend fun isLoggedIn(platformType: PlatformType): Boolean {
        val platformId = getPlatformId(platformType)
        return accountDao.accountExists(platformId)
    }

    private fun getPlatformId(platformType: PlatformType): String {
        return when (platformType) {
            PlatformType.KETANGPAI -> "ketangpai"
            PlatformType.YUKETANG -> "yuketang"
            PlatformType.CHANGJIANG -> "changjiang"
            PlatformType.CHANGKE -> "changke"
        }
    }

    private fun encryptPassword(password: String): String {
        return password
    }

    private fun AccountEntity.toDomainModel(): Account {
        return Account(
            id = id,
            platformId = platformId,
            platformName = platformType,
            username = username,
            isActive = isActive,
            lastLoginTime = lastLoginTime,
            createdAt = createdAt
        )
    }

    private fun Account.toEntity(): AccountEntity {
        return AccountEntity(
            id = id,
            platformId = platformId,
            platformType = platformName,
            username = username,
            encryptedPassword = "",
            authToken = null,
            refreshToken = null,
            isActive = isActive,
            lastLoginTime = lastLoginTime,
            createdAt = createdAt
        )
    }
}
