package com.project.job.data.repository.implement

import com.project.job.data.source.remote.BaseResponse
import com.project.job.data.source.remote.api.request.chat.SendMessageRequest
import com.project.job.data.source.remote.api.request.chat.UpdateStatusRequest
import com.project.job.data.source.remote.api.response.chat.ChatUserResponse
import com.project.job.data.source.remote.api.response.chat.ConversationResponse
import com.project.job.data.source.remote.api.response.chat.MessageResponse
import com.project.job.data.source.remote.api.response.chat.UserStatusResponse

interface ChatRepositoryImpl {
    suspend fun sendMessage(request: SendMessageRequest): Result<BaseResponse<MessageResponse>>
    suspend fun getMessages(userId: String, limit: Int = 50): Result<BaseResponse<List<MessageResponse>>>
    suspend fun getConversations(): Result<BaseResponse<List<ConversationResponse>>>
    suspend fun getAvailableUsers(): Result<BaseResponse<List<ChatUserResponse>>>
    suspend fun markAsRead(userId: String): Result<BaseResponse<Unit>>
    suspend fun deleteMessage(conversationId: String, messageId: String): Result<BaseResponse<Unit>>
    suspend fun deleteConversation(userId: String): Result<BaseResponse<Unit>>
    suspend fun getUserStatus(userId: String): Result<BaseResponse<UserStatusResponse>>
    suspend fun updateStatus(state: String): Result<BaseResponse<UserStatusResponse>>
}
