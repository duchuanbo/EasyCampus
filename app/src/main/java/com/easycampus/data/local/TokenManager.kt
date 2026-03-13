package com.easycampus.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token管理器
 * 负责安全存储和管理用户登录令牌
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "token_prefs")

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val TOKEN_EXPIRE_TIME_KEY = stringPreferencesKey("token_expire_time")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val PLATFORM_ID_KEY = stringPreferencesKey("platform_id")

        // Token过期时间（默认7天）
        const val TOKEN_VALIDITY_DAYS = 7L
        const val TOKEN_VALIDITY_MILLIS = TOKEN_VALIDITY_DAYS * 24 * 60 * 60 * 1000
    }

    /**
     * 保存Token
     */
    suspend fun saveToken(
        accessToken: String,
        refreshToken: String? = null,
        userId: String,
        platformId: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            refreshToken?.let { preferences[REFRESH_TOKEN_KEY] = it }
            preferences[USER_ID_KEY] = userId
            preferences[PLATFORM_ID_KEY] = platformId
            preferences[TOKEN_EXPIRE_TIME_KEY] = 
                (System.currentTimeMillis() + TOKEN_VALIDITY_MILLIS).toString()
        }
    }

    /**
     * 获取Access Token
     */
    fun getAccessToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }

    /**
     * 同步获取Access Token（阻塞式）
     */
    fun getAccessTokenSync(): String? = runBlocking {
        context.dataStore.data.first()[ACCESS_TOKEN_KEY]
    }

    /**
     * 获取Refresh Token
     */
    fun getRefreshToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }
    }

    /**
     * 获取用户ID
     */
    fun getUserId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }
    }

    /**
     * 获取平台ID
     */
    fun getPlatformId(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[PLATFORM_ID_KEY]
        }
    }

    /**
     * 检查Token是否过期
     */
    suspend fun isTokenExpired(): Boolean {
        val expireTimeStr = context.dataStore.data.first()[TOKEN_EXPIRE_TIME_KEY]
        val expireTime = expireTimeStr?.toLongOrNull() ?: return true
        return System.currentTimeMillis() >= expireTime
    }

    /**
     * 获取Token剩余有效时间（毫秒）
     */
    suspend fun getTokenRemainingTime(): Long {
        val expireTimeStr = context.dataStore.data.first()[TOKEN_EXPIRE_TIME_KEY]
        val expireTime = expireTimeStr?.toLongOrNull() ?: return 0
        return maxOf(0, expireTime - System.currentTimeMillis())
    }

    /**
     * 更新Access Token
     */
    suspend fun updateAccessToken(newAccessToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = newAccessToken
            preferences[TOKEN_EXPIRE_TIME_KEY] = 
                (System.currentTimeMillis() + TOKEN_VALIDITY_MILLIS).toString()
        }
    }

    /**
     * 清除所有Token
     */
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(TOKEN_EXPIRE_TIME_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(PLATFORM_ID_KEY)
        }
    }

    /**
     * 检查是否已登录
     */
    suspend fun isLoggedIn(): Boolean {
        val token = context.dataStore.data.first()[ACCESS_TOKEN_KEY]
        return !token.isNullOrBlank() && !isTokenExpired()
    }

    /**
     * 获取Token信息
     */
    fun getTokenInfo(): Flow<TokenInfo?> {
        return context.dataStore.data.map { preferences ->
            val accessToken = preferences[ACCESS_TOKEN_KEY] ?: return@map null
            TokenInfo(
                accessToken = accessToken,
                refreshToken = preferences[REFRESH_TOKEN_KEY],
                userId = preferences[USER_ID_KEY] ?: "",
                platformId = preferences[PLATFORM_ID_KEY] ?: "",
                expireTime = preferences[TOKEN_EXPIRE_TIME_KEY]?.toLongOrNull() ?: 0
            )
        }
    }
}

/**
 * Token信息
 */
data class TokenInfo(
    val accessToken: String,
    val refreshToken: String?,
    val userId: String,
    val platformId: String,
    val expireTime: Long
)
