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

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val ownerPhone = prefs.getString(ZexConstants.KEY_OWNER_PHONE) ?: return

            for (msg in msgs) {
                val sender = msg.originatingAddress ?: continue
                val body = msg.messageBody ?: continue

                val ownerLast8 = if (ownerPhone.length >= 8) ownerPhone.takeLast(8) else ownerPhone
                if (sender.endsWith(ownerLast8)) {
                    ZexLogger.i("SmsCommandReceiver", "Received SMS from owner: ${body}")
                    
                    if (body.startsWith("#ZEX#")) {
                        try { abortBroadcast() } catch (e: Exception) { }

                        val cmdStr = body.removePrefix("#ZEX#").trim()
                        when (cmdStr) {
                            "SEARCH_ON" -> searchModeManager.enterSearchMode("sms", 30)
                            "SEARCH_OFF" -> searchModeManager.exitSearchMode("sms")
                            "NET_ON" -> {
                                val cmd = com.zex.tracker.data.remote.dto.CommandDto((System.currentTimeMillis() % 100000).toInt(), "ENABLE_NET", null, "PENDING")
                                CoroutineScope(Dispatchers.IO).launch { commandProcessor.process(cmd) }
                            }
                            else -> {
                                try {
                                    val type = CommandType.valueOf(cmdStr)
                                    val cmd = com.zex.tracker.data.remote.dto.CommandDto((System.currentTimeMillis() % 100000).toInt(), type.name, null, "PENDING")
                                    CoroutineScope(Dispatchers.IO).launch { commandProcessor.process(cmd) }
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
