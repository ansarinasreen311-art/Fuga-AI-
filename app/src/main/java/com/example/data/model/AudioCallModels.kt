package com.example.data.model

enum class CallStatus {
    IDLE,
    CONNECTING,
    LISTENING,
    THINKING,
    SPEAKING,
    MUTED,
    ENDED
}

data class CallTranscriptItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val speaker: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUser: Boolean
)
