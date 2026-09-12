package com.zex.tracker.service

import android.content.Context
import android.content.Intent
import android.os.Build
import com.zex.tracker.core.logging.ZexLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceController @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        @Volatile var isTracking: Boolean = false
        @Volatile var trackingInterval: Long = 30000L
        @Volatile var isScreaming: Boolean = false
        @Volatile var isStolen: Boolean = false
        @Volatile var isSearching: Boolean = false
    }

    fun startProtection() {
        try {
            val intent = Intent(context, ZexForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            ZexLogger.i("ServiceController", "Requested Foreground Service start")
        } catch (e: Exception) {
            ZexLogger.e("ServiceController", "Failed to start service", e)
        }
    }

    fun stopProtection() {
        try {
            context.stopService(Intent(context, ZexForegroundService::class.java))
            ZexLogger.i("ServiceController", "Requested Foreground Service stop")
        } catch (e: Exception) {
            ZexLogger.e("ServiceController", "Failed to stop service", e)
        }
    }
}
