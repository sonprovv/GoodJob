package com.project.job.data.repository

import com.project.job.data.network.RetrofitClient
import com.project.job.data.repository.implement.NotificationRepositoryImpl
import com.project.job.data.source.remote.api.response.NotificationInfo


class NotificationRepository : NotificationRepositoryImpl {
    private val apiService = RetrofitClient.apiService
    
    override suspend fun getNotifications(): Result<List<NotificationInfo>?> {
        return try {
            val response = apiService.getNotifications()
            if (response.isSuccessful) {
                val notificationResponse = response.body()
                if (notificationResponse?.success == true) {
                    Result.success(notificationResponse.notifications)
                } else {
                    Result.failure(Exception(notificationResponse?.message ?: "Failed to get notifications"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markNotificationAsRead(notificationID: String): Result<NotificationInfo?> {
        return try {
            val response = apiService.markNotificationAsRead(notificationID)
            if (response.isSuccessful) {
                val notificationResponse = response.body()
                if (notificationResponse?.success == true) {
                    // The API returns the updated notification list, but we need to find the specific notification
                    val updatedNotification = notificationResponse.notification
//                        .find { it.uid == notificationID }
                    Result.success(updatedNotification)
                } else {
                    Result.failure(Exception(notificationResponse?.message ?: "Failed to mark notification as read"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private var instance: NotificationRepository? = null

        fun getInstance(): NotificationRepository {
            if (instance == null) {
                instance = NotificationRepository()
            }
            return instance!!
        }
    }
}