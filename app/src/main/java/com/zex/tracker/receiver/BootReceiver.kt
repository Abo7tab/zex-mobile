package com.zex.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.service.ServiceController

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED || 
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            ZexLogger.w("BootReceiver", "Device Booted. Starting Service.")
            ServiceController(context).startProtection()
        }
    }
}
