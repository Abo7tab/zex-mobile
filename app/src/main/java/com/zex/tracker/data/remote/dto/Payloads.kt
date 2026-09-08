package com.zex.tracker.data.remote.dto

import com.zex.tracker.domain.model.Command

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

data class HeartbeatResponsePayload(
    val pending_commands: List<CommandDto>? = null,
    val owner_is_searching: Boolean = false,
    val search_interval_seconds: Int = 30
)

data class DeviceStatusPayload(
    val device_uid: String,
    val is_searching: Boolean,
    val is_stolen: Boolean,
    val is_screaming: Boolean,
    val is_tracking_continuous: Boolean,
    val search_interval_seconds: Int,
    val battery_level: Int
)

data class CommandDto(
    val id: Int,
    val type: String,
    val parameters: Map<String, String>? = null,
    val status: String
)
