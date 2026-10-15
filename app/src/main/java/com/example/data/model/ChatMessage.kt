package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false,
    val actionType: String? = null,        // e.g. "OPEN_APP", "CALL", "ALARM", "FLASHLIGHT", "PAYMENT", "SEARCH", "SCREEN"
    val actionDetail: String? = null,      // e.g. "YouTube", "Mom", "7:00 AM", "₹500 to Rahul"
    val actionStatus: String? = null,      // "PENDING_CONFIRMATION", "PENDING_BIOMETRIC", "EXECUTED", "CANCELLED", null
    val isSensitive: Boolean = false
)
