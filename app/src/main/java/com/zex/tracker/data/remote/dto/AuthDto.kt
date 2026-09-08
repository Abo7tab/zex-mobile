package com.zex.tracker.data.remote.dto

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val password_confirmation: String,
    val pin_code: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val owner: OwnerDto,
    val token: String? = null
)

data class OwnerDto(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val pin_code: String?
)
