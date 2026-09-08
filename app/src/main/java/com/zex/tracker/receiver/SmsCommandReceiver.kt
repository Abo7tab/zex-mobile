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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SmsCommandReceiver : BroadcastReceiver() {

    @Inject lateinit var prefs: SecurePrefs
    @Inject lateinit var commandProcessor: CommandProcessor

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            val ownerPhone = prefs.getString(ZexConstants.KEY_OWNER_PHONE) ?: return

            for (msg in msgs) {
                val sender = msg.originatingAddress ?: continue
                val body = msg.messageBody ?: continue

                // Check if sender matches last 8 digits of owner phone
                val ownerLast8 = if (ownerPhone.length >= 8) ownerPhone.takeLast(8) else ownerPhone
                if (sender.endsWith(ownerLast8)) {
                    ZexLogger.i("SmsCommandReceiver", "Received SMS from owner: $body")
                    
                    if (body.startsWith("#ZEX#")) {
                        try {
                            // abort broadcast to hide sms from inbox
                            abortBroadcast()
                        } catch (e: Exception) {
                            ZexLogger.w("SmsCommandReceiver", "Could not abort SMS broadcast", e)
                        }

                        val cmdTypeStr = body.removePrefix("#ZEX#").trim()
                        try {
                            val type = CommandType.valueOf(cmdTypeStr)
                            val cmd = Command(
                                id = (System.currentTimeMillis() % 100000).toInt(),
                                type = type,
                                status = CommandStatus.PENDING
                            )
                            commandProcessor.process(cmd)
                        } catch (e: Exception) {
                            ZexLogger.w("SmsCommandReceiver", "Invalid SMS command type: $cmdTypeStr")
                        }
                    }
                }
            }
        }
    }
}

