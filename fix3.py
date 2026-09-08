def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f: f.write(content)

def read_file(path):
    with open(path, 'r', encoding='utf-8') as f: return f.read()

# 1. FirebaseCommandListener missing import
fcl_path = 'app/src/main/java/com/zex/tracker/data/remote/firebase/FirebaseCommandListener.kt'
fcl = read_file(fcl_path)
if 'import kotlinx.coroutines.launch' not in fcl:
    fcl = fcl.replace('import javax.inject.Singleton', 'import javax.inject.Singleton\nimport kotlinx.coroutines.launch\nimport kotlinx.coroutines.CoroutineScope\nimport kotlinx.coroutines.Dispatchers')
    write_file(fcl_path, fcl)

# 2. SimChangeReceiver Map issue
scr_path = 'app/src/main/java/com/zex/tracker/receiver/SimChangeReceiver.kt'
scr = read_file(scr_path)
scr = scr.replace('deviceRepo.sendAlert(mapOf(', 'deviceRepo.sendAlert(com.zex.tracker.data.remote.dto.AlertRequest(\n                            device_uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "",\n                            type = "SIM_CHANGED",\n                            message = "A new SIM card was inserted into the device."\n                        )//')
# Fix any syntax that might be broken by this blind replace
if '//' in scr:
    scr = scr.replace('))//', '))')
    scr = scr.replace(')\n                        )//\n                            "type" to "SIM_CHANGED",', ')')
    scr = scr.replace(')\n                        )//\n                            "message" to "A new SIM card was inserted into the device."\n                        )', '')
# Re-write the file correctly
scr = """package com.zex.tracker.receiver

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
"""
write_file(scr_path, scr)

# 3. ZexApi, DeviceRepo, ScreamActivity API Call
api_path = 'app/src/main/java/com/zex/tracker/data/remote/api/ZexApi.kt'
api = read_file(api_path)
if 'stopScream' not in api:
    api = api.replace('suspend fun sendAlert', '@POST("devices/{device}/stop-scream")\n    suspend fun stopScream(@Path("device") deviceId: Long, @Body payload: Map<String, String>): Response<Unit>\n\n    suspend fun sendAlert')
    write_file(api_path, api)

repo_path = 'app/src/main/java/com/zex/tracker/data/repository/DeviceRepository.kt'
repo = read_file(repo_path)
if 'stopScream' not in repo:
    repo = repo.replace('suspend fun sendAlert', 'suspend fun stopScream(alarmSecret: String): ApiResult<Unit> = safeApiCall {\n        val deviceId = prefs.getLong("device_numeric_id", 0L)\n        api.stopScream(deviceId, mapOf("alarm_secret" to alarmSecret))\n    }\n\n    suspend fun sendAlert')
    write_file(repo_path, repo)

act_path = 'app/src/main/java/com/zex/tracker/ui/screens/scream/ScreamActivity.kt'
act = read_file(act_path)
if 'deviceRepo.stopScream' not in act:
    act = act.replace('// optional API fallback/sync', 'deviceRepo.stopScream(pinInput)')
    act = act.replace('class ScreamActivity : ComponentActivity() {', 'class ScreamActivity : ComponentActivity() {\n\n    @Inject lateinit var deviceRepo: com.zex.tracker.data.repository.DeviceRepository')
    write_file(act_path, act)
