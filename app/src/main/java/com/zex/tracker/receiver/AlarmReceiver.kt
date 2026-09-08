package com.zex.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.service.ServiceController

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ZexLogger.i("AlarmReceiver", "Reviving service via Alarm")
        ServiceController(context).startProtection()
    }
}
