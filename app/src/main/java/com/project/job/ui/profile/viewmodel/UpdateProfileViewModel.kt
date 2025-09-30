package com.project.job.ui.profile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.UserRemote
import com.project.job.data.source.remote.api.response.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UpdateProfileViewModel : ViewModel() {
    private val userRepository = UserRemote.getInstance()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<Boolean>(false)
    val success_change: StateFlow<Boolean> = _success

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    fun updateProfile(user: User) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                Log.d("UpdateProfileViewModel", "Updating user: $user")
                val response = userRepository.updateProfile( user = user)
                
                when(response) {
                    is NetworkResult.Success -> {
                        _userData.value = response.data?.user
                        _success.value = true
                    }
                    is NetworkResult.Error -> {
                        _error.value = response.message
                        _success.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateProfileViewModel", "UpdateProfile error", e)
                _error.value = e.message ?: "Có lỗi xảy ra"
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun updateAvatar(avatarFile: java.io.File, user: User) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            
            try {
                Log.d("UpdateProfileViewModel", "Starting avatar upload for user: ${user.uid}")
                
                // Check if file exists
                if (!avatarFile.exists()) {
                    _error.value = "File ảnh không tồn tại"
                    return@launch
                }
                
                // First upload the avatar
                val avatarResponse = userRepository.updateAvatar(avatarFile)
                Log.d("UpdateProfileViewModel", "Avatar upload response: $avatarResponse")

                when (avatarResponse) {
                    is NetworkResult.Success -> {
                        // Backend có thể trả về url hoặc data field
                        val newAvatarUrl = avatarResponse.data?.url ?: avatarResponse.data?.data
                        
                        Log.d("UpdateProfileViewModel", "Avatar response: ${avatarResponse.data}")
                        Log.d("UpdateProfileViewModel", "Extracted avatar URL: $newAvatarUrl")
                        
                        if (!newAvatarUrl.isNullOrEmpty()) {
                            Log.d("UpdateProfileViewModel", "Avatar upload successful: $newAvatarUrl")
                            
                            // Create a new user object with the updated avatar URL
                            val updatedUser = user.copy(avatar = newAvatarUrl)
                            
                            // Update the user profile with the new avatar URL
                            updateProfileWithAvatar(updatedUser)
                        } else {
                            Log.e("UpdateProfileViewModel", "Avatar URL is null or empty")
                            _error.value = "Không thể lấy URL ảnh đại diện mới"
                        }
                    }
                    
                    is NetworkResult.Error -> {
                        Log.e("UpdateProfileViewModel", "Avatar upload failed: ${avatarResponse.message}")
                        _error.value = avatarResponse.message ?: "Lỗi khi tải lên ảnh đại diện"
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateProfileViewModel", "Update avatar error", e)
                _error.value = e.message ?: "Có lỗi xảy ra khi tải lên ảnh đại diện"
            } finally {
                _loading.value = false
            }
        }
    }
    
    private suspend fun updateProfileWithAvatar(user: User) {
        try {
            Log.d("UpdateProfileViewModel", "Updating profile with new avatar: ${user.avatar}")
            val response = userRepository.updateProfile(user = user)
            
            when (response) {
                is NetworkResult.Success -> {
                    _userData.value = response.data?.user
                    _success.value = true
                    Log.d("UpdateProfileViewModel", "Profile updated successfully with new avatar")
                }
                is NetworkResult.Error -> {
                    Log.e("UpdateProfileViewModel", "Profile update failed: ${response.message}")
                    _error.value = response.message ?: "Lỗi khi cập nhật thông tin người dùng"
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateProfileViewModel", "Profile update error", e)
            _error.value = e.message ?: "Có lỗi xảy ra khi cập nhật thông tin"
        }
    }
}