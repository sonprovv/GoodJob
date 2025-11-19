package com.project.job.data.source.remote

import com.project.job.data.network.ApiService
import com.project.job.data.network.ChatApiService
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.ChatDataSource
class ChatRemote(private val api: ChatApiService) : ChatDataSource.Remote {

    override suspend fun markAsRead(userId: String): NetworkResult<BaseResponse<Unit>> {
        return safeApiCall { api.chatMarkAsRead(userId) }
    }

    override suspend fun deleteMessage(conversationId: String, messageId: String): NetworkResult<BaseResponse<Unit>> {
        return safeApiCall { api.chatDeleteMessage(conversationId, messageId) }
    }

    override suspend fun deleteConversation(userId: String): NetworkResult<BaseResponse<Unit>> {
        return safeApiCall { api.chatDeleteConversation(userId) }
    }

    companion object {
        private var instance: ChatRemote? = null
        fun getInstance(): ChatRemote {
            if (instance == null) instance = ChatRemote(RetrofitClient.chatApiService)
            return instance!!
        }
    }
}
