package com.project.job.data.repository.implement

import com.project.job.data.source.remote.BaseResponse
import com.project.job.data.source.remote.api.request.chat.*
import com.project.job.data.source.remote.api.response.chat.*
import kotlinx.coroutines.flow.Flow

interface ChatRepositoryImpl {
    //region Message Operations
    suspend fun sendMessage(request: SendMessageRequest): Result<MessageResponse>
    fun getMessages(conversationId: String): Flow<List<MessageResponse>>
    suspend fun refreshMessages(conversationId: String): Result<Unit>
    suspend fun editMessage(messageId: String, content: String): Result<MessageResponse>
    suspend fun deleteMessage(conversationId: String, messageId: String): Result<BaseResponse<Unit>>
    //endregion

    //region Conversation Operations
    fun getConversations(): Flow<List<ConversationResponse>>
    suspend fun refreshConversations(): Result<Unit>
    suspend fun getConversation(conversationId: String): Result<ConversationResponse>
    suspend fun deleteConversation(conversationId: String): Result<BaseResponse<Unit>>
    //endregion
}
