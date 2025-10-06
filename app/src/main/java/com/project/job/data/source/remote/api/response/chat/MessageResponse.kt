package com.project.job.data.source.remote.api.response.chat

data class MessageResponse(
    val id: String?,
    val conversationId: String?,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val type: String = "text",
    val createdAt: String? = null,
    val isRead: Boolean? = null
)
