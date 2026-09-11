package com.zex.tracker.data.remote.dto

data class DeviceRegisterRequest(
    val device_uid: String,
    val device_name: String,
    val device_model: String,
    val android_version: String,
    val alarm_secret: String? = null,
    val sim_iccid: String?,
    val fcm_token: String? = null
)

data class DeviceRegisterResponse(
    val device: DeviceDto,
    val device_token: String
)

data class DeviceDto(
    val id: Long,
    val device_uid: String,
    val device_name: String,
    val device_model: String,
    val android_version: String,
    val alarm_secret: String? = null,
    val phone_number: String? = null,
    val battery_level: Int? = null
)
