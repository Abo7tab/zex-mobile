package com.zex.tracker.security

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsHandshakeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SecurePrefs
) {
    @SuppressLint("MissingPermission")
    fun startHandshake(devicePhoneNumber: String?) {
        val hasPassed = prefs.getBoolean(ZexConstants.KEY_SMS_HANDSHAKE_PASSED, false)
        if (hasPassed) return

        val targetNumber = devicePhoneNumber?.takeIf { it.isNotBlank() } ?: return
        val timestamp = System.currentTimeMillis()
        val testMessage = "#ZEX#SELFTEST#$timestamp"
        
        prefs.putString("sms_handshake_error", null)
        
        try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val activeSubscriptions = subscriptionManager.activeSubscriptionInfoList
            
            if (activeSubscriptions != null && activeSubscriptions.isNotEmpty()) {
                activeSubscriptions.forEach { subInfo ->
                    try {
                        val smsManager = if (android.os.Build.VERSION.SDK_INT >= 31) {
                            context.getSystemService(SmsManager::class.java).createForSubscriptionId(subInfo.subscriptionId)
                        } else {
                            SmsManager.getSmsManagerForSubscriptionId(subInfo.subscriptionId)
                        }
                        smsManager.sendTextMessage(targetNumber, null, testMessage, null, null)
                        ZexLogger.i("SmsHandshake", "Sent self-test SMS via SIM ${subInfo.subscriptionId}")
                    } catch (e: Exception) {
                        ZexLogger.e("SmsHandshake", "Failed to send self-test SMS on SIM ${subInfo.subscriptionId}", e)
                    }
                }
            } else {
                // Fallback to default SMS manager
                val smsManager = if (android.os.Build.VERSION.SDK_INT >= 31) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    SmsManager.getDefault()
                }
                smsManager.sendTextMessage(targetNumber, null, testMessage, null, null)
            }
            
        } catch (e: Exception) {
            ZexLogger.e("SmsHandshake", "Handshake initiation failed", e)
            prefs.putString("sms_handshake_error", e.localizedMessage ?: "Failed to initiate SMS")
        }
    }
}

