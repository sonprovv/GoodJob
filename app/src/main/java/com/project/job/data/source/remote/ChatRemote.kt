package com.project.job.data.source.remote

import com.project.job.data.network.ApiService
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.ChatDataSource
import com.project.job.data.source.remote.api.request.chat.SendMessageRequest
import com.project.job.data.source.remote.api.request.chat.UpdateStatusRequest
import com.project.job.data.source.remote.api.response.chat.ConversationResponse
import com.project.job.data.source.remote.api.response.chat.MessageResponse
import com.project.job.data.source.remote.api.response.chat.UserStatusResponse
import com.project.job.data.source.remote.api.response.chat.ChatUserResponse

class ChatRemote(private val api: ApiService) : ChatDataSource {

    override suspend fun sendMessage(request: SendMessageRequest): NetworkResult<BaseResponse<MessageResponse>> {
        return safeApiCall { api.chatSendMessage(request) }
    }

    override suspend fun getMessages(userId: String, limit: Int): NetworkResult<BaseResponse<List<MessageResponse>>> {
        return safeApiCall { api.chatGetMessages(userId, limit) }
    }

    override suspend fun getConversations(): NetworkResult<BaseResponse<List<ConversationResponse>>> {
        return safeApiCall { api.chatGetConversations() }
    }

    override suspend fun getAvailableUsers(): NetworkResult<BaseResponse<List<ChatUserResponse>>> {
        return safeApiCall { api.chatGetAvailableUsers() }
    }

    override suspend fun markAsRead(userId: String): NetworkResult<BaseResponse<Unit>> {
        return safeApiCall { api.chatMarkAsRead(userId) }
    }

    override suspend fun deleteMessage(conversationId: String, messageId: String): NetworkResult<BaseResponse<Unit>> {
        return safeApiCall { api.chatDeleteMessage(conversationId, messageId) }
    }

    override suspend fun deleteConversation(userId: String): NetworkResult<BaseResponse<Unit>> {
        return safeApiCall { api.chatDeleteConversation(userId) }
    }

    override suspend fun getUserStatus(userId: String): NetworkResult<BaseResponse<UserStatusResponse>> {
        return safeApiCall { api.chatGetUserStatus(userId) }
    }

    override suspend fun updateStatus(request: UpdateStatusRequest): NetworkResult<BaseResponse<UserStatusResponse>> {
        return safeApiCall { api.chatUpdateStatus(request) }
    }

    companion object {
        private var instance: ChatRemote? = null
        fun getInstance(): ChatRemote {
            if (instance == null) instance = ChatRemote(RetrofitClient.apiService)
            return instance!!
        }
    }
}
