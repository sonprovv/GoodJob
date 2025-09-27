package com.project.job.data.source.remote

import com.project.job.data.network.ApiService
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.UserDataSource
import com.project.job.data.source.remote.api.request.ChangePasswordRequest
import com.project.job.data.source.remote.api.request.FCMTokenRequest
import com.project.job.data.source.remote.api.request.ForgotPasswordRequest
import com.project.job.data.source.remote.api.request.GoogleSignInRequest
import com.project.job.data.source.remote.api.request.LoginRequest
import com.project.job.data.source.remote.api.request.RegisterRequest
import com.project.job.data.source.remote.api.request.SendMailRequest
import com.project.job.data.source.remote.api.request.toUpdateRequest
import com.project.job.data.source.remote.api.response.ChangePasswordResponse
import com.project.job.data.source.remote.api.response.FCMTokenResponse
import com.project.job.data.source.remote.api.response.ForgotPasswordResponse
import com.project.job.data.source.remote.api.response.SendMailResponse
import com.project.job.data.source.remote.api.response.UpdateAvatarResponse
import com.project.job.data.source.remote.api.response.UpdateUserResponse
import com.project.job.data.source.remote.api.response.User
import com.project.job.data.source.remote.api.response.UserResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserRemote(private val apiService: ApiService) : UserDataSource {
    override suspend fun login(email: String, password: String): NetworkResult<UserResponse?> {
        try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                return NetworkResult.Success(response.body())
            } else {
                return NetworkResult.Error("Something went wrong")
            }
        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        username: String,
        avatar: String?
    ): NetworkResult<UserResponse?> {
        try {
            val response =
                apiService.register(RegisterRequest(email, password, username, avatar, "user"))
            if (response.isSuccessful) {
                return NetworkResult.Success(response.body())
            } else {
                return NetworkResult.Error("Something went wrong")
            }
        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun loginWithGoogle(
        firebaseIdToken: String,
    ): NetworkResult<UserResponse?> {
        try {
            val response = apiService.googleSignIn(
                GoogleSignInRequest(
                    idToken = firebaseIdToken,
                    role = "user"
                )
            )
            if (response.isSuccessful) {
                return NetworkResult.Success(response.body())
            } else {
                return NetworkResult.Error("Something went wrong")
            }
        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Something went wrong")

        }
    }

    override suspend fun postFcmToken(fcmToken: String): NetworkResult<FCMTokenResponse?> {
        try {
            val response = apiService.postFcmToken(FCMTokenRequest(fcmToken))
            if (response.isSuccessful) {
                return NetworkResult.Success(response.body())
            } else {
                return NetworkResult.Error("Something went wrong")
            }
        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun deleteFcmToken(fcmToken: String): NetworkResult<FCMTokenResponse?> {
        try {
            val response = apiService.deleteFcmToken(FCMTokenRequest(fcmToken))
            if (response.isSuccessful) {
                return NetworkResult.Success(response.body())
            } else {
                return NetworkResult.Error("Something went wrong")
            }
        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun sendMailForgotPassword(email: String): NetworkResult<SendMailResponse?> {
        try {
            val response = apiService.sendMail(SendMailRequest(email))
            if (response.isSuccessful) {
                return NetworkResult.Success(response.body())
            } else {
                return NetworkResult.Error("Something went wrong")
            }
        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun forgotPassword(
        email: String,
        newPassword: String,
        code: String,
        confirmPassword: String,
        codeEnter: String
    ): NetworkResult<ForgotPasswordResponse?> {
        try {
            val response = apiService.forgotPassword(
                ForgotPasswordRequest(
                    email=email,
                    newPassword=newPassword,
                    confirmPassword=confirmPassword,
                    code=code,
                    codeEnter=codeEnter
                )
            )
            if (response.isSuccessful) {
                return NetworkResult.Success(response.body())
            } else {
                return NetworkResult.Error("Something went wrong")
            }
        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun changPassword(
        newPassword: String,
        confirmPassword: String
    ): NetworkResult<ChangePasswordResponse?> {
        try {
            val response = apiService.changePassword(
                ChangePasswordRequest(
                    newPassword,
                    confirmPassword
                )
            )
            if (response.isSuccessful) {
                return NetworkResult.Success(response.body())
            } else {
                return NetworkResult.Error("Something went wrong")
            }
        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun updateAvatar(imageFile: File): NetworkResult<UpdateAvatarResponse?> {
        try {
            // Create request body for file
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            
            // Create MultipartBody.Part
            val body = MultipartBody.Part.createFormData(
                "image",  // This must match the @Part parameter name in the API interface
                imageFile.name,
                requestFile
            )
            
            val response = apiService.updateAvatar(body)
            if (response.isSuccessful) {
                return NetworkResult.Success(response.body())
            } else {
                return NetworkResult.Error("Something went wrong")
            }
        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun updateProfile(user: User): NetworkResult<UpdateUserResponse?> {
        try {
            val response = apiService.updateProfile(user.toUpdateRequest())
            if (response.isSuccessful) {
                return NetworkResult.Success(response.body())
            } else {
                return NetworkResult.Error("Something went wrong")
            }
        } catch (e: Exception) {
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    companion object{
        private var instance: UserRemote? = null
        fun getInstance(): UserRemote {
            if (instance == null) {
                instance = UserRemote(RetrofitClient.apiService)
            }
            return instance!!
        }
    }
}