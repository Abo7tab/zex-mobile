package com.zex.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.telephony.SubscriptionManager
import android.annotation.SuppressLint
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.data.repository.DeviceRepository
import com.zex.tracker.security.location.LocationTracker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SimStateReceiver : BroadcastReceiver() {

    @Inject lateinit var prefs: SecurePrefs
    @Inject lateinit var deviceRepo: DeviceRepository
    @Inject lateinit var locationTracker: LocationTracker

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.SIM_STATE_CHANGED") {
            checkSimState(context)
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkSimState(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ZexLogger.w("SimStateReceiver", "Missing READ_PHONE_STATE permission")
            return
        }

        try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
            
            if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.isNotEmpty()) {
                val currentIccid = activeSubscriptionInfoList[0].iccId
                val savedIccid = prefs.getString("saved_iccid")

                if (savedIccid.isNullOrEmpty()) {
                    prefs.putString("saved_iccid", currentIccid)
                    ZexLogger.i("SimStateReceiver", "Saved initial SIM ICCID")
                } else if (savedIccid != currentIccid) {
                    ZexLogger.w("SimStateReceiver", "SIM CHANGE DETECTED!")
                    prefs.putString("saved_iccid", currentIccid)
                    handleSimChange(context, activeSubscriptionInfoList[0].number ?: "Unknown Number")
                }
            }
        } catch (e: Exception) {
            ZexLogger.e("SimStateReceiver", "Failed to check SIM state", e)
        }
    }

    private fun handleSimChange(context: Context, newNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Notify Backend
                deviceRepo.reportSimChange(newNumber)
                
                // 2. Send Emergency SMS
                val ownerPhone = prefs.getString(com.zex.tracker.core.constants.ZexConstants.KEY_OWNER_PHONE)
                if (!ownerPhone.isNullOrEmpty()) {
                    val location = locationTracker.getCurrentLocation()
                    val smsBody = if (location != null) {
                        "ZEX Alert: SIM Card Changed! New Number: $newNumber. Location: https://maps.google.com/?q=${location.latitude},${location.longitude}"
                    } else {
                        "ZEX Alert: SIM Card Changed! New Number: $newNumber."
                    }
                    
                    val smsManager = android.telephony.SmsManager.getDefault()
                    smsManager.sendTextMessage(ownerPhone, null, smsBody, null, null)
                    ZexLogger.i("SimStateReceiver", "Sent emergency SIM change SMS to owner")
                }
            } catch (e: Exception) {
                ZexLogger.e("SimStateReceiver", "Failed to handle SIM change", e)
            }
        }
    }
}
