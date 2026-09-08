package com.zex.tracker.domain.model

enum class CommandType {
    LOCATE, CONTINUOUS_TRACK, STOP_TRACKING, SCREAM, STOP_SCREAM,
    LOCK, ENABLE_NET, STATUS, STOLEN_MODE, FOUND_MODE, PHOTO
}

enum class CommandStatus {
    PENDING, SENT, EXECUTED, FAILED
}

data class Command(
    val id: Int,
    val type: CommandType,
    val parameters: Map<String, String>? = null,
    val status: CommandStatus = CommandStatus.PENDING
)
