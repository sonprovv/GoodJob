package com.project.job.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.project.job.data.network.RetrofitClient
import com.project.job.data.repository.implement.UserRepositoryImpl
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
import retrofit2.Response
import java.io.File
import java.io.IOException

class UserRepository() : UserRepositoryImpl {
    // Implement user-related data operations here
    private val apiService = RetrofitClient.apiService
    override suspend fun login(email: String, password: String): Result<UserResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true) {
                    Result.success(authResponse)
                } else {
                    Result.failure(Exception(authResponse?.message ?: "Login failed"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        username: String,
        avatar: String?
    ): Result<UserResponse> {
        return try {
            val response = apiService.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    username = username,
                    avatar = avatar,
                    role = "user"
                )
            )
            if (response.isSuccessful) {
                val authResponse = response.body()
                if (authResponse?.success == true) {
                    Result.success(authResponse)
                } else {
                    Result.failure(Exception(authResponse?.message ?: "Registration failed"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginWithGoogle(
        firebaseIdToken: String,
        role: String
    ): Result<UserResponse> {
        return try {
            Log.d(
                "UserRepository",
                "Sending Google Sign-In request with token: ${firebaseIdToken.take(10)}..."
            )
            Log.d("UserRepository", "ID Token: ${firebaseIdToken}")
            Log.d("UserRepository", "Role: $role")

            val requestBody = GoogleSignInRequest(
                idToken = firebaseIdToken,
                role = role
            )
            Log.d("UserRepository", "Request body: $requestBody")

            val response = apiService.googleSignIn(requestBody)
            Log.d("UserRepository", "Request sent to: ${response.raw().request.url}")

            if (response.isSuccessful) {
                val authResponse = response.body()
                
                // Log the raw response for debugging
                Log.d("UserRepository", "Raw response: ${response.raw()}")
                
                if (authResponse?.success == true) {
                    Log.d("UserRepository", "Google Sign-In successful: $authResponse")
                    
                    // Log the user data if available
                    authResponse.data?.user?.let { user ->
                        Log.d("UserRepository", "User data received: $user")
                    } ?: Log.e("UserRepository", "User data is null in the response")
                    
                    Result.success(authResponse)
                } else {
                    Log.e("UserRepository", "Google Sign-In failed: ${authResponse?.message}")
                    Result.failure(Exception(authResponse?.message ?: "Google Sign-In failed"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("UserRepository", "Google Sign-In failed: ${response.code()} - $errorBody")
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during Google Sign-In", e)
            Result.failure(e)
        }
    }

    override suspend fun postFcmToken(fcmToken: String): Result<FCMTokenResponse> {
        return try {
            val response = apiService.postFcmToken(FCMTokenRequest(fcmToken))
            if (response.isSuccessful) {
                val fcmResponse = response.body()
                if (fcmResponse?.success == true) {
                    Result.success(fcmResponse)
                } else {
                    Result.failure(Exception(fcmResponse?.message ?: "Failed to post FCM token"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteFcmToken(fcmToken: String): Result<FCMTokenResponse> {
        return try {
            val response = apiService.deleteFcmToken(FCMTokenRequest(fcmToken))
            if (response.isSuccessful) {
                val fcmResponse = response.body()
                if (fcmResponse?.success == true) {
                    Result.success(fcmResponse)
                } else {
                    Result.failure(Exception(fcmResponse?.message ?: "Failed to delete FCM token"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMailForgotPassword(email: String): Result<SendMailResponse> {
        return try {
            val response = apiService.sendMail(SendMailRequest(email))
            if (response.isSuccessful) {
                val mailResponse = response.body()
                if (mailResponse?.success == true) {
                    Result.success(mailResponse)
                } else {
                    Result.failure(Exception(mailResponse?.message ?: "Failed to send mail"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forgotPassword(
        email: String,
        newPassword: String,
        code: String
    ): Result<ForgotPasswordResponse> {
        return try {
            val response = apiService.forgotPassword(
                ForgotPasswordRequest(
                    email,
                    newPassword,
                    newPassword,
                    code,
                    code
                )
            )
            if (response.isSuccessful) {
                val forgotResponse = response.body()
                if (forgotResponse?.success == true) {
                    Result.success(forgotResponse)
                } else {
                    Result.failure(Exception(forgotResponse?.message ?: "Failed to reset password"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changPassword(
        newPassword: String,
        confirmPassword: String
    ): Result<ChangePasswordResponse> {
        return try {
            val response = apiService.changePassword(
                ChangePasswordRequest(newPassword, newPassword)
            )
            if (response.isSuccessful) {
                val changeResponse = response.body()
                if (changeResponse?.success == true) {
                    Result.success(changeResponse)
                } else {
                    Result.failure(Exception(changeResponse?.message ?: "Failed to change password"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAvatar(imageFile: File): Result<UpdateAvatarResponse> {
        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())

        // Create MultipartBody.Part
        val body = MultipartBody.Part.createFormData(
            "image",  // This must match the @Part parameter name in the API interface
            imageFile.name,
            requestFile
        )

        return try {
            val response = apiService.updateAvatar(body)
            if (response.isSuccessful) {
                val avatarResponse = response.body()
                if (avatarResponse?.success == true) {
                    Result.success(avatarResponse)
                } else {
                    Result.failure(Exception(avatarResponse?.message ?: "Failed to update avatar"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(
        user: User
    ): Result<UpdateUserResponse> {
        return try {
            val request =user.toUpdateRequest()
            Log.d("UserRepository", "Sending update request: $request")
            val response = apiService.updateProfile(request)
            if (response.isSuccessful) {
                val updateResponse = response.body()
                if (updateResponse?.success == true) {
                    Result.success(updateResponse)
                } else {
                    Result.failure(Exception(updateResponse?.message ?: "Failed to update profile"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


//    suspend fun postFcmToken(fcmToken: String): Response<FCMTokenResponse> {
//        return apiService.postFcmToken(FCMTokenRequest(fcmToken))
//    }
//
//    suspend fun deleteFcmToken(fcmToken: String): Response<FCMTokenResponse> {
//        return apiService.deleteFcmToken(FCMTokenRequest(fcmToken))
//    }
//
//    suspend fun login(email: String, password: String): Response<UserResponse> {
//        return apiService.login(LoginRequest(email, password))
//    }
//
//    suspend fun register(
//        email: String,
//        password: String,
//        username: String,
//        avatar: String? = null
//    ): Response<UserResponse> {
//        return apiService.register(
//            RegisterRequest(
//                email = email,
//                password = password,
//                username = username,
//                avatar = avatar,
//                role = "user"
//            )
//        )
//    }
//
//    suspend fun loginWithGoogle(firebaseIdToken: String, role: String): Response<UserResponse> {
//        try {
//            Log.d(
//                "UserRepository",
//                "Sending Google Sign-In request with token: ${firebaseIdToken.take(10)}..."
//            )
//            Log.d("UserRepository", "ID Token: ${firebaseIdToken}")
//            Log.d("UserRepository", "Role: $role")
//
//            val requestBody = mapOf(
//                "idToken" to firebaseIdToken,
//                "role" to role
//            )
//            Log.d("UserRepository", "Request body: $requestBody")
//
//            val response = apiService.googleSignIn(requestBody)
//            Log.d("UserRepository", "Request sent to: ${response.raw().request.url}")
//
//            if (response.isSuccessful) {
//                var authResponse = response.body()
//                Log.d("UserRepository", "Google Sign-In successful: $authResponse")
//
//                // Log the raw response for debugging
//                Log.d("UserRepository", "Raw response: ${response.raw()}")
//
//                // Log the user data if available
//                authResponse?.data?.user?.let { user ->
//                    Log.d("UserRepository", "User data received: $user")
//                } ?: Log.e("UserRepository", "User data is null in the response")
//            } else {
//                val errorBody = response.errorBody()?.string() ?: "No error body"
//                Log.e("UserRepository", "Google Sign-In failed: ${response.code()} - $errorBody")
//            }
//
//            return response
//        } catch (e: Exception) {
//            Log.e("UserRepository", "Exception during Google Sign-In", e)
//            throw e
//        }
//    }
//
//    suspend fun sendMailForgotPassword(email: String): Response<SendMailResponse> {
//        return apiService.sendMail(SendMailRequest(email))
//    }
//
//    suspend fun forgotPassword(
//        email: String,
//        newPassword: String,
//        confirmPassword: String,
//        code: String,
//        codeEnter: String
//    ): Response<ForgotPasswordResponse> {
//        return apiService.forgotPassword(
//            ForgotPasswordRequest(
//                email,
//                newPassword,
//                confirmPassword,
//                code,
//                codeEnter
//            )
//        )
//
//    }
//
//    suspend fun changPassword(
//        newPassword: String,
//        confirmPassword: String
//    ): Response<ChangePasswordResponse> {
//        return apiService.changePassword(
//            ChangePasswordRequest(newPassword, confirmPassword)
//        )
//    }
//
//    suspend fun updateAvatar(token: String, avatarUri: String): Response<UpdateAvatarResponse> {
//        val uri = Uri.parse(avatarUri)
//        val inputStream = safeContext.contentResolver.openInputStream(uri) ?:
//            throw IOException("Could not open input stream for URI: $avatarUri")
//
//        // Create a temporary file with proper extension
//        val fileExtension = safeContext.contentResolver.getType(uri)?.substringAfterLast("/") ?: "jpg"
//        val file = File.createTempFile("avatar_", ".$fileExtension", safeContext.cacheDir).apply {
//            outputStream().use { output ->
//                inputStream.copyTo(output)
//                inputStream.close()
//            }
//        }
//
//        // Create request body for file
//        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
//
//        // Create MultipartBody.Part
//        val body = MultipartBody.Part.createFormData(
//            "image",  // This must match the @Part parameter name in the API interface
//            file.name,
//            requestFile
//        )
//
//        return try {
//            apiService.updateAvatar(body).also {
//                // Clean up the temporary file
//                file.delete()
//            }
//        } catch (e: Exception) {
//            file.delete() // Ensure temp file is deleted even if there's an error
//            throw e
//        }
//    }
//
//    suspend fun updateProfile(user: User): Response<UpdateUserResponse> {
//        val request = user.toUpdateRequest()
//        Log.d("UserRepository", "Sending update request: $request")
//        return apiService.updateProfile( request)
//    }

}