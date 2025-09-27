package com.project.job.data.source

import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.response.NotificationInfo

interface NotificationDataSource {
    suspend fun getNotifications(): NetworkResult<List<NotificationInfo>?>
    suspend fun markNotificationAsRead(notificationID: String) : NetworkResult<Boolean?>
}