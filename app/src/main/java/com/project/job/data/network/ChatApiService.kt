package com.project.job.data.network

import com.project.job.data.source.remote.BaseResponse
import com.project.job.data.source.remote.api.request.chat.SendMessageRequest
import com.project.job.data.source.remote.api.request.chat.UpdateStatusRequest
import com.project.job.data.source.remote.api.response.chat.ChatUserResponse
import com.project.job.data.source.remote.api.response.chat.ConversationResponse
import com.project.job.data.source.remote.api.response.chat.GetMessagesResponse
import com.project.job.data.source.remote.api.response.chat.MessageResponse
import com.project.job.data.source.remote.api.response.chat.UserStatusResponse
import com.project.job.utils.AuthRequired
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ChatApiService {
    @AuthRequired
    @POST("api/chat/send")
    suspend fun chatSendMessage(
        @Body request: SendMessageRequest
    ): Response<MessageResponse>

    @AuthRequired
    @GET("api/chat/messages/{userId}")
    suspend fun chatGetMessages(
        @Path("userId") userId: String
    ): Response<GetMessagesResponse>

    @AuthRequired
    @GET("api/chat/conversations")
    suspend fun chatGetConversations(): Response<ConversationResponse>

    @AuthRequired
    @GET("api/chat/available-users")
    suspend fun chatGetAvailableUsers(): Response<BaseResponse<List<ChatUserResponse>>>

    @AuthRequired
    @PUT("api/chat/read/{userId}")
    suspend fun chatMarkAsRead(
        @Path("userId") userId: String
    ): Response<BaseResponse<Unit>>

    @AuthRequired
    @DELETE("api/chat/message/{conversationId}/{messageId}")
    suspend fun chatDeleteMessage(
        @Path("conversationId") conversationId: String,
        @Path("messageId") messageId: String
    ): Response<BaseResponse<Unit>>

    @AuthRequired
    @DELETE("api/chat/conversation/{userId}")
    suspend fun chatDeleteConversation(
        @Path("userId") userId: String
    ): Response<BaseResponse<Unit>>

    @GET("api/chat/status/{userId}")
    suspend fun chatGetUserStatus(
        @Path("userId") userId: String
    ): Response<BaseResponse<UserStatusResponse>>

    @AuthRequired
    @POST("api/chat/status")
    suspend fun chatUpdateStatus(
        @Body request: UpdateStatusRequest
    ): Response<BaseResponse<UserStatusResponse>>

}