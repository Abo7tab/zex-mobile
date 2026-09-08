package com.zex.tracker.security

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.receiver.ZexDeviceAdminReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LockManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val compName = ComponentName(context, ZexDeviceAdminReceiver::class.java)

    fun isDeviceAdminActive(): Boolean = dpm.isAdminActive(compName)

    fun lockNow() {
        try {
            if (isDeviceAdminActive()) {
                dpm.lockNow()
                ZexLogger.i("LockManager", "Device locked via admin")
            } else {
                ZexLogger.w("LockManager", "Cannot lock: Device admin not active")
            }
        } catch (e: Exception) {
            ZexLogger.e("LockManager", "Lock failed", e)
        }
    }
}
