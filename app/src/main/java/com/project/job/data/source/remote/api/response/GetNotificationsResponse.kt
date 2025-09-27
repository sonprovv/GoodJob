package com.project.job.data.source.remote.api.response

data class GetNotificationsResponse(
    val success: Boolean,
    val message: String,
    val notifications: List<NotificationInfo>
)

data class NotificationInfo(
    val uid: String,
    val jobID: String,
    val title: String,
    val content: String,
    val isRead: Boolean,
    val time : String,
    val serviceType: String,
    val createdAt: String,
)
