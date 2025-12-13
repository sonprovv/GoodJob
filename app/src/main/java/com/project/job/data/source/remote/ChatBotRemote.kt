package com.project.job.data.source.remote

import com.project.job.data.network.ApiService
import com.project.job.data.network.ChatApiService
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.ChatBotDataSource
import com.project.job.data.source.remote.api.request.ChatBotRequest
import com.project.job.data.source.remote.api.request.ReferenceData
import com.project.job.data.source.remote.api.response.ChatBotResponse

class ChatBotRemote(private val chatApiService: ApiService) : ChatBotDataSource {
    override suspend fun chatBot(request: String, reference : ReferenceData): NetworkResult<ChatBotResponse> {
        return safeApiCall {
            chatApiService.chatBot(ChatBotRequest(request, reference))
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