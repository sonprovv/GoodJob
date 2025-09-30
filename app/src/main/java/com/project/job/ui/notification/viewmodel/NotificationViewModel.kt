package com.project.job.ui.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.NotificationRemote
import com.project.job.data.source.remote.api.response.NotificationInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val notificationRepository = NotificationRemote.getInstance()

    private val _notifications = MutableStateFlow<List<NotificationInfo>>(emptyList())
    val notifications: StateFlow<List<NotificationInfo>> = _notifications

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success

    private val _markReadSuccess = MutableStateFlow<Boolean?>(null)
    val markReadSuccess: StateFlow<Boolean?> = _markReadSuccess

    fun getNotifications() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = notificationRepository.getNotifications()
                when(response) {
                    is NetworkResult.Success -> {
                        _notifications.value = response.data.notifications
                    }
                    is NetworkResult.Error -> {
                        _error.value = response.message
                        _notifications.value = emptyList()
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
    fun markNotificationAsRead(notificationID: String) {
        viewModelScope.launch {
            _markReadSuccess.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = notificationRepository.markNotificationAsRead(notificationID)
                when(response) {
                    is NetworkResult.Success -> {
                        _markReadSuccess.value = true
                    }
                    is NetworkResult.Error -> {
                        _error.value = response.message
                        _markReadSuccess.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}