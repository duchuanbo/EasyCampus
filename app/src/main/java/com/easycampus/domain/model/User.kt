package com.easycampus.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String?,
    val avatarUrl: String?,
    val createdAt: Long,
    val lastLoginAt: Long
)

data class Account(
    val id: String,
    val platformId: String,
    val platformName: String,
    val username: String,
    val isActive: Boolean,
    val lastLoginTime: Long,
    val createdAt: Long
)

enum class PlatformType {
    KETANGPAI,
    YUKETANG,
    CHANGJIANG,
    CHANGKE
}

data class Platform(
    val id: String,
    val type: PlatformType,
    val name: String,
    val iconRes: Int,
    val baseUrl: String,
    val isEnabled: Boolean
)
