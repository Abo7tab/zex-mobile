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
    suspend fun getOwnerMe(): Response<DataWrapper<OwnerDto>>

    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: Map<String, String>): Response<DataWrapper<OwnerDto>>

    @PUT("auth/security")
    suspend fun updateSecurity(@Body request: Map<String, String>): Response<Unit>

    @GET("devices")
    suspend fun getOwnerDevices(): Response<DataWrapper<List<DeviceDto>>>

    @POST("devices/register")
    suspend fun registerDevice(@Body request: DeviceRegisterRequest): Response<DeviceRegisterResponse>

    @POST("locations")
    suspend fun sendLocation(@Body payload: LocationPayload): Response<Unit>

    @POST("locations/ble-relay")
    suspend fun sendBleRelayLocation(@Body payload: BleRelayPayload): Response<Unit>

    @POST("devices/heartbeat")
    suspend fun sendHeartbeat(@Body payload: HeartbeatPayload): Response<HeartbeatResponsePayload>

    @POST("commands/{command}/response")
    suspend fun sendCommandResponse(@Path("command") commandId: Int, @Body payload: CommandResponsePayload): Response<Unit>

    @POST("devices/{device}/stop-scream")
    suspend fun stopScream(@Path("device") deviceId: Long, @Body payload: Map<String, String>): Response<Unit>

    @POST("alerts")
    suspend fun sendAlert(@Body payload: AlertRequest): Response<Unit>

    @GET("devices/{device}/status")
    suspend fun getDeviceStatus(@Path("device") deviceId: Long): Response<DeviceStatusPayload>
}
