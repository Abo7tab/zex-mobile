package com.zex.tracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.zex.tracker.R
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.remote.firebase.FirebaseCommandListener
import com.zex.tracker.security.location.LocationTracker
import com.zex.tracker.security.Scheduler
import com.zex.tracker.security.SearchModeManager
import com.zex.tracker.data.repository.DeviceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ZexForegroundService : Service() {

    @Inject lateinit var locationTracker: LocationTracker
    @Inject lateinit var deviceRepo: DeviceRepository
    @Inject lateinit var firebaseListener: FirebaseCommandListener
    @Inject lateinit var scheduler: Scheduler
    @Inject lateinit var searchModeManager: SearchModeManager

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    override fun onCreate() {
        super.onCreate()
        ZexLogger.i("ZexForegroundService", "Service Created")
        startForeground(1001, createNotification())
        
        firebaseListener.startListening()
        scheduler.scheduleHourlyChecks()
        
        // Initial boot/start check
        scope.launch { searchModeManager.checkOwnerSearching() }
        startPeriodicHeartbeat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ZexLogger.i("ZexForegroundService", "onStartCommand")
        
        // Update notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, createNotification())

        manageTracking()
        return START_STICKY
    }

    private fun manageTracking() {
        if (ServiceController.isTracking || ServiceController.isSearching || ServiceController.isStolen) {
            val interval = ServiceController.trackingInterval.coerceAtLeast(10000L)
            locationTracker.startContinuous(interval) { loc ->
                scope.launch { deviceRepo.sendLocation(loc) }
            }
        } else {
            locationTracker.stopContinuous()
        }
    }

    private fun startPeriodicHeartbeat() {
        scope.launch {
            while (isActive) {
                val delayMs = if (ServiceController.isSearching || ServiceController.isStolen) 30000L else 15 * 60 * 1000L
                delay(delayMs)
                try {
                    searchModeManager.checkOwnerSearching()
                } catch (e: Exception) {
                    ZexLogger.e("ZexForegroundService", "Periodic heartbeat failed", e)
                }
            }
        }
    }

    private fun createNotification(): android.app.Notification {
        val channelId = "zex_protection_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, "ZEX Protection", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }
        
        val text = when {
            ServiceController.isStolen -> "Stolen mode active"
            ServiceController.isSearching -> "Search mode active"
            else -> "Protection active"
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("ZEX")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        locationTracker.stopContinuous()
        firebaseListener.stopListening()
        ZexLogger.w("ZexForegroundService", "Service Destroyed")
        sendBroadcast(Intent("com.zex.tracker.REVIVE_SERVICE"))
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
