package com.project.job.ui.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.UserRepository
import com.project.job.data.source.remote.api.response.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UpdateProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<Boolean>(false)
    val success_change: StateFlow<Boolean> = _success

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    fun updateProfile(user: User, token: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                Log.d("UpdateProfileViewModel", "Updating user: $user")
                val response = userRepository.updateProfile(token = token, user = user)
                
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    Log.d("UpdateProfileViewModel", "UpdateProfile response: $userResponse")
                    
                    when {
                        userResponse?.success == true -> {
                            // Handle successful response with user data
                            val updatedUser = userResponse.data?.user ?: userResponse.user ?: user
                            _userData.value = updatedUser
                            _success.value = true
                            _error.value = null
                        }
                        !userResponse?.message.isNullOrEmpty() -> {
                            // Handle error message from server
                            _error.value = userResponse?.message ?: "Cập nhật thất bại"
                        }
                        else -> {
                            _error.value = "Có lỗi xảy ra khi cập nhật thông tin"
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("UpdateProfileViewModel", "UpdateProfile error: $errorBody")
                    _error.value = errorBody ?: "Lỗi mạng: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("UpdateProfileViewModel", "UpdateProfile error", e)
                _error.value = e.message ?: "Có lỗi xảy ra"
            } finally {
                _loading.value = false
            }
        }
    }


    private var pendingProfileUpdate: User? = null
    private var pendingToken: String? = null

    fun updateAvatar(token: String, avatar: String, user: User) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                // First upload the avatar
                val response = userRepository.updateAvatar(token, avatar)
                Log.d("UpdateProfileViewModel", "UpdateAvatar response: $response")

                if (response.isSuccessful) {
                    val avatarResponse = response.body()
                    val newAvatarUrl = avatarResponse?.url
                    
                    if (!newAvatarUrl.isNullOrEmpty()) {
                        Log.d("UpdateProfileViewModel", "Avatar upload successful: $newAvatarUrl")
                        
                        // Create a new user object with the updated avatar URL
                        val updatedUser = user.copy(avatar = newAvatarUrl)
                        
                        // Update the user profile with the new avatar URL
                        updateProfile(updatedUser, token)
                    } else {
                        _error.value = "Không thể lấy URL ảnh đại diện mới"
                        _loading.value = false
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("UpdateProfileViewModel", "Avatar upload error: $errorBody")
                    _error.value = errorBody ?: "Lỗi khi tải lên ảnh đại diện"
                    _loading.value = false
                }
            } catch (e: Exception) {
                Log.e("UpdateProfileViewModel", "Update avatar error", e)
                _error.value = e.message ?: "Có lỗi xảy ra khi tải lên ảnh đại diện"
                _loading.value = false
            }
        }
    }
}