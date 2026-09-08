package com.zex.tracker.data.remote.dto

data class LocationPayload(
    val device_uid: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double,
    val speed: Float,
    val bearing: Float,
    val provider: String,
    val battery_level: Int,
    val fcm_token: String? = null,
    val network_type: String,
    val address: String?,
    val recorded_at: String
)

data class HeartbeatPayload(
    val device_uid: String,
    val battery_level: Int,
    val fcm_token: String? = null
)

data class CommandResponsePayload(
    val device_uid: String,
    val status: String,
    val response: Map<String, String>? = null
)

data class HeartbeatResponsePayload(
    val pending_commands: List<CommandDto>? = null,
    val owner_is_searching: Boolean = false,
    val search_interval_seconds: Int = 30,
    
)

data class DeviceStatusPayload(
    val device_uid: String,
    val is_searching: Boolean,
    val is_stolen: Boolean,
    val is_screaming: Boolean,
    val is_tracking_continuous: Boolean,
    val search_interval_seconds: Int,
    val battery_level: Int,
    val fcm_token: String? = null
)

data class CommandDto(
    val id: Int,
    val type: String,
    val parameters: Map<String, String>? = null,
    val status: String
)

data class AlertRequest(
    val device_uid: String,
    val type: String,
    val message: String,
    val photo_url: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
