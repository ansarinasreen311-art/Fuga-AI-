package com.example.data.model

enum class DynamicIslandStatus {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    CLOSE
}

data class DynamicIslandState(
    val status: DynamicIslandStatus = DynamicIslandStatus.IDLE,
    val headline: String = "Fuga is ready",
    val subtitle: String = "Tap mic or say “Hey Fuga”",
    val audioLevel: Float = 0f
)
