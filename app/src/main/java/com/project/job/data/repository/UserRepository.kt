package com.project.job.data.repository

import android.util.Log
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.remote.api.request.ForgotPasswordRequest
import com.project.job.data.source.remote.api.request.LoginRequest
import com.project.job.data.source.remote.api.request.RegisterRequest
import com.project.job.data.source.remote.api.request.SendMailRequest
import com.project.job.data.source.remote.api.response.ForgotPasswordResponse
import com.project.job.data.source.remote.api.response.SendMailResponse
import com.project.job.data.source.remote.api.response.UserResponse
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Response

class UserRepository {
    // Implement user-related data operations here
    private val apiService = RetrofitClient.apiService

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
            Log.d("UserRepository", "Sending Google Sign-In request with token: ${firebaseIdToken.take(10)}...")
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
                authResponse?.data?.user?.google = true
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

    suspend fun forgotPassword(email: String, newPassword: String, confirmPassword: String, code: String, codeEnter: String): Response<ForgotPasswordResponse> {
        return apiService.forgotPassword(ForgotPasswordRequest( email, newPassword, confirmPassword, code, codeEnter))

    }

}