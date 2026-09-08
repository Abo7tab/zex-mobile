package com.zex.tracker.data.remote.dto

data class LocationPayload(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val battery: Int,
    val network: String
)

data class HeartbeatPayload(
    val device_uid: String,
    val battery: Int
)

data class CommandResponsePayload(
    val status: String,
    val message: String? = null
)
