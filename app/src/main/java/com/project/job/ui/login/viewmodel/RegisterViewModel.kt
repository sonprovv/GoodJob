package com.project.job.ui.login.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.UserRepository
import com.project.job.data.source.remote.api.response.UserResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.project.job.data.source.remote.api.response.User
import kotlinx.coroutines.launch

class RegisterViewModel: ViewModel() {
    private val userRepository = UserRepository()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    private val _loginResult = MutableStateFlow<UserResponse?>(null)
    val loginResult: StateFlow<UserResponse?> = _loginResult

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = userRepository.register(email, password, username)
                Log.d("RegisterViewModel", "Register response: $response")

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null && userResponse.success) {
                        _error.value = null
                        _token.value = userResponse.data.token
                        _user.value = userResponse.data.user
                        _loginResult.value = userResponse
                        Log.d("RegisterViewModel", "Register successful: $userResponse")
                    } else {
                        _error.value = userResponse?.message ?: "Register failed"
                        _loginResult.value = null
                    }
                } else {
                    _error.value = response.message() ?: "Register failed"
                    _loginResult.value = null
                }

            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Register error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("RegisterViewModel", "Register finally")
                _loading.value = false
            }
        }
    }
}