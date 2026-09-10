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
            action == "android.intent.action.QUICKBOOT_POWERON") {
            ZexLogger.w("BootReceiver", "Device Booted or Package Replaced. Starting Service.")
            com.zex.tracker.service.ZexForegroundService.startService(context)
        }
    }
}
