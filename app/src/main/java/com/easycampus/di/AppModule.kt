package com.easycampus.di

import android.content.Context
import androidx.room.Room
import com.easycampus.data.local.TokenManager
import com.easycampus.data.local.database.EasyCampusDatabase
import com.easycampus.data.local.datastore.SettingsDataStore
import com.easycampus.data.remote.guet.GuetAuthService
import com.easycampus.data.remote.guet.GuetCheckInService
import com.easycampus.data.remote.guet.GuetCourseService
import com.easycampus.data.repository.*
import com.easycampus.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EasyCampusDatabase {
        return Room.databaseBuilder(
            context,
            EasyCampusDatabase::class.java,
            "easycampus_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore {
        return SettingsDataStore(context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(settingsDataStore: SettingsDataStore): SettingsRepository {
        return SettingsRepositoryImpl(settingsDataStore)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        database: EasyCampusDatabase,
        @ApplicationContext context: Context
    ): UserRepository {
        return UserRepositoryImpl(database.accountDao(), context)
    }

    @Provides
    @Singleton
    fun provideCourseRepository(database: EasyCampusDatabase): CourseRepository {
        return CourseRepositoryImpl(database.courseDao())
    }

    @Provides
    @Singleton
    fun provideCheckInRepository(database: EasyCampusDatabase): CheckInRepository {
        return CheckInRepositoryImpl(database.checkInDao())
    }

    // ==================== GUET Services ====================

    @Provides
    @Singleton
    fun provideGuetAuthService(): GuetAuthService {
        return GuetAuthService()
    }

    @Provides
    @Singleton
    fun provideGuetCourseService(): GuetCourseService {
        return GuetCourseService()
    }

    @Provides
    @Singleton
    fun provideGuetCheckInService(@ApplicationContext context: Context): GuetCheckInService {
        return GuetCheckInService(context)
    }

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideGuetRepository(
        authService: GuetAuthService,
        courseService: GuetCourseService,
        checkInService: GuetCheckInService,
        tokenManager: TokenManager
    ): GuetRepositoryImpl {
        return GuetRepositoryImpl(
            authService,
            courseService,
            checkInService,
            tokenManager
        )
    }
}
