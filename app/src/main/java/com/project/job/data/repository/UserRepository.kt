package com.project.job.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.remote.api.request.ChangePasswordRequest
import com.project.job.data.source.remote.api.request.ForgotPasswordRequest
import com.project.job.data.source.remote.api.request.LoginRequest
import com.project.job.data.source.remote.api.request.RegisterRequest
import com.project.job.data.source.remote.api.request.SendMailRequest
import com.project.job.data.source.remote.api.request.UpdateUserRequest
import com.project.job.data.source.remote.api.request.toUpdateRequest
import com.project.job.data.source.remote.api.response.ChangePasswordResponse
import com.project.job.data.source.remote.api.response.ForgotPasswordResponse
import com.project.job.data.source.remote.api.response.SendMailResponse
import com.project.job.data.source.remote.api.response.UpdateAvatarResponse
import com.project.job.data.source.remote.api.response.UpdateUserResponse
import com.project.job.data.source.remote.api.response.User
import com.project.job.data.source.remote.api.response.UserResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.IOException

class UserRepository(private val context: Context? = null) {
    // Implement user-related data operations here
    private val apiService = RetrofitClient.apiService
    
    private val safeContext: Context
        get() = context ?: if (RetrofitClient.isInitialized()) {
            RetrofitClient.context
        } else {
            throw IllegalStateException("Context not provided and RetrofitClient not initialized")
        }

    suspend fun login(email: String, password: String): Response<UserResponse> {
        return apiService.login(LoginRequest(email, password))
    }

    suspend fun register(
        email: String,
        password: String,
        username: String,
        avatar: String? = null
    ): Response<UserResponse> {
        return apiService.register(
            RegisterRequest(
                email = email,
                password = password,
                username = username,
                avatar = avatar,
                role = "user"
            )
        )
    }

    suspend fun loginWithGoogle(firebaseIdToken: String, role: String): Response<UserResponse> {
        try {
            Log.d(
                "UserRepository",
                "Sending Google Sign-In request with token: ${firebaseIdToken.take(10)}..."
            )
            Log.d("UserRepository", "ID Token: ${firebaseIdToken}")
            Log.d("UserRepository", "Role: $role")

            val requestBody = mapOf(
                "idToken" to firebaseIdToken,
                "role" to role
            )
            Log.d("UserRepository", "Request body: $requestBody")

            val response = apiService.googleSignIn(requestBody)
            Log.d("UserRepository", "Request sent to: ${response.raw().request.url}")

            if (response.isSuccessful) {
                var authResponse = response.body()
                Log.d("UserRepository", "Google Sign-In successful: $authResponse")

                // Log the raw response for debugging
                Log.d("UserRepository", "Raw response: ${response.raw()}")

                // Log the user data if available
                authResponse?.data?.user?.let { user ->
                    Log.d("UserRepository", "User data received: $user")
                } ?: Log.e("UserRepository", "User data is null in the response")
            } else {
                val errorBody = response.errorBody()?.string() ?: "No error body"
                Log.e("UserRepository", "Google Sign-In failed: ${response.code()} - $errorBody")
            }

            return response
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception during Google Sign-In", e)
            throw e
        }
    }

    suspend fun sendMailForgotPassword(email: String): Response<SendMailResponse> {
        return apiService.sendMail(SendMailRequest(email))
    }

    suspend fun forgotPassword(
        email: String,
        newPassword: String,
        confirmPassword: String,
        code: String,
        codeEnter: String
    ): Response<ForgotPasswordResponse> {
        return apiService.forgotPassword(
            ForgotPasswordRequest(
                email,
                newPassword,
                confirmPassword,
                code,
                codeEnter
            )
        )

    }

    suspend fun changPassword(
        token: String,
        newPassword: String,
        confirmPassword: String
    ): Response<ChangePasswordResponse> {
        return apiService.changePassword(
            "Bearer $token",
            ChangePasswordRequest(newPassword, confirmPassword)
        )
    }

    suspend fun updateAvatar(token: String, avatarUri: String): Response<UpdateAvatarResponse> {
        val uri = Uri.parse(avatarUri)
        val inputStream = safeContext.contentResolver.openInputStream(uri) ?: 
            throw IOException("Could not open input stream for URI: $avatarUri")
            
        // Create a temporary file with proper extension
        val fileExtension = safeContext.contentResolver.getType(uri)?.substringAfterLast("/") ?: "jpg"
        val file = File.createTempFile("avatar_", ".$fileExtension", safeContext.cacheDir).apply {
            outputStream().use { output ->
                inputStream.copyTo(output)
                inputStream.close()
            }
        }
        
        // Create request body for file
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        
        // Create MultipartBody.Part
        val body = MultipartBody.Part.createFormData(
            "image",  // This must match the @Part parameter name in the API interface
            file.name,
            requestFile
        )
        
        return try {
            apiService.updateAvatar("Bearer $token", body).also {
                // Clean up the temporary file
                file.delete()
            }
        } catch (e: Exception) {
            file.delete() // Ensure temp file is deleted even if there's an error
            throw e
        }
    }

    suspend fun updateProfile(user: User, token: String): Response<UpdateUserResponse> {
        val request = user.toUpdateRequest()
        Log.d("UserRepository", "Sending update request: $request")
        return apiService.updateProfile("Bearer $token", request)
    }

}