package com.zex.tracker.data.repository

import com.zex.tracker.data.remote.ApiResult
import com.zex.tracker.data.remote.api.ZexApi
import com.zex.tracker.data.remote.dto.AuthResponse
import com.zex.tracker.data.remote.dto.LoginRequest
import com.zex.tracker.data.remote.dto.RegisterRequest
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: ZexApi
) : BaseRepository() {
    suspend fun register(request: RegisterRequest): ApiResult<AuthResponse> = safeApiCall {
        api.registerOwner(request)
    }

    suspend fun login(request: LoginRequest): ApiResult<AuthResponse> = safeApiCall {
        api.loginOwner(request)
    }
}
