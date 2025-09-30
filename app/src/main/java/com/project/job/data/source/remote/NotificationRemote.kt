package com.project.job.data.source.remote

import com.project.job.data.network.ApiService
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.NotificationDataSource
import com.project.job.data.source.remote.api.response.GetNotificationResponse
import com.project.job.data.source.remote.api.response.GetNotificationsResponse
import com.project.job.data.source.remote.api.response.NotificationInfo

class NotificationRemote(private val apiService: ApiService) : NotificationDataSource {
    override suspend fun getNotifications(): NetworkResult<GetNotificationsResponse> {
        return safeApiCall { apiService.getNotifications() }
    }

    override suspend fun markNotificationAsRead(notificationID: String): NetworkResult<GetNotificationResponse> {
        return safeApiCall { apiService.markNotificationAsRead(notificationID) }
    }

    companion object {
        private var instance: NotificationRemote? = null
        fun getInstance(): NotificationRemote {
            if (instance == null) {
                instance = NotificationRemote(RetrofitClient.apiService)
            }
            return instance!!
        }
    }
}