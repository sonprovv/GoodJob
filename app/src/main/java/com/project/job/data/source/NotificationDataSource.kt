package com.project.job.data.source

import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.response.GetNotificationResponse
import com.project.job.data.source.remote.api.response.GetNotificationsResponse

interface NotificationDataSource {
    suspend fun getNotifications(): NetworkResult<GetNotificationsResponse>
    suspend fun markNotificationAsRead(notificationID: String) : NetworkResult<GetNotificationResponse>
}