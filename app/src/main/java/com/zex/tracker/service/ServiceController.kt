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
        var isTracking: Boolean = false
        var trackingInterval: Long = 30000L
        var isScreaming: Boolean = false
        var isStolen: Boolean = false
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
