import os

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content.strip() + '\n')

def read_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

# Payloads.kt
payloads_path = r"app/src/main/java/com/zex/tracker/data/remote/dto/Payloads.kt"
p_data = read_file(payloads_path)
if 'AlertRequest' not in p_data:
    p_data += """
data class AlertRequest(
    val device_uid: String,
    val type: String,
    val message: String,
    val photo_url: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
"""
p_data = p_data.replace('val battery_level: Int', 'val battery_level: Int,\n    val fcm_token: String? = null')
write_file(payloads_path, p_data)

# ZexApi.kt
api_path = r"app/src/main/java/com/zex/tracker/data/remote/api/ZexApi.kt"
api_data = read_file(api_path)
api_data = api_data.replace('suspend fun getOwnerMe(): Response<AuthResponse>', 'suspend fun getOwnerMe(): Response<OwnerDto>')
api_data = api_data.replace('suspend fun sendAlert(@Body payload: Map<String, String>)', 'suspend fun sendAlert(@Body payload: AlertRequest)')
write_file(api_path, api_data)

# DeviceDto.kt
dto_path = r"app/src/main/java/com/zex/tracker/data/remote/dto/DeviceDto.kt"
dto_data = read_file(dto_path)
dto_data = dto_data.replace('val sim_iccid: String?', 'val sim_iccid: String?,\n    val fcm_token: String? = null')
if 'val alarm_secret: String?' not in dto_data:
    dto_data = dto_data.replace('val android_version: String', 'val android_version: String,\n    val alarm_secret: String? = null')
write_file(dto_path, dto_data)


# DeviceRepository.kt
repo_path = r"app/src/main/java/com/zex/tracker/data/repository/DeviceRepository.kt"
repo_data = read_file(repo_path)
repo_data = repo_data.replace('api.registerDevice(request)', 'api.registerDevice(request.copy(fcm_token = prefs.getString("fcm_token")))')
repo_data = repo_data.replace('battery_level = BatteryUtils.getBatteryLevel(context)', 'battery_level = BatteryUtils.getBatteryLevel(context),\n            fcm_token = prefs.getString("fcm_token")')

# Offline Room logic and UTC Time
repo_data = repo_data.replace('import javax.inject.Inject', 'import javax.inject.Inject\nimport com.zex.tracker.data.local.dao.LocationDao\nimport com.zex.tracker.data.local.entity.LocationEntity')
repo_data = repo_data.replace('private val prefs: SecurePrefs', 'private val prefs: SecurePrefs,\n    private val locationDao: LocationDao')

repo_data = repo_data.replace("""    suspend fun sendLocation(location: Location): ApiResult<Unit> = safeApiCall {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val recordedAt = dateFormat.format(Date(location.time))
        val payload = LocationPayload(
            device_uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "",
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            altitude = location.altitude,
            speed = location.speed,
            bearing = location.bearing,
            provider = location.provider ?: "gps",
            battery_level = BatteryUtils.getBatteryLevel(context),
            network_type = NetworkUtils.getNetworkType(context),
            address = null,
            recorded_at = recordedAt
        )
        api.sendLocation(payload)
    }""", """    suspend fun sendLocation(location: Location): ApiResult<Unit> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val recordedAt = dateFormat.format(Date(location.time))
        
        val payload = LocationPayload(
            device_uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "",
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            altitude = location.altitude,
            speed = location.speed,
            bearing = location.bearing,
            provider = location.provider ?: "gps",
            battery_level = BatteryUtils.getBatteryLevel(context),
            network_type = NetworkUtils.getNetworkType(context),
            address = null,
            recorded_at = recordedAt
        )
        val res = safeApiCall { api.sendLocation(payload) }
        if (res is ApiResult.Error) {
            try {
                locationDao.insert(LocationEntity(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    altitude = location.altitude,
                    speed = location.speed,
                    bearing = location.bearing,
                    provider = location.provider ?: "gps",
                    batteryLevel = payload.battery_level,
                    networkType = payload.network_type,
                    recordedAt = location.time
                ))
            } catch (e: Exception) {}
        }
        return res
    }""")

repo_data = repo_data.replace("""    suspend fun sendHeartbeat(): ApiResult<HeartbeatResponsePayload> = safeApiCall {
        val payload = HeartbeatPayload(
            device_uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "",
            battery_level = BatteryUtils.getBatteryLevel(context),
            fcm_token = prefs.getString("fcm_token")
        )
        api.sendHeartbeat(payload)
    }""", """    suspend fun sendHeartbeat(): ApiResult<HeartbeatResponsePayload> {
        val res = safeApiCall {
            val payload = HeartbeatPayload(
                device_uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "",
                battery_level = BatteryUtils.getBatteryLevel(context),
                fcm_token = prefs.getString("fcm_token")
            )
            api.sendHeartbeat(payload)
        }
        if (res is ApiResult.Success) {
            flushPendingLocations()
        }
        return res
    }

    private suspend fun flushPendingLocations() {
        try {
            val pending = locationDao.getPendingUploads(50)
            if (pending.isEmpty()) return
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val uids = mutableListOf<Int>()
            for (loc in pending) {
                val payload = LocationPayload(
                    device_uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "",
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    accuracy = loc.accuracy,
                    altitude = loc.altitude,
                    speed = loc.speed,
                    bearing = loc.bearing,
                    provider = loc.provider,
                    battery_level = loc.batteryLevel,
                    network_type = loc.networkType,
                    address = null,
                    recorded_at = dateFormat.format(Date(loc.recordedAt))
                )
                val attempt = safeApiCall { api.sendLocation(payload) }
                if (attempt is ApiResult.Success) uids.add(loc.id)
            }
            if (uids.isNotEmpty()) locationDao.markUploaded(uids)
        } catch (e: Exception) {}
    }""")

repo_data = repo_data.replace('suspend fun sendAlert(payload: Map<String, String>)', 'suspend fun sendAlert(payload: AlertRequest)')
write_file(repo_path, repo_data)


# CommandProcessor.kt - Fix B10 FOUND_MODE
cp_path = r"app/src/main/java/com/zex/tracker/service/CommandProcessor.kt"
cp_data = read_file(cp_path)
cp_data = cp_data.replace("""                    "FOUND_MODE" -> {
                        prefs.putBoolean("isStolen", false)
                        ServiceController.isStolen = false
                        searchModeManager.exitSearchMode("found_mode")
                        handleStopScream()
                        context.sendBroadcast(Intent("ACTION_STOP_SCREAM_AND_FINISH"))
                    }""", """                    "FOUND_MODE" -> {
                        prefs.putBoolean("isStolen", false)
                        ServiceController.isTracking = false
                        ServiceController.isStolen = false
                        ServiceController.isScreaming = false
                        ServiceController.isSearching = false
                        searchModeManager.exitSearchMode("found_mode")
                        handleStopScream()
                        locationTracker.stopContinuous()
                        context.sendBroadcast(Intent("ACTION_STOP_SCREAM_AND_FINISH"))
                    }""")
write_file(cp_path, cp_data)

# FirebaseCommandListener.kt - Fix B5 RTDB deletion
fcl_path = r"app/src/main/java/com/zex/tracker/data/remote/firebase/FirebaseCommandListener.kt"
fcl_data = read_file(fcl_path)
fcl_data = fcl_data.replace("""                        commandProcessor.process(cmd)
                        
                        // Remove from firebase after queuing
                        child.ref.removeValue()""", """                        // Queue command and remove on success inside CommandProcessor OR launch here
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                            commandProcessor.process(cmd)
                            // Clean up RTDB AFTER processing
                            child.ref.removeValue()
                        }""")
write_file(fcl_path, fcl_data)

# ScreamActivity.kt - Fix B1
sa_path = r"app/src/main/java/com/zex/tracker/ui/screens/scream/ScreamActivity.kt"
sa_data = read_file(sa_path)
sa_data = sa_data.replace('import at.favre.lib.crypto.bcrypt.BCrypt\n', '')
sa_data = sa_data.replace("""                    CoroutineScope(Dispatchers.IO).launch {
                        val storedHash = prefs.getString("owner_password_hash")
                        if (!storedHash.isNullOrEmpty()) {
                            val verified = BCrypt.verifyer().verify(pinInput.toCharArray(), storedHash.toByteArray()).verified
                            if (verified) {
                                screamManager.stopScream()
                                ServiceController.isScreaming = false
                                finish()
                            } else {
                                error = true
                            }
                        } else {
                            // API fallback not fully needed if we have hash, but we gracefully log
                            ZexLogger.w("ScreamActivity", "No stored hash available to verify.")
                            error = true
                        }
                    }""", """                    val storedSecret = prefs.getString("alarm_secret")
                    if (pinInput == storedSecret) {
                        screamManager.stopScream()
                        ServiceController.isScreaming = false
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                // optional API fallback/sync
                            } catch(e: Exception) {}
                        }
                        finish()
                    } else {
                        error = true
                    }""")
write_file(sa_path, sa_data)

# SimChangeReceiver.kt - B8 and B3
scr_path = r"app/src/main/java/com/zex/tracker/receiver/SimChangeReceiver.kt"
if os.path.exists(scr_path):
    scr_data = read_file(scr_path)
    scr_data = scr_data.replace('import android.telephony.TelephonyManager', 'import android.telephony.TelephonyManager\nimport com.zex.tracker.core.constants.ZexConstants')
    scr_data = scr_data.replace("""        if (intent.action == "android.intent.action.SIM_STATE_CHANGED") {
            val state = intent.getStringExtra("ss")
            if (state == "LOADED" || state == "READY") {
                ZexLogger.w("SimChangeReceiver", "SIM card changed/loaded!")
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        deviceRepo.sendAlert(mapOf(
                            "type" to "SIM_CHANGED",
                            "message" to "A new SIM card was inserted into the device."
                        ))
                    } catch (e: Exception) {
                        ZexLogger.e("SimChangeReceiver", "Failed to send SIM alert", e)
                    }
                }
            }
        }""", """        if (intent.action == "android.intent.action.SIM_STATE_CHANGED") {
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
        }""")
    write_file(scr_path, scr_data)
else:
    write_file(scr_path, """package com.zex.tracker.receiver

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
""")

# SmsCommandReceiver.kt - B7 
sms_path = r"app/src/main/java/com/zex/tracker/receiver/SmsCommandReceiver.kt"
sms_data = read_file(sms_path)
sms_data = sms_data.replace("""                        when (cmdStr) {
                            "SEARCH_ON" -> searchModeManager.enterSearchMode("sms", 30)
                            "SEARCH_OFF" -> searchModeManager.exitSearchMode("sms")
                            else -> {""", """                        when (cmdStr) {
                            "SEARCH_ON" -> searchModeManager.enterSearchMode("sms", 30)
                            "SEARCH_OFF" -> searchModeManager.exitSearchMode("sms")
                            "NET_ON" -> {
                                val cmd = com.zex.tracker.data.remote.dto.CommandDto((System.currentTimeMillis() % 100000).toInt(), "ENABLE_NET", null, "PENDING")
                                commandProcessor.process(cmd)
                            }
                            else -> {""")
write_file(sms_path, sms_data)

# SetupViewModel.kt - save alarm_secret
svm_path = r"app/src/main/java/com/zex/tracker/ui/screens/setup/SetupViewModel.kt"
svm_data = read_file(svm_path)
svm_data = svm_data.replace('prefs.putString(ZexConstants.KEY_DEVICE_UID, finalUid)', 'prefs.putString(ZexConstants.KEY_DEVICE_UID, finalUid)\n                    prefs.putString("alarm_secret", res.data.device.alarm_secret ?: "")')
write_file(svm_path, svm_data)

# Also fix the issue in SearchModeManager.kt reading owner_password_hash? 
# The backend removed it. The user said: "In the Heartbeat Action/Resource: REMOVE owner_password_hash... Do NOT send any password or PIN hash to the device."
# So I should remove owner_password_hash logic in mobile.
smm_path = r"app/src/main/java/com/zex/tracker/security/SearchModeManager.kt"
smm_data = read_file(smm_path)
smm_data = smm_data.replace("""            // Save hash
            payload.owner_password_hash?.let { hash ->
                prefs.putString("owner_password_hash", hash)
            }""", "")
write_file(smm_path, smm_data)

payloads_data = read_file(payloads_path)
payloads_data = payloads_data.replace('val owner_password_hash: String? = null', '')
write_file(payloads_path, payloads_data)

