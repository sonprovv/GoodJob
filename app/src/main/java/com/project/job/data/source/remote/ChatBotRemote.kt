package com.project.job.data.source.remote

import com.project.job.data.network.ApiService
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.ChatBotDataSource
import com.project.job.data.source.remote.api.request.ChatBotRequest
import com.project.job.data.source.remote.api.response.ChatBotResponse

class ChatBotRemote(private val apiService: ApiService) : ChatBotDataSource {
    override suspend fun chatBot(request: String): NetworkResult<ChatBotResponse> {
        return safeApiCall {
            apiService.chatBot(ChatBotRequest(request))
        }
    }
    companion object {
        private var instance: ChatBotRemote? = null
        fun getInstance(): ChatBotRemote {
            if (instance == null) instance = ChatBotRemote(RetrofitClient.apiService)
            return instance!!
        }
    }
}