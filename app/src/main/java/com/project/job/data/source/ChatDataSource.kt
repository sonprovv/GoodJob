package com.project.job.data.source

import com.project.job.data.source.remote.BaseResponse
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.chat.SendMessageRequest
import com.project.job.data.source.remote.api.request.chat.UpdateStatusRequest
import com.project.job.data.source.remote.api.response.chat.ConversationResponse
import com.project.job.data.source.remote.api.response.chat.MessageResponse
import com.project.job.data.source.remote.api.response.chat.UserStatusResponse
import com.project.job.data.source.remote.api.response.chat.ChatUserResponse

interface ChatDataSource {
    suspend fun sendMessage(request: SendMessageRequest): NetworkResult<BaseResponse<MessageResponse>>
    suspend fun getMessages(userId: String, limit: Int = 50): NetworkResult<BaseResponse<List<MessageResponse>>>
    suspend fun getConversations(): NetworkResult<BaseResponse<List<ConversationResponse>>>
    suspend fun getAvailableUsers(): NetworkResult<BaseResponse<List<ChatUserResponse>>>
    suspend fun markAsRead(userId: String): NetworkResult<BaseResponse<Unit>>
    suspend fun deleteMessage(conversationId: String, messageId: String): NetworkResult<BaseResponse<Unit>>
    suspend fun deleteConversation(userId: String): NetworkResult<BaseResponse<Unit>>
    suspend fun getUserStatus(userId: String): NetworkResult<BaseResponse<UserStatusResponse>>
    suspend fun updateStatus(request: UpdateStatusRequest): NetworkResult<BaseResponse<UserStatusResponse>>
}
