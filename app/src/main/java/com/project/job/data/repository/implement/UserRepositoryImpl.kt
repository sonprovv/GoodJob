package com.project.job.data.repository.implement

import com.project.job.data.source.remote.api.response.ChangePasswordResponse
import com.project.job.data.source.remote.api.response.FCMTokenResponse
import com.project.job.data.source.remote.api.response.ForgotPasswordResponse
import com.project.job.data.source.remote.api.response.SendMailResponse
import com.project.job.data.source.remote.api.response.UpdateAvatarResponse
import com.project.job.data.source.remote.api.response.UpdateUserResponse
import com.project.job.data.source.remote.api.response.User
import com.project.job.data.source.remote.api.response.UserResponse
import java.io.File

interface UserRepositoryImpl {
    suspend fun login(email: String, password: String): Result<UserResponse>
    suspend fun register(email: String, password: String, username: String, avatar: String? = null): Result<UserResponse>
    suspend fun loginWithGoogle(firebaseIdToken: String, role: String): Result<UserResponse>
    suspend fun postFcmToken(fcmToken: String): Result<FCMTokenResponse>
    suspend fun deleteFcmToken(fcmToken: String): Result<FCMTokenResponse>
    suspend fun sendMailForgotPassword(email: String): Result<SendMailResponse>
    suspend fun forgotPassword(email: String, newPassword: String, code: String): Result<ForgotPasswordResponse>
    suspend fun changPassword(newPassword: String, confirmPassword: String): Result<ChangePasswordResponse>
    suspend fun updateAvatar(imageFile: File): Result<UpdateAvatarResponse>
    suspend fun updateProfile(user: User): Result<UpdateUserResponse>
}