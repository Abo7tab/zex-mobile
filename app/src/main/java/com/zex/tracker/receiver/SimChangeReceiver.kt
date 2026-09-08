package com.zex.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.data.repository.DeviceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SimChangeReceiver : BroadcastReceiver() {
    @Inject lateinit var deviceRepo: DeviceRepository
    @Inject lateinit var prefs: SecurePrefs

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.SIM_STATE_CHANGED") {
            val state = intent.getStringExtra("ss")
            if (state == "LOADED" || state == "READY") {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        val currentIccid = try { tm.simSerialNumber } catch(e: Exception) { null }
                        val storedIccid = prefs.getString("original_iccid")
                        
                        if (storedIccid.isNullOrEmpty() && !currentIccid.isNullOrEmpty()) {
                            prefs.putString("original_iccid", currentIccid)
                        } else if (!currentIccid.isNullOrEmpty() && currentIccid != storedIccid) {
                            ZexLogger.w("SimChangeReceiver", "SIM card changed! Sending alert.")
                            prefs.putString("original_iccid", currentIccid)
                            deviceRepo.sendAlert(com.zex.tracker.data.remote.dto.AlertRequest(
                                device_uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "",
                                type = "SIM_CHANGED",
                                message = "A new SIM card was inserted. ICCID: $currentIccid"
                            ))
                        }
                    } catch (e: Exception) {
                        ZexLogger.e("SimChangeReceiver", "Failed to process SIM change", e)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
