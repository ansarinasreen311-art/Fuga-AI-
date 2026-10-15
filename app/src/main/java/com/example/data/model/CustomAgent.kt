package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_agents")
data class CustomAgent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: String,
    val systemPrompt: String,
    val iconName: String = "code",
    val accentColorHex: String = "#8B5CF6",
    val capabilities: String = "Code Generation, Full-Stack",
    val isDefault: Boolean = false,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
