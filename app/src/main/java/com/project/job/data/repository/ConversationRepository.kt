package com.project.job.data.repository

import com.project.job.data.source.local.room.entity.ChatEntity
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.response.chat.ConversationResponse
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    /**
     * Clear all local conversations
     */
    suspend fun clearLocalConversations()
}
