package com.project.job.data.repository.implement

import android.content.Context
import android.util.Log
import com.project.job.data.mapper.ChatMapper
import com.project.job.data.repository.ConversationRepository
import com.project.job.data.source.local.room.AppDatabase
import com.project.job.data.source.local.room.ChatDAO
import com.project.job.data.source.local.room.entity.ChatEntity
import com.project.job.data.source.remote.ChatRemote
import com.project.job.data.source.remote.NetworkResult
import kotlinx.coroutines.flow.Flow

class ConversationRepositoryImpl(context: Context) : ConversationRepository {
    private val chatDao: ChatDAO = AppDatabase(context).getChatDAO()
    private val chatRemote = ChatRemote.getInstance()

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
     * Get all conversations from local database as Flow
     * UI will automatically update when data changes
     */
    override fun getAllConversationsLocal(): Flow<List<ChatEntity>> {
        Log.d(TAG, "Getting all conversations from local database")
        return chatDao.getAllConversations()
    }

    /**
     * Get conversations by sender ID from local database as Flow
     */
    override fun getConversationsBySenderLocal(senderId: String): Flow<List<ChatEntity>> {
        Log.d(TAG, "Getting conversations for sender: $senderId from local database")
        return chatDao.getConversationsBySender(senderId)
    }

    /**
     * Fetch conversations from remote API and save to local database
     * This will automatically trigger UI update via Flow
     */
    override suspend fun fetchAndSaveConversations(): NetworkResult<Unit> {
        return try {
            Log.d(TAG, "Fetching conversations from remote API")
            val response = chatRemote.getConversations()
            
            when (response) {
                is NetworkResult.Success -> {
                    val conversations = response.data.conversations
                    Log.d(TAG, "Fetched ${conversations.size} conversations from API")
                    
                    // Convert API response to Room entities
                    val chatEntities = ChatMapper.toEntityList(conversations)
                    
                    // Save to local database
                    chatDao.insertConversations(chatEntities)
                    Log.d(TAG, "Saved ${chatEntities.size} conversations to local database")
                    
                    NetworkResult.Success(Unit)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Error fetching conversations: ${response.message}")
                    NetworkResult.Error(response.message)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching conversations: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Mark conversation as read in local database
     */
    override suspend fun markConversationAsRead(conversationId: String) {
        try {
            Log.d(TAG, "Marking conversation as read: $conversationId")
            chatDao.markAsRead(conversationId)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking conversation as read: ${e.message}", e)
        }
    }

    /**
     * Delete conversation (local only for now, can add remote call later)
     */
    override suspend fun deleteConversation(conversationId: String): NetworkResult<Unit> {
        return try {
            Log.d(TAG, "Deleting conversation: $conversationId")
            chatDao.deleteConversationById(conversationId)
            NetworkResult.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting conversation: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Unknown error")
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

    /**
     * Insert conversations into local database
     */
    override suspend fun insertConversations(conversations: List<ChatEntity>) {
        try {
            Log.d(TAG, "Inserting ${conversations.size} conversations into local database")
            chatDao.insertConversations(conversations)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting conversations: ${e.message}", e)
        }
    }

    /**
     * Get a single conversation by ID
     */
    override suspend fun getConversationById(conversationId: String): ChatEntity? {
        return try {
            Log.d(TAG, "Getting conversation by ID: $conversationId")
            chatDao.getConversationById(conversationId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting conversation by ID: ${e.message}", e)
            null
        }
    }

    /**
     * Get total unread count
     */
    override fun getTotalUnreadCount(): Flow<Int?> {
        Log.d(TAG, "Getting total unread count")
        return chatDao.getTotalUnreadCount()
    }

    /**
     * Search conversations by name or username
     */
    override fun searchConversations(query: String): Flow<List<ChatEntity>> {
        Log.d(TAG, "Searching conversations with query: $query")
        return chatDao.searchConversations(query)
    }
}
