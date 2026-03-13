package com.easycampus.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["platformId"], unique = true)]
)
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val platformId: String,
    val platformType: String,
    val username: String,
    val encryptedPassword: String,
    val authToken: String?,
    val refreshToken: String?,
    val isActive: Boolean,
    val lastLoginTime: Long,
    val createdAt: Long
)
