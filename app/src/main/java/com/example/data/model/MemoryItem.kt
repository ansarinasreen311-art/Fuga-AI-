package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuga_memories")
data class MemoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,      // e.g. "Preference", "Fact", "Contact", "Custom"
    val content: String,       // e.g. "Prefers morning workout at 6:30 AM"
    val timestamp: Long = System.currentTimeMillis()
)
