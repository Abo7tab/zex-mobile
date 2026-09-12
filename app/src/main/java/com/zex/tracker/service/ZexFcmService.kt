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

    private val job = kotlinx.coroutines.SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        ZexLogger.i("FCM", "New FCM token generated: $token")
        prefs.putString("fcm_token", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        ZexLogger.i("FCM", "Received FCM Message")
        
        val powerManager = getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
        val wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "ZEX:FCMWakeLock")
        wakeLock.acquire(20000L) // 20 seconds max
        
        val data = message.data
        val cmdId = data["id"]?.toIntOrNull()
        val typeStr = data["type"]
        
        if (cmdId == null || typeStr == null) {
            try { if (wakeLock.isHeld) wakeLock.release() } catch (e: Exception) {}
            return
        }
        
        val type = try {
            CommandType.valueOf(typeStr)
        } catch (e: IllegalArgumentException) {
            ZexLogger.w("FCM", "Unknown command type received: $typeStr")
            try { if (wakeLock.isHeld) wakeLock.release() } catch (e2: Exception) {}
            return
        }
        
        val cmd = com.zex.tracker.data.remote.dto.CommandDto(cmdId, type.name, data, "PENDING")
        
        scope.launch { 
            try {
                commandProcessor.process(cmd)
            } catch (e: Exception) {
                ZexLogger.e("FCM", "Failed to process FCM command", e)
            } finally {
                try { if (wakeLock.isHeld) wakeLock.release() } catch (e: Exception) {}
            }
        }
    }
}
