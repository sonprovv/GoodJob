package com.project.job.ui.login.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.UserRemote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    private val userRepository = UserRemote.getInstance()

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
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = userRepository.sendMailForgotPassword(email)
                Log.d("ForgotPasswordViewModel", "ForgotPassword response: $response")
                when (response) {
                    is NetworkResult.Success -> {
                        val responseBody = response.data
                        if (responseBody != null) {
                            _code.value = responseBody.code
                            _success.value = true
                        } else {
                            _error.value =
                                responseBody?.message ?: "Failed to send mail for password reset"
                        }
                    }

                    is NetworkResult.Error -> {
                        _error.value = response.message ?: "Failed to send mail for password reset"
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _loading.value = false

            }
        }
    }

    fun forgotPassword(
        email: String,
        newPassword: String,
        code: String,
        confirmPassword: String,
        codeEnter: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = userRepository.forgotPassword(
                    email = email,
                    newPassword = newPassword,
                    code = code,
                    confirmPassword = confirmPassword,
                    codeEnter = codeEnter

                )
                Log.d(
                    "ForgotPasswordViewModel",
                    "ForgotPassword : $email, $newPassword, $code"
                )

                when (response) {
                    is NetworkResult.Success -> {
                        val responseBody = response.data
                        if (responseBody != null) {
                            _success.value = true
                        } else {
                            _error.value = responseBody?.message ?: "Failed to reset password"
                        }
                    }

                    is NetworkResult.Error -> {
                        _error.value = response.message ?: "Failed to reset password"
                    }

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