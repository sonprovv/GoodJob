package com.project.job.data.source

import com.project.job.data.source.remote.BaseResponse
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.chat.*
import com.project.job.data.source.remote.api.response.chat.*
import kotlinx.coroutines.flow.Flow

/**
 * Data source interface for chat-related operations
 */
interface ChatDataSource {
    /**
     * Local data source for chat operations
     */
    interface Local {
        // Message operations
        suspend fun saveMessage(message: MessageResponse)
        suspend fun saveMessages(messages: List<MessageResponse>)
        suspend fun getMessageById(messageId: String): MessageResponse?
        suspend fun getMessagesWithUser(userId: String): List<MessageResponse>
        suspend fun getMessagesWithUserFlow(userId: String): Flow<List<MessageResponse>>
        suspend fun deleteMessage(messageId: String)
        
        // Conversation operations
        suspend fun saveConversation(conversation: ConversationResponse)
        suspend fun saveConversations(conversations: List<ConversationResponse>)
        suspend fun getConversation(conversationId: String): ConversationResponse?
        suspend fun getAllConversations(): List<ConversationResponse>
        suspend fun getAllConversationsFlow(): Flow<List<ConversationResponse>>
        suspend fun deleteConversation(conversationId: String)
        
        // Message status operations
        suspend fun markMessagesAsRead(conversationId: String)
        suspend fun getUnreadMessageCount(conversationId: String): Int
        suspend fun getTotalUnreadCount(): Int
        
        // Media operations
        suspend fun saveMedia(media: MediaResponse)
        suspend fun getMedia(messageId: String): List<MediaResponse>
        
        // User presence
        suspend fun updateUserStatus(userId: String, isOnline: Boolean, lastSeen: Long)
        suspend fun getUserStatus(userId: String): UserStatusResponse?
        
        // Clear all data (for logout)
        suspend fun clearAllChatData()
    }

    /**
     * Remote data source for chat operations
     */
    interface Remote {

        suspend fun sendMessage(request: SendMessageRequest): NetworkResult<MessageResponse>
        suspend fun getMessages(userId: String): NetworkResult<GetMessagesResponse>
        suspend fun getConversations(): NetworkResult<ConversationResponse>
        suspend fun getAvailableUsers(): NetworkResult<BaseResponse<List<ChatUserResponse>>>
        suspend fun markAsRead(userId: String): NetworkResult<BaseResponse<Unit>>
        suspend fun deleteMessage(
            conversationId: String,
            messageId: String
        ): NetworkResult<BaseResponse<Unit>>
        
        suspend fun deleteConversation(userId: String): NetworkResult<BaseResponse<Unit>>
        suspend fun getUserStatus(userId: String): NetworkResult<BaseResponse<UserStatusResponse>>
        suspend fun updateStatus(request: UpdateStatusRequest): NetworkResult<BaseResponse<UserStatusResponse>>
    }
}
