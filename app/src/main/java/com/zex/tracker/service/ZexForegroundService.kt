package com.zex.tracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
        @Volatile var isRunning = false
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
    @Inject lateinit var bleManager: com.zex.tracker.security.ble.ZexBleManager

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    private var wakeLock: android.os.PowerManager.WakeLock? = null
    
    override fun onCreate() {
        super.onCreate()
        
        
        isRunning = true
        ZexLogger.i("ZexForegroundService", "Service Created")
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "ZEX:ForegroundWakeLock").apply {
            setReferenceCounted(false)
        }
        
        startForeground(1001, createNotification())
        
        firebaseListener.startListening()
        scheduler.scheduleHourlyChecks()
        
        // Initial boot/start check
        scope.launch { searchModeManager.checkOwnerSearching() }
        startPeriodicHeartbeat()
        startHealthMonitor()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ZexLogger.i("ZexForegroundService", "onStartCommand")
        
        // Update notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, createNotification())

        manageTracking()
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        ZexLogger.w("ZexForegroundService", "Task removed. Scheduling instant restart...")
        val restartIntent = Intent(applicationContext, com.zex.tracker.receiver.BootReceiver::class.java).apply {
            action = "com.zex.tracker.RESTART_SERVICE"
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            applicationContext, 1, restartIntent, 
            android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        try {
            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pendingIntent)
        } catch (e: Exception) {
            alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, pendingIntent)
        }
    }

    private fun manageTracking() {
        if (ServiceController.isTracking || ServiceController.isSearching || ServiceController.isStolen) {
            val interval = ServiceController.trackingInterval.coerceAtLeast(10000L)
            locationTracker.startContinuous(interval) { loc ->
                scope.launch {
                    try {
                        wakeLock?.acquire(60000L) // 1 minute timeout
                        deviceRepo.sendLocation(loc)
                    } finally {
                        try {
                            if (wakeLock?.isHeld == true) wakeLock?.release()
                        } catch (e: Exception) {}
                    }
                }
            }
        } else {
            locationTracker.stopContinuous()
        }

        bleManager.startScanning()
        if (ServiceController.isStolen) bleManager.startAdvertising()
        else bleManager.stopAdvertising()
    }

    private fun startPeriodicHeartbeat() {
        scope.launch {
            while (isActive) {
                if (ServiceController.isSearching || ServiceController.isStolen) {
                    try {
                        searchModeManager.checkOwnerSearching()
                    } catch (e: Exception) {
                        ZexLogger.e("ZexForegroundService", "Periodic heartbeat failed", e)
                    }
                    delay(30000L)
                } else {
                    delay(60000L) // Wait a minute before checking mode again. Hourly sync is via AlarmManager.
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

    private fun startHealthMonitor() {
        scope.launch {
            while (isActive) {
                checkHealthAndNotify()
                kotlinx.coroutines.delay(30_000) // Check every 30 seconds
            }
        }
    }

    private fun checkHealthAndNotify() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        val caps = connectivityManager.getNetworkCapabilities(network)
        val hasInternet = caps?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        val hasGps = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        val hasBluetooth = bluetoothManager.adapter?.isEnabled == true

        val warnings = mutableListOf<String>()
        if (!hasInternet) warnings.add("الإنترنت معطل! اضغط لتشغيله لحماية الجهاز من الضياع.")
        if (!hasGps) warnings.add("الموقع (GPS) معطل! شغّله فوراً.")
        if (!hasBluetooth) warnings.add("البلوتوث معطل! شغّله لتفعيل الرادار الميداني.")

        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (warnings.isNotEmpty()) {
            val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder = androidx.core.app.NotificationCompat.Builder(this, "zex_health_channel")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle("⚠️ ZEX Military: حماية معطلة!")
                .setContentText(warnings.joinToString(" | "))
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(warnings.joinToString("\n")))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_MAX)
                .setOngoing(true) // Sticky
                .setContentIntent(pendingIntent)
                
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel("zex_health_channel", "Health Warnings", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "تحذيرات أمنية هامة"
                }
                notifManager.createNotificationChannel(channel)
            }
            notifManager.notify(1002, builder.build())
        } else {
            notifManager.cancel(1002)
        }
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
        bleManager.stopScanning()
        bleManager.stopAdvertising()
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
