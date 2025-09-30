package com.project.job.data.source.remote

import android.util.Log
import com.project.job.data.network.ApiService
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.UserDataSource
import com.project.job.data.source.remote.api.request.ChangePasswordRequest
import com.project.job.data.source.remote.api.request.FCMTokenRequest
import com.project.job.data.source.remote.api.request.ForgotPasswordRequest
import com.project.job.data.source.remote.api.request.GoogleSignInRequest
import com.project.job.data.source.remote.api.request.LoginRequest
import com.project.job.data.source.remote.api.request.RefreshTokenRequest
import com.project.job.data.source.remote.api.request.RegisterRequest
import com.project.job.data.source.remote.api.request.SendMailRequest
import com.project.job.data.source.remote.api.request.toUpdateRequest
import com.project.job.data.source.remote.api.response.ChangePasswordResponse
import com.project.job.data.source.remote.api.response.FCMTokenResponse
import com.project.job.data.source.remote.api.response.ForgotPasswordResponse
import com.project.job.data.source.remote.api.response.RefreshTokenResponse
import com.project.job.data.source.remote.api.response.SendMailResponse
import com.project.job.data.source.remote.api.response.UpdateAvatarResponse
import com.project.job.data.source.remote.api.response.UpdateUserResponse
import com.project.job.data.source.remote.api.response.User
import com.project.job.data.source.remote.api.response.UserResponse
import com.project.job.data.source.remote.safeApiCall
import com.project.job.utils.ImageUploadDebugger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserRemote(private val apiService: ApiService) : UserDataSource {
    override suspend fun login(email: String, password: String): NetworkResult<UserResponse?> {
        return safeApiCall {
            apiService.login(LoginRequest(email, password))
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        username: String,
        avatar: String?
    ): NetworkResult<UserResponse?> {
        return safeApiCall {
            apiService.register(RegisterRequest(email, password, username, avatar, "user"))
        }
    }

    override suspend fun loginWithGoogle(
        firebaseIdToken: String,
    ): NetworkResult<UserResponse?> {
        return safeApiCall {  apiService.googleSignIn(
                GoogleSignInRequest(
                    idToken = firebaseIdToken,
                    role = "user"
                )
            )
        }
    }

    override suspend fun postFcmToken(fcmToken: String): NetworkResult<FCMTokenResponse?> {
        return safeApiCall {
            apiService.postFcmToken(FCMTokenRequest(fcmToken))
        }
    }

    override suspend fun deleteFcmToken(fcmToken: String): NetworkResult<FCMTokenResponse?> {
        return safeApiCall {
            apiService.deleteFcmToken(FCMTokenRequest(fcmToken))
        }
    }

    override suspend fun sendMailForgotPassword(email: String): NetworkResult<SendMailResponse?> {
        return safeApiCall {
            apiService.sendMail(SendMailRequest(email))
        }
    }

    override suspend fun forgotPassword(
        email: String,
        newPassword: String,
        code: String,
        confirmPassword: String,
        codeEnter: String
    ): NetworkResult<ForgotPasswordResponse?> {
        return safeApiCall {
             apiService.forgotPassword(
                ForgotPasswordRequest(
                    email = email,
                    newPassword = newPassword,
                    confirmPassword = confirmPassword,
                    code = code,
                    codeEnter = codeEnter
                )
            )
        }
    }

    override suspend fun changPassword(
        newPassword: String,
        confirmPassword: String
    ): NetworkResult<ChangePasswordResponse?> {
        return safeApiCall {
            apiService.changePassword(
                ChangePasswordRequest(
                    newPassword,
                    confirmPassword
                )
            )
        }
    }

    override suspend fun updateAvatar(imageFile: File): NetworkResult<UpdateAvatarResponse?> {
        return safeApiCall {
            // Create request body for file
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())

            // Create MultipartBody.Part
            val body = MultipartBody.Part.createFormData(
                "image",  // This must match the @Part parameter name in the API interface
                imageFile.name,
                requestFile
            )

            apiService.updateAvatar(body)
        }
    }

    override suspend fun updateProfile(user: User): NetworkResult<UpdateUserResponse?> {
        return safeApiCall {
            apiService.updateProfile(user.toUpdateRequest())
        }
    }

    override suspend fun refreshToken(refreshToken: String): NetworkResult<RefreshTokenResponse?> {
        return safeApiCall {
            apiService.refreshToken(RefreshTokenRequest(refreshToken))
        }
    }

    companion object {
        private var instance: UserRemote? = null
        fun getInstance(): UserRemote {
            if (instance == null) {
                instance = UserRemote(RetrofitClient.apiService)
            }
            return instance!!
        }
    }
}