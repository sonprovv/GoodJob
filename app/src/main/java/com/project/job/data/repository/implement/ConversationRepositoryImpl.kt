package com.project.job.data.repository.implement

import android.content.Context
import android.util.Log
import com.project.job.data.repository.ConversationRepository
import com.project.job.data.source.local.room.AppDatabase
import com.project.job.data.source.local.room.ChatDAO

class ConversationRepositoryImpl(context: Context) : ConversationRepository {
    private val chatDao: ChatDAO = AppDatabase(context).getChatDAO()

    companion object {
        private const val TAG = "ConversationRepository"
        
        @Volatile
        private var instance: ConversationRepositoryImpl? = null

        fun getInstance(context: Context): ConversationRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: ConversationRepositoryImpl(context.applicationContext).also { instance = it }
            }
        }
    }
    /**
     * Clear all local conversations
     */
    override suspend fun clearLocalConversations() {
        try {
            Log.d(TAG, "Clearing all local conversations")
            chatDao.clearAllConversations()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local conversations: ${e.message}", e)
        }
    }
}
