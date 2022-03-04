package com.albar.runningtracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

// compile time Injected
@HiltAndroidApp
class BaseApplication : Application() {

    // Creating timber
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}