package com.zex.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.service.ServiceController

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED || 
            action == "android.intent.action.MY_PACKAGE_REPLACED" ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == "com.zex.tracker.RESTART_SERVICE") {
            ZexLogger.w("BootReceiver", "Boot or Restart triggered. Starting Service.")
            com.zex.tracker.service.ZexForegroundService.startService(context)
            
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.zex.tracker.service.ZexWatchdogWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
                .build()
            androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "ZexWatchdogWorker",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
