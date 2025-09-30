package com.project.job.data.source

import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.response.ChangePasswordResponse
import com.project.job.data.source.remote.api.response.FCMTokenResponse
import com.project.job.data.source.remote.api.response.ForgotPasswordResponse
import com.project.job.data.source.remote.api.response.RefreshTokenResponse
import com.project.job.data.source.remote.api.response.SendMailResponse
import com.project.job.data.source.remote.api.response.UpdateAvatarResponse
import com.project.job.data.source.remote.api.response.UpdateUserResponse
import com.project.job.data.source.remote.api.response.User
import com.project.job.data.source.remote.api.response.UserResponse
import java.io.File

interface UserDataSource {
    suspend fun login(email: String, password: String): NetworkResult<UserResponse?>
    suspend fun register(email: String, password: String, username: String, avatar: String? = null): NetworkResult<UserResponse?>
    suspend fun loginWithGoogle(firebaseIdToken: String): NetworkResult<UserResponse?>
    suspend fun postFcmToken(fcmToken: String): NetworkResult<FCMTokenResponse?>
    suspend fun deleteFcmToken(fcmToken: String): NetworkResult<FCMTokenResponse?>
    suspend fun sendMailForgotPassword(email: String): NetworkResult<SendMailResponse?>
    suspend fun forgotPassword(email: String, newPassword: String, confirmPassword: String, code: String, codeEnter: String): NetworkResult<ForgotPasswordResponse?>
    suspend fun changPassword(newPassword: String, confirmPassword: String): NetworkResult<ChangePasswordResponse?>
    suspend fun updateAvatar(imageFile: File): NetworkResult<UpdateAvatarResponse?>
    suspend fun updateProfile(user: User): NetworkResult<UpdateUserResponse?>
    suspend fun refreshToken(refreshToken: String): NetworkResult<RefreshTokenResponse?>
}