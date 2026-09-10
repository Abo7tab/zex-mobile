package com.zex.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.annotation.SuppressLint
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
import com.zex.tracker.security.NetworkForcer
import com.zex.tracker.data.repository.DeviceRepository
import android.telephony.SmsManager
import com.zex.tracker.core.utils.BatteryUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SmsCommandReceiver : BroadcastReceiver() {

    @Inject lateinit var prefs: SecurePrefs
    @Inject lateinit var commandProcessor: CommandProcessor
    @Inject lateinit var searchModeManager: SearchModeManager
    @Inject lateinit var locationTracker: LocationTracker
    @Inject lateinit var networkForcer: NetworkForcer
    @Inject lateinit var deviceRepo: DeviceRepository
    @Inject lateinit var screamManager: com.zex.tracker.security.ScreamManager

    private fun sendReplySms(context: Context, to: String, message: String) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                val smsManager: SmsManager = try {
                    val subId = SubscriptionManager.getDefaultSmsSubscriptionId()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val baseSmsManager = context.getSystemService(SmsManager::class.java)
                        if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                            baseSmsManager.createForSubscriptionId(subId)
                        } else {
                            baseSmsManager
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                            @Suppress("DEPRECATION")
                            SmsManager.getSmsManagerForSubscriptionId(subId)
                        } else {
                            @Suppress("DEPRECATION")
                            SmsManager.getDefault()
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getDefault()
                    }
                } catch (e: Exception) {
                    ZexLogger.w("SmsCommandReceiver", "Failed to resolve subscription-specific SmsManager, falling back to default", e)
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                
                val sentPI = android.app.PendingIntent.getBroadcast(context, 0, android.content.Intent("SMS_SENT"), android.app.PendingIntent.FLAG_IMMUTABLE)
                val deliveredPI = android.app.PendingIntent.getBroadcast(context, 0, android.content.Intent("SMS_DELIVERED"), android.app.PendingIntent.FLAG_IMMUTABLE)
                
                try {
                    smsManager.sendTextMessage(to, null, message, sentPI, deliveredPI)
                    ZexLogger.i("SmsCommandReceiver", "Dispatched SMS to $to. Awaiting carrier confirmation.")
                } catch(e: Exception) {
                    ZexLogger.e("SmsCommandReceiver", "FATAL SMS DISPATCH ERROR", e)
                }
            } else {
                ZexLogger.e("SmsCommandReceiver", "SEND_SMS permission missing at runtime")
            }
        } catch (e: Exception) {
            ZexLogger.e("SmsCommandReceiver", "Failed to send SMS reply", e)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val storedOwnerPhone = prefs.getString(ZexConstants.KEY_OWNER_PHONE) ?: ""
            val alarmSecret = prefs.getString("alarm_secret")

            for (msg in msgs) {
                val sender = msg.originatingAddress ?: continue
                val rawBody = msg.messageBody?.trim() ?: continue
                val upper = rawBody.uppercase()
                if (!upper.startsWith("#ZEX#") && !upper.startsWith("ZEX#")) continue

                // Cleanly strip prefix whether it starts with # or not
                val cleanBody = upper.removePrefix("#ZEX#").removePrefix("ZEX#").trim().removeSuffix("#")
                val parts = cleanBody.split("#").map { it.trim() }
                var isAuthorized = false
                var cmdStr = ""

                val ownerPin = prefs.getString(ZexConstants.KEY_PIN_CODE)
                val isValidPin = parts.isNotEmpty() && (
                    (!alarmSecret.isNullOrEmpty() && parts[0] == alarmSecret) ||
                    (!ownerPin.isNullOrEmpty() && parts[0] == ownerPin)
                )

                if (isValidPin) {
                    if (parts.size == 1) {
                        isAuthorized = true
                        cmdStr = "SOS"
                    } else if (parts.size >= 2) {
                        isAuthorized = true
                        cmdStr = parts[1]
                    }
                }
                
                if (!isAuthorized && storedOwnerPhone.isNotEmpty()) {
                    val sanitizedSender = sender.replace(Regex("\\D"), "")
                    val sanitizedOwner = storedOwnerPhone.replace(Regex("\\D"), "")
                    val ownerLast8 = if (sanitizedOwner.length >= 8) sanitizedOwner.takeLast(8) else sanitizedOwner
                    if (sanitizedSender.endsWith(ownerLast8)) {
                        isAuthorized = true
                        cmdStr = parts.joinToString("#")
                        if (cmdStr.isEmpty()) cmdStr = "SOS"
                    }
                }

                if (!isAuthorized) {
                    ZexLogger.w("SmsCommandReceiver", "Unauthorized SMS command from $sender. Rejecting.")
                    sendReplySms(context, sender, "ZEX Error: Invalid PIN or Secret provided.")
                    continue
                }

                ZexLogger.i("SmsCommandReceiver", "Received authorized SMS: ${rawBody}")
                try { abortBroadcast() } catch (e: Exception) { }

                when (cmdStr) {
                    "SOS", "PANIC", "HELP", "LOCATE", "NET_ON" -> {
                        val pendingResult = goAsync()
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                if (cmdStr in listOf("SOS", "PANIC", "HELP")) {
                                    ZexLogger.i("SmsCommandReceiver", "Triggering SOS Emergency Routine")
                                    screamManager.startScream()
                                    com.zex.tracker.service.ServiceController.isStolen = true
                                    com.zex.tracker.service.ServiceController.isSearching = true
                                    com.zex.tracker.service.ServiceController.isScreaming = true
                                    prefs.putBoolean("isStolen", true)
                                }
                                
                                networkForcer.forceNetwork()
                                
                                @SuppressLint("MissingPermission")
                                var location = locationTracker.getCurrentLocation()
                                if (location == null) {
                                    // Fallback to Last Known Location
                                    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
                                    location = locationManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                                        ?: locationManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                                }
                                
                                if (location != null) {
                                    val batteryLevel = BatteryUtils.getBatteryLevel(context)
                                    val lat = location.latitude
                                    val lng = location.longitude
                                    val mapsUrl = "https://maps.google.com/?q=${lat},${lng}"
                                    val smsBody = "ZEX Alert: $mapsUrl (Battery: ${batteryLevel}%)"
                                    
                                    sendReplySms(context, sender, smsBody)
                                    
                                    try {
                                        deviceRepo.sendLocation(location)
                                    } catch (e: Exception) {
                                        ZexLogger.e("SmsCommandReceiver", "Failed to sync location to backend", e)
                                    }
                                } else {
                                    sendReplySms(context, sender, "ZEX Error: GPS location unavailable. Please enable Location/GPS.")
                                }
                            } catch (e: Exception) {
                                ZexLogger.e("SmsCommandReceiver", "Failed command routine", e)
                                sendReplySms(context, sender, "ZEX Error: ${e.message ?: "Failed to execute command"}")
                            } finally {
                                pendingResult.finish()
                            }
                        }
                    }
                    "SEARCH_ON" -> searchModeManager.enterSearchMode("sms", 30)
                    "SEARCH_OFF" -> searchModeManager.exitSearchMode("sms")
                    else -> {
                        try {
                            val type = CommandType.valueOf(cmdStr)
                            val cmd = com.zex.tracker.data.remote.dto.CommandDto((System.currentTimeMillis() % 100000).toInt(), type.name, null, "PENDING")
                            val pendingResult = goAsync()
                            CoroutineScope(Dispatchers.IO).launch { 
                                try { commandProcessor.process(cmd) } catch (e: Exception) {
                                    sendReplySms(context, sender, "ZEX Error: ${e.message ?: "Failed to execute command"}")
                                } finally { pendingResult.finish() }
                            }
                        } catch (e: Exception) {
                            ZexLogger.w("SmsCommandReceiver", "Invalid SMS command type: ${cmdStr}")
                            sendReplySms(context, sender, "ZEX Error: Invalid command type ${cmdStr}")
                        }
                    }
                }
            }
        }
    }
}