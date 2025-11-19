package com.project.job.data.source

import com.project.job.data.source.remote.BaseResponse
import com.project.job.data.source.remote.NetworkResult
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
    }

    /**
     * Remote data source for chat operations
     */
    interface Remote {
        suspend fun markAsRead(userId: String): NetworkResult<BaseResponse<Unit>>
        suspend fun deleteMessage(
            conversationId: String,
            messageId: String
        ): NetworkResult<BaseResponse<Unit>>
        
        suspend fun deleteConversation(userId: String): NetworkResult<BaseResponse<Unit>>
    }
}
