package com.project.job.data.source.remote.api.response.chat

data class ConversationResponse(
    val conversationId: String,
    val partner: ChatUserResponse,
    val lastMessage: MessageResponse?,
    val unreadCount: Int = 0,
    val updatedAt: String? = null
)
