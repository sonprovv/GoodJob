package com.project.job.data.repository

import com.project.job.data.repository.implement.ChatRepositoryImpl
import com.project.job.data.source.ChatDataSource
import com.project.job.data.source.remote.BaseResponse
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.chat.*
import com.project.job.data.source.remote.api.response.chat.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val remote: ChatDataSource.Remote,
    private val local: ChatDataSource.Local
) : ChatRepositoryImpl {

    private fun <T> mapResult(result: NetworkResult<T>): Result<T> = when (result) {
        is NetworkResult.Success -> Result.success(result.data)
        is NetworkResult.Error -> Result.failure(Exception(result.message))
    }

    //region Message Operations
    override suspend fun sendMessage(request: SendMessageRequest): Result<MessageResponse> {
        return try {
            val result = remote.sendMessage(request)
            if (result is NetworkResult.Success) {
                local.saveMessage(result.data)
            }
            mapResult(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMessages(conversationId: String): Flow<List<MessageResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshMessages(conversationId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun editMessage(messageId: String, content: String): Result<MessageResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessage(
        conversationId: String,
        messageId: String
    ): Result<BaseResponse<Unit>> {
        TODO("Not yet implemented")
    }

    override fun getConversations(): Flow<List<ConversationResponse>> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshConversations(): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getConversation(conversationId: String): Result<ConversationResponse> {
        TODO("Not yet implemented")
    }


    override suspend fun deleteConversation(conversationId: String): Result<BaseResponse<Unit>> {
        return try {
            val result = remote.deleteConversation(conversationId)
            if (result is NetworkResult.Success) {
                local.deleteConversation(conversationId)
            }
            mapResult(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
