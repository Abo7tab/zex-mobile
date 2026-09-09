package com.zex.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.domain.model.Command
import com.zex.tracker.domain.model.CommandStatus
import com.zex.tracker.domain.model.CommandType
import com.zex.tracker.service.CommandProcessor
import com.zex.tracker.security.SearchModeManager
import com.zex.tracker.security.location.LocationTracker
import android.telephony.SmsManager
import com.zex.tracker.core.utils.BatteryUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SmsCommandReceiver : BroadcastReceiver() {

    @Inject lateinit var prefs: SecurePrefs
    @Inject lateinit var commandProcessor: CommandProcessor
    @Inject lateinit var searchModeManager: SearchModeManager
    @Inject lateinit var locationTracker: LocationTracker

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val storedOwnerPhone = prefs.getString(ZexConstants.KEY_OWNER_PHONE)

            if (storedOwnerPhone.isNullOrEmpty()) {
                ZexLogger.w("SmsCommandReceiver", "No owner phone number stored in SecurePrefs. Rejecting incoming SMS command for security.")
                return
            }

            for (msg in msgs) {
                val sender = msg.originatingAddress ?: continue
                val body = msg.messageBody ?: continue

                val sanitizedSender = sender.replace(Regex("\\D"), "")
                val sanitizedOwner = storedOwnerPhone.replace(Regex("\\D"), "")
                val ownerLast8 = if (sanitizedOwner.length >= 8) sanitizedOwner.takeLast(8) else sanitizedOwner
                
                val isAuthorized = sanitizedSender.endsWith(ownerLast8)

                if (isAuthorized) {
                    ZexLogger.i("SmsCommandReceiver", "Received authorized SMS: ${body}")
                    
                    if (body.startsWith("#ZEX#")) {
                        try { abortBroadcast() } catch (e: Exception) { }

                        val cmdStr = body.removePrefix("#ZEX#").trim()
                        when (cmdStr) {
                            "LOCATE" -> {
                                val pendingResult = goAsync()
                                CoroutineScope(Dispatchers.IO).launch { 
                                    try {
                                        val location = locationTracker.getCurrentLocation()
                                        if (location != null) {
                                            val batteryLevel = BatteryUtils.getBatteryLevel(context)
                                            val mapsUrl = "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                                            val smsBody = "ZEX Tracker: $mapsUrl (Battery: $batteryLevel%)"
                                            SmsManager.getDefault().sendTextMessage(sender, null, smsBody, null, null)
                                            ZexLogger.i("SmsCommandReceiver", "Sent LOCATE reply to $sender")
                                        } else {
                                            SmsManager.getDefault().sendTextMessage(sender, null, "ZEX Tracker: Location unavailable. GPS might be off.", null, null)
                                        }
                                    } catch (e: Exception) {
                                        ZexLogger.e("SmsCommandReceiver", "Failed to handle LOCATE command", e)
                                    } finally {
                                        pendingResult.finish()
                                    }
                                }
                            }
                            "SEARCH_ON" -> searchModeManager.enterSearchMode("sms", 30)
                            "SEARCH_OFF" -> searchModeManager.exitSearchMode("sms")
                            "NET_ON" -> {
                                val cmd = com.zex.tracker.data.remote.dto.CommandDto((System.currentTimeMillis() % 100000).toInt(), "ENABLE_NET", null, "PENDING")
                                val pendingResult = goAsync()
                                CoroutineScope(Dispatchers.IO).launch { 
                                    try { commandProcessor.process(cmd) } finally { pendingResult.finish() }
                                }
                            }
                            else -> {
                                try {
                                    val type = CommandType.valueOf(cmdStr)
                                    val cmd = com.zex.tracker.data.remote.dto.CommandDto((System.currentTimeMillis() % 100000).toInt(), type.name, null, "PENDING")
                                    val pendingResult = goAsync()
                                    CoroutineScope(Dispatchers.IO).launch { 
                                        try { commandProcessor.process(cmd) } finally { pendingResult.finish() }
                                    }
                                } catch (e: Exception) {
                                    ZexLogger.w("SmsCommandReceiver", "Invalid SMS command type: ${cmdStr}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
