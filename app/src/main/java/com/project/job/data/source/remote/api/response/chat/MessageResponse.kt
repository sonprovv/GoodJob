package com.project.job.data.source.remote.api.response.chat

data class MessageResponse(
    val success: Boolean,
    val message: MessageData,
)

data class MessageData(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val type: String = "text",
    val createdAt: String,
    val isRead: Boolean
)
