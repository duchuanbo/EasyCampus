package com.easycampus

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EasyCampusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application initialization
    }
}
