package com.zex.tracker.data.remote.api

import com.zex.tracker.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ZexExtendedApi : ZexApi {
    @POST("locations")
    suspend fun sendLocation(@Body payload: LocationPayload): Response<Unit>

    @POST("devices/heartbeat")
    suspend fun sendHeartbeat(@Body payload: HeartbeatPayload): Response<Unit>

    @POST("commands/{id}/response")
    suspend fun sendCommandResponse(@Path("id") id: Int, @Body payload: CommandResponsePayload): Response<Unit>

    @POST("alerts")
    suspend fun sendAlert(@Body payload: Map<String, String>): Response<Unit>
}
