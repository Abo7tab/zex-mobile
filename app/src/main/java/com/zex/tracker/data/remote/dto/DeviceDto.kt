package com.zex.tracker.data.remote.dto

data class DeviceRegisterRequest(
    val device_uid: String,
    val device_name: String,
    val device_model: String,
    val android_version: String,
    val sim_iccid: String?
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
    val android_version: String
)
