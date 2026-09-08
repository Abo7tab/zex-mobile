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
import com.zex.tracker.data.repository.DeviceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ZexForegroundService : Service() {

    @Inject lateinit var locationTracker: LocationTracker
    @Inject lateinit var deviceRepo: DeviceRepository
    @Inject lateinit var firebaseListener: FirebaseCommandListener

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    override fun onCreate() {
        super.onCreate()
        ZexLogger.i("ZexForegroundService", "Service Created")
        startForeground(1001, createNotification())
        
        firebaseListener.startListening()
        startPeriodicHeartbeat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ZexLogger.i("ZexForegroundService", "onStartCommand")
        manageTracking()
        return START_STICKY
    }

    private fun manageTracking() {
        if (ServiceController.isTracking || ServiceController.isStolen) {
            locationTracker.startContinuous(ServiceController.trackingInterval) { loc ->
                scope.launch {
                    deviceRepo.sendLocation(loc)
                }
            }
        } else {
            locationTracker.stopContinuous()
        }
    }

    private fun startPeriodicHeartbeat() {
        scope.launch {
            while (isActive) {
                delay(if (ServiceController.isStolen) 30000L else 15 * 60 * 1000L)
                try {
                    deviceRepo.sendHeartbeat()
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
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("ZEX Protection")
            .setContentText("Protection is active")
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
        // Try to revive via intent broadcast or worker if killed by OS
        sendBroadcast(Intent("com.zex.tracker.REVIVE_SERVICE"))
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
