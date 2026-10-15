package com.example.data.repository

import com.example.data.db.ChatDao
import com.example.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val allMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages()

    suspend fun addMessage(message: ChatMessage): Long = chatDao.insertMessage(message)

    suspend fun updateMessage(message: ChatMessage) = chatDao.updateMessage(message)

    suspend fun deleteMessage(id: Long) = chatDao.deleteMessageById(id)

    suspend fun clearHistory() = chatDao.clearAllMessages()
}
