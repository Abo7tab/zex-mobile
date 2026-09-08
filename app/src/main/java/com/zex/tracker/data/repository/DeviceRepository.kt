package com.zex.tracker.data.repository

import android.content.Context
import android.location.Location
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.core.utils.BatteryUtils
import com.zex.tracker.core.utils.NetworkUtils
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.data.remote.ApiResult
import com.zex.tracker.data.remote.api.ZexExtendedApi
import com.zex.tracker.data.remote.dto.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeviceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: ZexExtendedApi,
    private val prefs: SecurePrefs
) : BaseRepository() {

    suspend fun registerDevice(request: DeviceRegisterRequest): ApiResult<DeviceRegisterResponse> = safeApiCall {
        api.registerDevice(request)
    }

    suspend fun sendLocation(location: Location): ApiResult<Unit> = safeApiCall {
        val payload = LocationPayload(
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = location.accuracy,
            battery = BatteryUtils.getBatteryLevel(context),
            network = NetworkUtils.getNetworkType(context)
        )
        api.sendLocation(payload)
    }

    suspend fun sendHeartbeat(): ApiResult<HeartbeatResponsePayload> = safeApiCall {
        val payload = HeartbeatPayload(
            device_uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "",
            battery = BatteryUtils.getBatteryLevel(context)
        )
        api.sendHeartbeat(payload)
    }

    suspend fun sendCommandResponse(cmdId: Int, status: String): ApiResult<Unit> = safeApiCall {
        api.sendCommandResponse(cmdId, CommandResponsePayload(status))
    }

    suspend fun getDeviceStatus(): ApiResult<DeviceStatusPayload> = safeApiCall {
        val uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: ""
        api.getDeviceStatus(uid)
    }

    suspend fun sendAlert(payload: Map<String, String>): ApiResult<Unit> = safeApiCall {
        api.sendAlert(payload)
    }
}
