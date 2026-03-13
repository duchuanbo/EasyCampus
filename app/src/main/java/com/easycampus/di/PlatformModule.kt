package com.easycampus.di

import com.easycampus.data.remote.platform.PlatformAdapter
import com.easycampus.data.remote.platform.PlatformAdapterFactory
import com.easycampus.data.remote.platform.KetangpaiAdapter
import com.easycampus.data.remote.platform.YuketangAdapter
import com.easycampus.data.remote.platform.ChangjiangAdapter
import com.easycampus.data.remote.platform.ChangkeAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlatformModule {

    @Provides
    @Singleton
    fun provideKetangpaiAdapter(): PlatformAdapter {
        return KetangpaiAdapter()
    }

    @Provides
    @Singleton
    fun provideYuketangAdapter(): PlatformAdapter {
        return YuketangAdapter()
    }

    @Provides
    @Singleton
    fun provideChangjiangAdapter(): PlatformAdapter {
        return ChangjiangAdapter()
    }

    @Provides
    @Singleton
    fun provideChangkeAdapter(): PlatformAdapter {
        return ChangkeAdapter()
    }

    @Provides
    @Singleton
    fun providePlatformAdapterFactory(
        ketangpaiAdapter: KetangpaiAdapter,
        yuketangAdapter: YuketangAdapter,
        changjiangAdapter: ChangjiangAdapter,
        changkeAdapter: ChangkeAdapter
    ): PlatformAdapterFactory {
        val adapters = mapOf(
            "ketangpai" to ketangpaiAdapter,
            "yuketang" to yuketangAdapter,
            "changjiang" to changjiangAdapter,
            "changke" to changkeAdapter
        )
        return PlatformAdapterFactory(adapters)
    }
}
