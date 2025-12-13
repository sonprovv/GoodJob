package com.project.job.ui.login.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.TokenRepository
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.UserRemote
import com.project.job.data.source.remote.api.response.UserResponse
import com.project.job.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.project.job.data.source.remote.api.response.User
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException

class RegisterViewModel(private val tokenRepository: TokenRepository) : ViewModel() {
    private val userRepository = UserRemote.getInstance()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _registerResult = MutableStateFlow<UserResponse?>(null)
    val registerResult: StateFlow<UserResponse?> = _registerResult

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    fun register(email: String, password: String, confirmPassword: String, fcmToken: String = "") {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = userRepository.register(email=email, password=password, confirmPassword = confirmPassword)
                Log.d("RegisterViewModel", "Register response: $response")

                when (response) {
                    is NetworkResult.Success -> {
                        val authResponse = response.data

                        if (authResponse != null && authResponse.success) {
                            _error.value = null
                            _token.value = authResponse.data.token
                            _user.value = authResponse.data.user

                            tokenRepository.saveAccessToken(authResponse.data.token)
                            tokenRepository.saveRefreshToken(authResponse.data.refreshToken)
                            postFCMToken(fcmToken)
                            _registerResult.value = authResponse
                        } else {
                            // Handle specific register error messages from server
                            val errorMessage = authResponse?.message
                            _error.value = ErrorHandler.handleRegisterError(errorMessage)
                            _registerResult.value = null
                            Log.e("RegisterViewModel", "Register failed: $errorMessage")
                        }
                    }

                    is NetworkResult.Error -> {
                        // Handle HTTP error codes and network errors
                        _error.value = ErrorHandler.handleHttpError(response.message)
                        _registerResult.value = null
                        Log.e("RegisterViewModel", "Network error: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                _error.value = ErrorHandler.handleException(e)
                _registerResult.value = null
                Log.e("RegisterViewModel", "Register exception: ${e.javaClass.simpleName} - ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    private fun postFCMToken( fcmToken: String) {
        viewModelScope.launch {
            try {
                val response = userRepository.postFcmToken( fcmToken)
                Log.d("RegisterViewModel", "Post FCM Token response: $response")

                when (response) {
                    is NetworkResult.Success -> {
                        val fcmResponse = response.data
                        Log.d("RegisterViewModel", "Post FCM Token response: $fcmResponse")
                    }
                    is NetworkResult.Error -> {
                        _error.value = response.message
                    }
                }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Post FCM Token error: ${e.message}")
            }
        }
    }
}