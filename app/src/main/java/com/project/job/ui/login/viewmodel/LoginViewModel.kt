package com.project.job.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.model.User
import com.project.job.data.repository.UserRepository
import com.project.job.data.source.remote.api.response.AuthResponse
import com.project.job.data.source.remote.api.response.UserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    private val _loginResult = MutableStateFlow<UserResponse?>(null)
    val loginResult: StateFlow<UserResponse?> = _loginResult

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token
    
    private val _user = MutableStateFlow<com.project.job.data.source.remote.api.response.User?>(null)
    val user: StateFlow<com.project.job.data.source.remote.api.response.User?> = _user
    
    // Google Sign-In
    private val _googleSignInResult = MutableStateFlow<UserResponse?>(null)
    val googleSignInResult: StateFlow<UserResponse?> = _googleSignInResult

    // Google Sign-In
    fun loginWithGoogle(firebaseIdToken: String, role : String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val response = userRepository.loginWithGoogle(firebaseIdToken, "user")
                Log.d("LoginViewModel", "Google Sign-In response: $response")
                
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null && authResponse.success) {
                        _error.value = null
                        _token.value = authResponse.data.token
                        _user.value = authResponse.data.user
                        _googleSignInResult.value = authResponse
                        Log.d("LoginViewModel", "Google Sign-In successful: $authResponse")
                    } else {
                        _error.value = authResponse?.message ?: "Authentication failed"
                        Log.e("LoginViewModel", "Google Sign-In failed: ${authResponse?.message}")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _error.value = "Error: ${response.code()} - $errorBody"
                    Log.e("LoginViewModel", "Google Sign-In error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
                Log.e("LoginViewModel", "Google Sign-In exception", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun fetchLogin(email: String, password: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val response = userRepository.login(email, password)
                Log.d("LoginViewModel", "Login response: $response")
                
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null && authResponse.success) {
                        _error.value = null
                        _token.value = authResponse.data.token
                        _user.value = authResponse.data.user
                        _loginResult.value = authResponse
                        Log.d("LoginViewModel", "Login successful: $authResponse")
                    } else {
                        _error.value = authResponse?.message ?: "Login failed"
                        _loginResult.value = null
                    }
                } else {
                    _error.value = response.message() ?: "Login failed"
                    _loginResult.value = null
                }

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("LoginViewModel", "Login finally")
                _loading.value = false
            }

        }
    }

}