package com.zex.tracker.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.zex.tracker.core.logging.ZexLogger

class ZexDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        ZexLogger.i("DeviceAdmin", "Device Admin Enabled")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        ZexLogger.w("DeviceAdmin", "Device Admin Disable Requested")
        return "Disabling device admin will reduce anti-theft capabilities."
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        ZexLogger.w("DeviceAdmin", "Device Admin Disabled")
    }
}
