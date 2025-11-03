package com.project.job.data.source.remote.api.response.chat

data class ConversationResponse(
    val success: Boolean,
    val message: String,
    val conversations: List<ConversationData>,
)

data class ConversationData(
    val id: String,
    val lastMessageTime: Long,  // Changed to Long (timestamp)
    val lastMessage: String?,    // Changed to String (direct message text)
    val unreadCount: Int = 0,
    val updatedAt: String,
    val senderId: String,
    val sender: SenderData
)

data class SenderData(
    val id: String,
    val username: String,
    val name: String,
    val avatar: String,
    val dob : String,
    val tel: String,
    val email: String,
    val location: String,
    val gender: String,
    val userType: String
)
