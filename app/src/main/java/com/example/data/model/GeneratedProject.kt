package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProjectType {
    ANDROID_APP,
    WEBSITE
}

@Entity(tableName = "generated_projects")
data class GeneratedProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val type: ProjectType,
    val prompt: String,
    val filesJson: String, // JSON mapping relative path -> content
    val zipPath: String? = null,
    val previewUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
