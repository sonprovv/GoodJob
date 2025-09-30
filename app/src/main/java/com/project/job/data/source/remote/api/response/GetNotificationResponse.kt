package com.project.job.data.source.remote.api.response

data class GetNotificationResponse(
    val success: Boolean,
    val message: String,
    val notification: NotificationInfo
)
