package com.project.job.ui.login.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChangePasswordViewModel : ViewModel() {
    private val userRepository = UserRepository()
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
                val response = userRepository.changPassword(token=token, newPassword=newPassword, confirmPassword=confirmPassword)
                Log.d("ChangePasswordViewModel", "ChangePassword response: $response")
                Log.d("ChangePasswordViewModel", "ChangePassword response: $token, $newPassword, $confirmPassword")

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null && userResponse.success) {
                        _error.value = null
                        Log.d("ChangePasswordViewModel", "ChangePassword successful: $userResponse")
                        _success.value = true
                    } else {
                        _error.value = userResponse?.message ?: "ChangePassword failed"
                    }
                } else {
                    _error.value = response.message() ?: "ChangePassword failed"
                }

            } catch (e: Exception) {
                Log.e("ChangePasswordViewModel", "ChangePassword error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("ChangePasswordViewModel", "ChangePassword finally")
                _loading.value = false
            }
        }
    }
}