package com.project.job.data.repository

import com.project.job.data.repository.implement.ChatRepositoryImpl
import com.project.job.data.source.remote.BaseResponse
import com.project.job.data.source.remote.ChatRemote
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.chat.SendMessageRequest
import com.project.job.data.source.remote.api.response.chat.ChatUserResponse
import com.project.job.data.source.remote.api.response.chat.ConversationResponse
import com.project.job.data.source.remote.api.response.chat.MessageResponse
import com.project.job.data.source.remote.api.response.chat.UserStatusResponse

class ChatRepository(
    private val remote: ChatRemote = ChatRemote.getInstance()
) : ChatRepositoryImpl {

    private fun <T> mapResult(result: NetworkResult<T>): Result<T> = when (result) {
        is NetworkResult.Success -> Result.success(result.data)
        is NetworkResult.Error -> Result.failure(Exception(result.message))
    }

    override suspend fun sendMessage(request: SendMessageRequest): Result<BaseResponse<MessageResponse>> =
        mapResult(remote.sendMessage(request))

    override suspend fun getMessages(userId: String, limit: Int): Result<BaseResponse<List<MessageResponse>>> =
        mapResult(remote.getMessages(userId, limit))

    override suspend fun getConversations(): Result<BaseResponse<List<ConversationResponse>>> =
        mapResult(remote.getConversations())

    override suspend fun getAvailableUsers(): Result<BaseResponse<List<ChatUserResponse>>> =
        mapResult(remote.getAvailableUsers())

    override suspend fun markAsRead(userId: String): Result<BaseResponse<Unit>> =
        mapResult(remote.markAsRead(userId))

    override suspend fun deleteMessage(conversationId: String, messageId: String): Result<BaseResponse<Unit>> =
        mapResult(remote.deleteMessage(conversationId, messageId))

    override suspend fun deleteConversation(userId: String): Result<BaseResponse<Unit>> =
        mapResult(remote.deleteConversation(userId))

    override suspend fun getUserStatus(userId: String): Result<BaseResponse<UserStatusResponse>> =
        mapResult(remote.getUserStatus(userId))

    override suspend fun updateStatus(state: String): Result<BaseResponse<UserStatusResponse>> =
        mapResult(remote.updateStatus(com.project.job.data.source.remote.api.request.chat.UpdateStatusRequest(state)))
}
