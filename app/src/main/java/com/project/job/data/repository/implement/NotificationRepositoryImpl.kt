package com.project.job.data.repository.implement

import com.project.job.data.source.remote.api.response.NotificationInfo

interface NotificationRepositoryImpl {
    suspend fun getNotifications(): Result<List<NotificationInfo>?>
    suspend fun markNotificationAsRead(notificationID: String): Result<NotificationInfo?>
}