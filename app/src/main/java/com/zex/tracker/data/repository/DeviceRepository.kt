package com.zex.tracker.data.repository

import com.zex.tracker.data.remote.ApiResult
import com.zex.tracker.data.remote.api.ZexApi
import com.zex.tracker.data.remote.dto.DeviceRegisterRequest
import com.zex.tracker.data.remote.dto.DeviceRegisterResponse
import javax.inject.Inject

class DeviceRepository @Inject constructor(
    private val api: ZexApi
) : BaseRepository() {
    suspend fun registerDevice(request: DeviceRegisterRequest): ApiResult<DeviceRegisterResponse> = safeApiCall {
        api.registerDevice(request)
    }
}
