package com.project.job.data.repository

import com.project.job.data.source.local.room.entity.ChatEntity
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.response.chat.ConversationResponse
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    /**
     * Get all conversations from local database as Flow (auto-update UI when data changes)
     */
    fun getAllConversationsLocal(): Flow<List<ChatEntity>>

    /**
     * Get conversations by sender ID from local database
     */
    fun getConversationsBySenderLocal(senderId: String): Flow<List<ChatEntity>>

    /**
     * Fetch conversations from remote API and save to local database
     * This will trigger UI update automatically via Flow
     */
    suspend fun fetchAndSaveConversations(): NetworkResult<Unit>

    /**
     * Mark conversation as read in local database
     */
    suspend fun markConversationAsRead(conversationId: String)

    /**
     * Delete conversation (both remote and local)
     */
    suspend fun deleteConversation(conversationId: String): NetworkResult<Unit>

    /**
     * Clear all local conversations
     */
    suspend fun clearLocalConversations()

    /**
     * Insert conversations into local database
     */
    suspend fun insertConversations(conversations: List<ChatEntity>)

    /**
     * Get a single conversation by ID
     */
    suspend fun getConversationById(conversationId: String): ChatEntity?

    /**
     * Get total unread count
     */
    fun getTotalUnreadCount(): Flow<Int?>

    /**
     * Search conversations by name or username
     */
    fun searchConversations(query: String): Flow<List<ChatEntity>>
}
