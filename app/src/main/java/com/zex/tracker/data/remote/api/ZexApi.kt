package com.zex.tracker.data.remote.api

import com.zex.tracker.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ZexApi {
    @POST("auth/register")
    suspend fun registerOwner(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun loginOwner(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getOwnerMe(): Response<AuthResponse>

    @POST("devices/register")
    suspend fun registerDevice(@Body request: DeviceRegisterRequest): Response<DeviceRegisterResponse>

    @POST("locations")
    suspend fun sendLocation(@Body payload: LocationPayload): Response<Unit>

    @POST("devices/heartbeat")
    suspend fun sendHeartbeat(@Body payload: HeartbeatPayload): Response<HeartbeatResponsePayload>

    @POST("commands/{id}/response")
    suspend fun sendCommandResponse(@Path("id") id: Int, @Body payload: CommandResponsePayload): Response<Unit>

    @POST("alerts")
    suspend fun sendAlert(@Body payload: Map<String, String>): Response<Unit>

    @GET("devices/{deviceUid}/status")
    suspend fun getDeviceStatus(@Path("deviceUid") deviceUid: String): Response<DeviceStatusPayload>
}
