package com.project.job.data.source.local.room

import androidx.room.*
import com.project.job.data.source.local.room.entity.ChatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDAO {
    // Insert a new message
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatEntity): Long

    // Insert multiple messages
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatEntity>)

    // Update a message
    @Update
    suspend fun updateMessage(message: ChatEntity)

    // Delete a message
    @Delete
    suspend fun deleteMessage(message: ChatEntity)

    // Get all messages for a conversation
    @Query("SELECT * FROM conversations WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp ASC")
    fun getConversation(userId1: String, userId2: String): Flow<List<ChatEntity>>

    // Get the last message for each conversation
    @Query("""
        SELECT * FROM conversations 
        WHERE id IN (
            SELECT MAX(id) 
            FROM conversations 
            WHERE senderId = :userId OR receiverId = :userId 
            GROUP BY 
                CASE 
                    WHEN senderId = :userId THEN receiverId 
                    ELSE senderId 
                END
        )
        ORDER BY timestamp DESC
    """)
    fun getAllConversations(userId: String): Flow<List<ChatEntity>>

    // Get unread message count for a conversation
    @Query("SELECT COUNT(*) FROM conversations WHERE receiverId = :userId AND status = 'sent'")
    fun getUnreadMessageCount(userId: String): Flow<Int>

    // Mark messages as read
    @Query("UPDATE conversations SET status = 'read' WHERE senderId = :otherUserId AND receiverId = :userId AND status = 'sent'")
    suspend fun markMessagesAsRead(userId: String, otherUserId: String)

    // Get last message in a conversation
    @Query("SELECT * FROM conversations WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1) ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(userId1: String, userId2: String): ChatEntity?

    // Delete all messages in a conversation
    @Query("DELETE FROM conversations WHERE (senderId = :userId1 AND receiverId = :userId2) OR (senderId = :userId2 AND receiverId = :userId1)")
    suspend fun deleteConversation(userId1: String, userId2: String)

    // Clear all messages
    @Query("DELETE FROM conversations")
    suspend fun clearAllMessages()
}