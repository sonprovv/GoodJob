package com.project.job.ui.login.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.TokenRepository
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.UserRemote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChangePasswordViewModel : ViewModel() {
    private val userRepository = UserRemote.getInstance()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success

    fun changPassword(newPassword: String, confirmPassword: String, token: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = userRepository.changPassword(
                    newPassword = newPassword,
                    confirmPassword = confirmPassword
                )
                Log.d("ChangePasswordViewModel", "ChangePassword response: $response")
                Log.d(
                    "ChangePasswordViewModel",
                    "ChangePassword response: $token, $newPassword, $confirmPassword"
                )
                if (response is NetworkResult.Success) {
                    _success.value = true
                } else if (response is NetworkResult.Error) {
                    _error.value = response.message
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _loading.value = false
            }
        }
    }
}