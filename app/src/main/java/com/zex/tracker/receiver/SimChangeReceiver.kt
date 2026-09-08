package com.zex.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.repository.DeviceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SimChangeReceiver : BroadcastReceiver() {

    @Inject lateinit var deviceRepo: DeviceRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.SIM_STATE_CHANGED") {
            ZexLogger.w("SimChangeReceiver", "SIM State Changed!")
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simState = tm.simState
            if (simState == TelephonyManager.SIM_STATE_READY) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        deviceRepo.sendAlert(mapOf(
                            "type" to "SIM_CHANGED",
                            "message" to "New SIM detected."
                        ))
                    } catch (e: Exception) {
                        ZexLogger.e("SimChangeReceiver", "Failed to send SIM alert", e)
                    }
                }
            }
        }
    }
}
