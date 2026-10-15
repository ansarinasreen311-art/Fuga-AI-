package com.example.data.repository

import com.example.data.db.CustomAgentDao
import com.example.data.model.CustomAgent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CustomAgentRepository(private val dao: CustomAgentDao) {

    val allAgents: Flow<List<CustomAgent>> = dao.getAllAgents()
    val activeAgent: Flow<CustomAgent?> = dao.getActiveAgent()

    suspend fun ensureDefaultAgents() {
        val existing = dao.getAllAgents().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val defaultAgents = listOf(
                CustomAgent(
                    name = "Fuga Prime",
                    role = "Primary Personal Assistant",
                    systemPrompt = "You are Fuga Prime, the core AI assistant. Be direct, helpful, adaptive, and handle daily tasks, voice queries, and device actions seamlessly.",
                    iconName = "fuga",
                    accentColorHex = "#8B5CF6",
                    capabilities = "Voice, Dynamic Island, Device Automation, Payments",
                    isDefault = true,
                    isActive = true
                ),
                CustomAgent(
                    name = "Android Architect",
                    role = "Senior Android & Kotlin Specialist",
                    systemPrompt = "You are an elite Senior Android Architect. You generate complete, production-ready Kotlin Jetpack Compose apps, Material Design 3 UI, ViewModels, and Gradle architectures. Always write clean, self-contained, working code.",
                    iconName = "android",
                    accentColorHex = "#10B981",
                    capabilities = "Jetpack Compose, Kotlin, Room, APK Structure, Zip Packaging",
                    isDefault = true,
                    isActive = false
                ),
                CustomAgent(
                    name = "Web Master",
                    role = "Full-Stack Web & Frontend Engineer",
                    systemPrompt = "You are a master Web Developer and UI Engineer. You build stunning, responsive, modern websites using HTML5, modern CSS with flexbox/grid, and modern JavaScript. Always deliver complete, beautiful, working code ready to run directly in any browser.",
                    iconName = "web",
                    accentColorHex = "#06B6D4",
                    capabilities = "HTML5, CSS3, Modern JS, Responsive Web, Live Browser Preview",
                    isDefault = true,
                    isActive = false
                ),
                CustomAgent(
                    name = "Code Auditor",
                    role = "Security & Performance Specialist",
                    systemPrompt = "You are an expert Security Engineer and Code Reviewer. You analyze code for vulnerabilities, logic bugs, performance bottlenecks, and security compliance.",
                    iconName = "security",
                    accentColorHex = "#EC4899",
                    capabilities = "Security Auditing, Optimization, Vulnerability Scanner",
                    isDefault = true,
                    isActive = false
                )
            )

            for (agent in defaultAgents) {
                dao.insertAgent(agent)
            }
        }
    }

    suspend fun createAgent(
        name: String,
        role: String,
        systemPrompt: String,
        iconName: String,
        accentColorHex: String,
        capabilities: String
    ): Long {
        return dao.insertAgent(
            CustomAgent(
                name = name,
                role = role,
                systemPrompt = systemPrompt,
                iconName = iconName,
                accentColorHex = accentColorHex,
                capabilities = capabilities,
                isDefault = false,
                isActive = false
            )
        )
    }

    suspend fun setActiveAgent(id: Long) {
        dao.deactivateAllAgents()
        dao.setActiveAgent(id)
    }

    suspend fun deleteAgent(id: Long) {
        dao.deleteAgentById(id)
    }
}
