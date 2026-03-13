package com.easycampus.data.local.dao

import androidx.room.*
import com.easycampus.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE platformId = :platformId LIMIT 1")
    fun getAccountByPlatform(platformId: String): Flow<AccountEntity?>

    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveAccounts(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE platformId = :platformId")
    suspend fun deleteAccountByPlatform(platformId: String)

    @Query("UPDATE accounts SET isActive = :isActive WHERE platformId = :platformId")
    suspend fun updateAccountActiveStatus(platformId: String, isActive: Boolean)

    @Query("SELECT EXISTS(SELECT 1 FROM accounts WHERE platformId = :platformId)")
    suspend fun accountExists(platformId: String): Boolean
}
