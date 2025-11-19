package com.project.job.data.network

import com.project.job.data.source.remote.BaseResponse
import com.project.job.data.source.remote.api.request.ChatBotRequest
import com.project.job.data.source.remote.api.response.ChatBotResponse
import com.project.job.data.source.remote.api.response.chat.ConversationResponse

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
    @GET("api/chat/conversations")
    suspend fun chatGetConversations(): Response<ConversationResponse>

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

    // ----------- AI chat bot --------------------------------------
    @AuthRequired
    @POST("api/chatbot")
    suspend fun chatBot(
        @Body request : ChatBotRequest
    ) : Response<ChatBotResponse>
    // ----------- end AI chat bot --------------------------------------
}