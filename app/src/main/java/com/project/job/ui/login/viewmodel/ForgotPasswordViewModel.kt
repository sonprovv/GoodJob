package com.project.job.ui.login.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel: ViewModel() {
    private val userRepository = UserRepository()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    private val _code = MutableStateFlow<String?>(null)
    val code: StateFlow<String?> = _code

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_forgot: StateFlow<Boolean?> = _success

    fun sendMailForgotPassword(email: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = userRepository.sendMailForgotPassword(email)
                Log.d("ForgotPasswordViewModel", "ForgotPassword response: $response")

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null && userResponse.success) {
                        _error.value = null
                        _code.value = userResponse.code
                        Log.d("ForgotPasswordViewModel", "ForgotPassword successful: $userResponse")
                    } else {
                        _error.value = userResponse?.message ?: "ForgotPassword failed"
                    }
                } else {
                    _error.value = response.message() ?: "ForgotPassword failed"
                }

            } catch (e: Exception) {
                Log.e("ForgotPasswordViewModel", "ForgotPassword error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("ForgotPasswordViewModel", "ForgotPassword finally")
                _loading.value = false
            }
        }
    }

    fun forgotPassword(email: String, newPassword: String, confirmPassword: String, code: String, codeEnter: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = userRepository.forgotPassword(email, newPassword, confirmPassword, code, codeEnter)
                Log.d("ForgotPasswordViewModel", "ForgotPassword : $email, $newPassword, $confirmPassword, $code, $codeEnter")
                Log.d("ForgotPasswordViewModel", "ForgotPassword response: ${response.message()}, ${response.code()}, ${response.isSuccessful}, ${response.body()}")

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null && userResponse.success) {
                        _error.value = null
                        Log.d("ForgotPasswordViewModel", "ForgotPassword successful: $userResponse")
                        _success.value = true
                    } else {
                        _error.value = userResponse?.message ?: "ForgotPassword failed"
                    }
                } else {
                    _error.value = response.message() ?: "ForgotPassword failed"
                }

            } catch (e: Exception) {
                Log.e("ForgotPasswordViewModel", "ForgotPassword error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("ForgotPasswordViewModel", "ForgotPassword finally")
                _loading.value = false
            }
        }
    }
}