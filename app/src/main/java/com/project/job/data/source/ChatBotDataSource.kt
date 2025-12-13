package com.project.job.data.source

import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.ReferenceData
import com.project.job.data.source.remote.api.response.ChatBotResponse

interface ChatBotDataSource {
    suspend fun chatBot(request: String, reference : ReferenceData) : NetworkResult<ChatBotResponse>
}