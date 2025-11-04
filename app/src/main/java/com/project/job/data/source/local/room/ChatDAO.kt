package com.project.job.data.source.local.room

import androidx.room.*
import com.project.job.data.source.local.room.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDAO {
    // Insert a conversation
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ChatEntity): Long

    // Insert multiple conversations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<ChatEntity>)

    // Update a conversation
    @Update
    suspend fun updateConversation(conversation: ChatEntity)

    // Delete a conversation
    @Delete
    suspend fun deleteConversation(conversation: ChatEntity)

    // Get all conversations ordered by last message time
    @Query("SELECT * FROM conversations ORDER BY lastMessageTime DESC")
    fun getAllConversations(): Flow<List<ChatEntity>>

    // Get conversation by ID
    @Query("SELECT * FROM conversations WHERE id = :conversationId LIMIT 1")
    suspend fun getConversationById(conversationId: String): ChatEntity?

    // Get conversations by sender ID
    @Query("SELECT * FROM conversations WHERE senderId = :senderId ORDER BY lastMessageTime DESC")
    fun getConversationsBySender(senderId: String): Flow<List<ChatEntity>>

    // Update unread count
    @Query("UPDATE conversations SET unreadCount = :count WHERE id = :conversationId")
    suspend fun updateUnreadCount(conversationId: String, count: Int)

    // Mark conversation as read (reset unread count)
    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun markAsRead(conversationId: String)

    // Get total unread message count
    @Query("SELECT SUM(unreadCount) FROM conversations")
    fun getTotalUnreadCount(): Flow<Int?>

    // Delete conversation by ID
    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversationById(conversationId: String)

    // Clear all conversations
    @Query("DELETE FROM conversations")
    suspend fun clearAllConversations()

    // Search conversations by name
    @Query("SELECT * FROM conversations WHERE senderName LIKE '%' || :query || '%' OR senderUsername LIKE '%' || :query || '%' ORDER BY lastMessageTime DESC")
    fun searchConversations(query: String): Flow<List<ChatEntity>>
}