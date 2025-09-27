package com.project.job.data.source.remote

import com.project.job.data.network.ApiService
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.NotificationDataSource
import com.project.job.data.source.remote.api.response.NotificationInfo

class NotificationRemote(private val apiService: ApiService) : NotificationDataSource {
    override suspend fun getNotifications(): NetworkResult<List<NotificationInfo>?> {
        try {
            val response = apiService.getNotifications()
            if (response.isSuccessful) {
                return NetworkResult.Success(response.body()?.notifications)
            } else {
                return NetworkResult.Error("Something went wrong")
            }
        }
        catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun markNotificationAsRead(notificationID: String): NetworkResult<Boolean?> {
        try {
            val response = apiService.markNotificationAsRead(notificationID)
            if (response.isSuccessful) {
                return NetworkResult.Success(response.body()?.success)
            } else {
                return NetworkResult.Error("Something went wrong")
            }
        }
        catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }

    }

    companion object{
        private var instance: NotificationRemote? = null
        fun getInstance(): NotificationRemote {
            if (instance == null) {
                instance = NotificationRemote(RetrofitClient.apiService)
            }
            return instance!!
        }
    }
}