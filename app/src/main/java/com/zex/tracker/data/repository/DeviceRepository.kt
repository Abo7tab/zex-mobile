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

class DeviceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ZexApi,
    private val prefs: SecurePrefs
) : BaseRepository() {

    suspend fun registerDevice(request: DeviceRegisterRequest): ApiResult<DeviceRegisterResponse> = safeApiCall {
        api.registerDevice(request)
    }

    suspend fun sendLocation(location: Location): ApiResult<Unit> = safeApiCall {
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
    }

    suspend fun sendHeartbeat(): ApiResult<HeartbeatResponsePayload> = safeApiCall {
        val payload = HeartbeatPayload(
            device_uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "",
            battery_level = BatteryUtils.getBatteryLevel(context)
        )
        api.sendHeartbeat(payload)
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

    suspend fun sendAlert(payload: Map<String, String>): ApiResult<Unit> = safeApiCall {
        api.sendAlert(payload)
    }
}
