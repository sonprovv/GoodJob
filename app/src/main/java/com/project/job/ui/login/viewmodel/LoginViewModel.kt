package com.project.job.ui.login.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.TokenRepository
import com.project.job.data.source.remote.NetworkResult
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.project.job.data.source.remote.UserRemote
import com.project.job.data.source.remote.api.response.UserResponse
import com.project.job.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(private val tokenRepository: TokenRepository) : ViewModel() {
    private val userRepository = UserRemote.getInstance()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    private val _loginResult = MutableStateFlow<UserResponse?>(null)
    val loginResult: StateFlow<UserResponse?> = _loginResult

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _successPUT = MutableStateFlow<Boolean>(false)
    val successPUT: StateFlow<Boolean> = _successPUT

    private val _user =
        MutableStateFlow<com.project.job.data.source.remote.api.response.User?>(null)
    val user: StateFlow<com.project.job.data.source.remote.api.response.User?> = _user

    // Refresh Token từ backend
    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken

    // Google Sign-In
    private val _googleSignInResult = MutableStateFlow<UserResponse?>(null)
    val googleSignInResult: StateFlow<UserResponse?> = _googleSignInResult

    // Google Sign-In
    fun loginWithGoogle(firebaseIdToken: String, fcmToken: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val response = userRepository.loginWithGoogle(firebaseIdToken)
                Log.d("LoginViewModel", "Google Sign-In response: $response")

                when (response) {
                    is NetworkResult.Success -> {
                        val authResponse = response.data

                        if (authResponse != null && authResponse.success) {
                            _error.value = null
                            _token.value = authResponse.data.token
                            _user.value = authResponse.data.user
                            _googleSignInResult.value = authResponse
                            tokenRepository.saveAccessToken(authResponse.data.token)
                            tokenRepository.saveRefreshToken(authResponse.data.refreshToken!!)
                            postFCMToken(fcmToken = fcmToken)
                        } else {
                            // Handle specific Google Sign-In errors
                            val errorMessage = authResponse?.message
                            _error.value = ErrorHandler.handleGoogleSignInError(errorMessage)
                            _googleSignInResult.value = null
                            Log.e("LoginViewModel", "Google Sign-In failed: $errorMessage")
                        }
                    }

                    is NetworkResult.Error -> {
                        // Handle HTTP error codes for Google Sign-In
                        _error.value = ErrorHandler.handleHttpError(response.message)
                        _googleSignInResult.value = null
                        Log.e("LoginViewModel", "Google Sign-In network error: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                _error.value = ErrorHandler.handleException(e)
                Log.e("LoginViewModel", "Google Sign-In exception: ${e.javaClass.simpleName} - ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchLogin(email: String, password: String, fcmToken: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val response = userRepository.login(email, password)
                Log.d("LoginViewModel", "Login response: $response")
                when (response) {
                    is NetworkResult.Success -> {
                        val authResponse = response.data
                        if (authResponse != null && authResponse.success) {
                            // Sign in to Firebase with email and password
                            try {
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).await()
                                Log.d("LoginViewModel", "Firebase signInWithEmailAndPassword successful")
                                
                                // Only proceed if Firebase sign-in succeeded
                                _error.value = null
                                _token.value = authResponse.data.token
                                _user.value = authResponse.data.user
                                tokenRepository.saveAccessToken(authResponse.data.token)
                                tokenRepository.saveRefreshToken(authResponse.data.refreshToken!!)
                                tokenRepository.saveFcmToken(fcmToken)
                                
                            } catch (e: Exception) {
                                Log.e("LoginViewModel", "Error signing in to Firebase", e)
                                _error.value = "Lỗi đăng nhập chat (Firebase): ${e.message}"
                            }
                        } else {
                            // Handle specific error messages from server
                            val errorMessage = authResponse?.message
                            _error.value = ErrorHandler.handleLoginError(errorMessage)
                            Log.e("LoginViewModel", "Login failed: $errorMessage")
                        }
                    }

                    is NetworkResult.Error -> {
                        // Handle HTTP error codes and network errors
                        _error.value = ErrorHandler.handleHttpError(response.message)
                        Log.e("LoginViewModel", "Network error: ${response.message}")
                    }
                }

            } catch (e: Exception) {
                _error.value = ErrorHandler.handleException(e)
                Log.e("LoginViewModel", "Login exception: ${e.javaClass.simpleName} - ${e.message}")
            } finally {
                Log.d("LoginViewModel", "Login process completed")
                _loading.value = false
            }
        }
    }

    fun postFCMToken(fcmToken: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val response = userRepository.postFcmToken(fcmToken)
                Log.d("LoginViewModel", "Post FCM Token response: $response")

                when(response){
                    is NetworkResult.Success -> {
                        val fcmResponse = response.data
                        Log.d("LoginViewModel", "Post FCM Token response: $fcmResponse")
                    }
                    is NetworkResult.Error -> {
                        _error.value = response.message ?: "Post FCM Token failed"
                    }
                }

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Post FCM Token error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("LoginViewModel", "Post FCM Token finally")
                _loading.value = false
            }

        }
    }

    fun putFCMToken(fcmToken: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _successPUT.value = false
            try {
                val response = userRepository.deleteFcmToken(fcmToken)
                Log.d("LoginViewModel", "Delete FCM Token response: $response")
                when(response){
                    is NetworkResult.Success -> {
                        val fcmResponse = response.data
                        Log.d("LoginViewModel", "Delete FCM Token response: $fcmResponse")
                    }
                    is NetworkResult.Error -> {
                        _error.value = response.message ?: "Delete FCM Token failed"
                    }
                }

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Delete FCM Token error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("LoginViewModel", "Delete FCM Token finally")
                _loading.value = false
            }

        }
    }

}