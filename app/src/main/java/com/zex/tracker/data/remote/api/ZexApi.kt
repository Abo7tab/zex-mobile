package com.zex.tracker.data.remote.api

import com.zex.tracker.data.remote.dto.AuthResponse
import com.zex.tracker.data.remote.dto.DeviceRegisterRequest
import com.zex.tracker.data.remote.dto.DeviceRegisterResponse
import com.zex.tracker.data.remote.dto.LoginRequest
import com.zex.tracker.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ZexApi {
    @POST("auth/register")
    suspend fun registerOwner(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun loginOwner(@Body request: LoginRequest): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getOwnerMe(): Response<AuthResponse>

    @POST("devices/register")
    suspend fun registerDevice(@Body request: DeviceRegisterRequest): Response<DeviceRegisterResponse>
}
