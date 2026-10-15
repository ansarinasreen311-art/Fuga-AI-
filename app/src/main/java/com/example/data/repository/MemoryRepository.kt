package com.example.data.repository

import com.example.data.db.MemoryDao
import com.example.data.model.MemoryItem
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {
    val allMemories: Flow<List<MemoryItem>> = memoryDao.getAllMemories()

    suspend fun addMemory(category: String, content: String): Long {
        return memoryDao.insertMemory(MemoryItem(category = category, content = content))
    }

    suspend fun deleteMemory(id: Long) = memoryDao.deleteMemoryById(id)

    suspend fun clearMemories() = memoryDao.clearAllMemories()
}
