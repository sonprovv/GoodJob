package com.project.job.data.source.remote.api.response

data class GetNotificationsResponse(
    val success: Boolean,
    val message: String,
    val notifications: List<NotificationInfo>
)

data class NotificationInfo(
    val uid: String,
//    val jobID: String,
    val title: String,
    val content: String,
    val isRead: Boolean,
    val time: String,
//    val serviceType: String,
    val createdAt: String,

    // Chat notification fields
    val notificationType: String? = null, // "Chat" for chat notifications
    val senderId: String? = null,
    val senderName: String? = null,
    val senderAvatar: String? = null,
    val messageType: String? = null // "text", "image", "file"
)
