package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.pref.FugaPreferences
import com.example.data.repository.ChatRepository
import com.example.data.repository.CustomAgentRepository
import com.example.data.repository.FirebaseVertexAIRepository
import com.example.data.repository.MemoryRepository
import com.example.data.repository.ProjectRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FugaApp : Application() {

    lateinit var preferences: FugaPreferences
        private set
    lateinit var database: AppDatabase
        private set
    lateinit var chatRepository: ChatRepository
        private set
    lateinit var memoryRepository: MemoryRepository
        private set
    lateinit var customAgentRepository: CustomAgentRepository
        private set
    lateinit var projectRepository: ProjectRepository
        private set
    lateinit var vertexAIRepository: FirebaseVertexAIRepository
        private set

    companion object {
        lateinit var instance: FugaApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferences = FugaPreferences(this)
        database = AppDatabase.getDatabase(this)
        chatRepository = ChatRepository(database.chatDao())
        memoryRepository = MemoryRepository(database.memoryDao())
        customAgentRepository = CustomAgentRepository(database.customAgentDao())
        projectRepository = ProjectRepository(database.projectDao())
        vertexAIRepository = FirebaseVertexAIRepository(this)

        CoroutineScope(Dispatchers.IO).launch {
            customAgentRepository.ensureDefaultAgents()
        }
    }
}
