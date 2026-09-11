package com.zex.tracker.security

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.receiver.ZexDeviceAdminReceiver

class DeviceAdminManager(private val context: Context) {
    
    private val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val component = ComponentName(context, ZexDeviceAdminReceiver::class.java)
    
    fun lockDevice() {
        try {
            if (dpm.isAdminActive(component)) {
                dpm.lockNow()
                ZexLogger.i("DeviceAdminManager", "Device locked successfully.")
            } else {
                ZexLogger.w("DeviceAdminManager", "Cannot lock device: Admin not active.")
            }
        } catch (e: Exception) {
            ZexLogger.e("DeviceAdminManager", "Failed to lock device", e)
        }
    }
    
    fun wipeData() {
        try {
            if (dpm.isAdminActive(component)) {
                ZexLogger.w("DeviceAdminManager", "Executing remote wipe...")
                dpm.wipeData(0)
            } else {
                ZexLogger.w("DeviceAdminManager", "Cannot wipe device: Admin not active.")
            }
        } catch (e: Exception) {
            ZexLogger.e("DeviceAdminManager", "Failed to wipe device", e)
        }
    }
}
