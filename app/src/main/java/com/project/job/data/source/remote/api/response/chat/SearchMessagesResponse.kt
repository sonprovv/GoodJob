package com.project.job.data.source.remote.api.response.chat

data class SearchMessagesResponse(
    val success: Boolean,
    val message: String,
    val messages: List<ChatMessageData>
)
data class ChatMessageData(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val timestamp: Long,
    val avatar: String,
    val name: String,
    val status: String
)
