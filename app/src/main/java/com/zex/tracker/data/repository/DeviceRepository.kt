package com.zex.tracker.data.repository

import android.content.Context
import android.location.Location
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.core.utils.BatteryUtils
import com.zex.tracker.core.utils.NetworkUtils
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.data.remote.ApiResult
import com.zex.tracker.data.remote.api.ZexApi
import com.zex.tracker.data.remote.dto.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.zex.tracker.data.local.dao.LocationDao
import com.zex.tracker.data.local.entity.LocationEntity

class DeviceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ZexApi,
    private val prefs: SecurePrefs,
    private val locationDao: LocationDao
) : BaseRepository() {

    suspend fun registerDevice(request: DeviceRegisterRequest): ApiResult<DeviceRegisterResponse> = safeApiCall {
        api.registerDevice(request.copy(fcm_token = prefs.getString("fcm_token")))
    }

    suspend fun sendLocation(location: Location): ApiResult<Unit> {
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
            fcm_token = prefs.getString("fcm_token"),
            network_type = NetworkUtils.getNetworkType(context),
            address = null,
            recorded_at = java.time.Instant.now().toString()
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
    }

    suspend fun sendHeartbeat(): ApiResult<HeartbeatResponsePayload> {
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

    suspend fun flushPendingLocations() {
        try {
            val pending = locationDao.getPendingUploads(50)
            if (pending.isEmpty()) return
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
                    recorded_at = java.time.Instant.ofEpochMilli(loc.recordedAt).toString()
                )
                val attempt = safeApiCall { api.sendLocation(payload) }
                if (attempt is ApiResult.Success) uids.add(loc.id)
            }
            if (uids.isNotEmpty()) locationDao.markUploaded(uids)
        } catch (e: Exception) {}
    }

    suspend fun sendCommandResponse(cmdId: Int, status: String, responseData: Map<String, String>? = null): ApiResult<Unit> = safeApiCall {
        val payload = CommandResponsePayload(
            device_uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "",
            status = status,
            response = responseData
        )
        api.sendCommandResponse(cmdId, payload)
    }

    suspend fun getDeviceStatus(): ApiResult<DeviceStatusPayload> = safeApiCall {
        val deviceId = prefs.getLong("device_numeric_id", 0L)
        api.getDeviceStatus(deviceId)
    }

    suspend fun stopScream(alarmSecret: String): ApiResult<Unit> = safeApiCall {
        val deviceId = prefs.getLong("device_numeric_id", 0L)
        api.stopScream(deviceId, mapOf("alarm_secret" to alarmSecret))
    }

    suspend fun sendAlert(payload: AlertRequest): ApiResult<Unit> = safeApiCall {
        api.sendAlert(payload)
    }

    suspend fun reportSimChange(newNumber: String): ApiResult<Unit> = safeApiCall {
        val payload = AlertRequest(
            device_uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "",
            type = "SIM_CHANGED",
            message = "SIM Card was changed. New Number: $newNumber"
        )
        api.sendAlert(payload)
    }
}
