package com.zex.tracker.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.domain.model.Command
import com.zex.tracker.domain.model.CommandStatus
import com.zex.tracker.domain.model.CommandType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@AndroidEntryPoint
class ZexFcmService : FirebaseMessagingService() {

    @Inject lateinit var prefs: SecurePrefs
    @Inject lateinit var commandProcessor: CommandProcessor

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        ZexLogger.i("FCM", "New FCM token generated: $token")
        // Note: Store local, sync with backend on next heartbeat if endpoint doesn't exist yet
        prefs.putString("fcm_token", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        ZexLogger.i("FCM", "Received FCM Message")
        
        try {
            val data = message.data
            val cmdId = data["id"]?.toIntOrNull() ?: return
            val typeStr = data["type"] ?: return
            val type = CommandType.valueOf(typeStr)
            
            val cmd = com.zex.tracker.data.remote.dto.CommandDto(cmdId, type.name, data, "PENDING")
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch { commandProcessor.process(cmd) }
        } catch (e: Exception) {
            ZexLogger.e("FCM", "Failed to process FCM data", e)
        }
    }
}

