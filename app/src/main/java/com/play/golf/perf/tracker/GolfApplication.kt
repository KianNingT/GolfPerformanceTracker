package com.play.golf.perf.tracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Application entry point.
 * [@HiltAndroidApp] triggers Hilt's code generation and sets up
 * the application-level dependency container.
 */
@HiltAndroidApp
class GolfApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}