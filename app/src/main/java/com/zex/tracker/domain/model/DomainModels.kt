package com.zex.tracker.domain.model

data class Owner(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?
)

data class Device(
    val id: Int,
    val deviceUid: String,
    val name: String,
    val status: String
)

data class Command(
    val id: Int,
    val type: String,
    val status: String
)

data class Alert(
    val id: Int,
    val type: String,
    val message: String
)

data class LocationPoint(
    val lat: Double,
    val lng: Double,
    val timestamp: Long
)
