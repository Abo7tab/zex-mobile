package com.zex.tracker

import android.app.Application
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.zex.tracker.core.logging.CrashReportingTree
import com.zex.tracker.core.logging.ZexLogger
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * ZexApplication
 * Main application class for ZEX Tracker. Initializes Hilt, WorkManager, and Logging.
 */
@HiltAndroidApp
class ZexApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }

        val version = BuildConfig.VERSION_NAME
        val sdk = Build.VERSION.SDK_INT
        val model = Build.MODEL

        ZexLogger.i("AppLifecycle", "ZEX App Started. Version: $version, Android SDK: $sdk, Model: $model")

        // Default uncaught exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            ZexLogger.crash("Uncaught exception in thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    /**
     * Provide configuration for WorkManager to use Hilt dependency injection.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
