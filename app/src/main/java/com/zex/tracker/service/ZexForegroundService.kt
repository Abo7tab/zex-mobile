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

    companion object {
        var isRunning = false
        fun startService(context: Context) {
            val intent = Intent(context, ZexForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        fun stopService(context: Context) {
            context.stopService(Intent(context, ZexForegroundService::class.java))
        }
    }

    @Inject lateinit var locationTracker: LocationTracker
    @Inject lateinit var deviceRepo: DeviceRepository
    @Inject lateinit var firebaseListener: FirebaseCommandListener
    @Inject lateinit var scheduler: Scheduler
    @Inject lateinit var searchModeManager: SearchModeManager

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    private var wakeLock: android.os.PowerManager.WakeLock? = null
    
    override fun onCreate() {
        super.onCreate()
        
        // Self-Healing Crash Handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            ZexLogger.e("ZexForegroundService", "CRASH DETECTED. Restarting...", exception)
            val intent = Intent(applicationContext, ZexForegroundService::class.java)
            val pendingIntent = android.app.PendingIntent.getService(
                applicationContext, 1, intent, 
                android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pendingIntent)
            defaultHandler?.uncaughtException(thread, exception)
            Runtime.getRuntime().exit(0)
        }
        
        isRunning = true
        ZexLogger.i("ZexForegroundService", "Service Created")
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "ZEX:ForegroundWakeLock").apply {
            setReferenceCounted(false)
            acquire()
        }
        
        startForeground(1001, createNotification())
        
        firebaseListener.startListening()
        scheduler.scheduleHourlyChecks()
        
        // Initial boot/start check
        scope.launch { searchModeManager.checkOwnerSearching() }
        startPeriodicHeartbeat()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ZexLogger.i("ZexForegroundService", "onStartCommand")
        
        wakeLock?.acquire()
        
        // Update notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, createNotification())

        manageTracking()
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        ZexLogger.w("ZexForegroundService", "Task removed by user. Scheduling instant restart...")
        val restartIntent = Intent(applicationContext, ZexForegroundService::class.java)
        val pendingIntent = android.app.PendingIntent.getService(
            applicationContext, 1, restartIntent, 
            android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pendingIntent)
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
            val chan = NotificationChannel(channelId, "خدمة الأمان والحماية", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chan)
        }
        
        val text = when {
            ServiceController.isStolen -> "وضع السرقة مفعل — ZEX Military"
            ServiceController.isSearching -> "وضع البحث مفعل — ZEX Military"
            else -> "نظام حماية الهاتف يعمل في الخلفية"
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("ZEX Military - تتبع الهاتف")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        job.cancel()
        
        try {
            wakeLock?.let {
                if (it.isHeld) it.release()
            }
        } catch (e: Exception) {
            ZexLogger.e("ZexForegroundService", "Failed to release wake lock", e)
        }
        
        locationTracker.stopContinuous()
        firebaseListener.stopListening()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            stopForeground(true)
        }
        
        ZexLogger.w("ZexForegroundService", "Service Destroyed")
        sendBroadcast(Intent("com.zex.tracker.REVIVE_SERVICE"))
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
