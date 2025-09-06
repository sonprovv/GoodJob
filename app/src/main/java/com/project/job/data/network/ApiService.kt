package com.project.job.data.network

import com.project.job.data.source.remote.api.request.ChangePasswordRequest
import com.project.job.data.source.remote.api.request.ForgotPasswordRequest
import com.project.job.data.source.remote.api.request.LoginRequest
import com.project.job.data.source.remote.api.request.RegisterRequest
import com.project.job.data.source.remote.api.request.SendMailRequest
import com.project.job.data.source.remote.api.response.ChangePasswordResponse
import com.project.job.data.source.remote.api.response.ForgotPasswordResponse
import com.project.job.data.source.remote.api.response.SendMailResponse
import com.project.job.data.source.remote.api.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface ApiService {
    @POST("api/users/me")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<UserResponse>

    @POST("api/users/create")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<UserResponse>
    
    @POST("api/users/loginGG")
    suspend fun googleSignIn(
        @Body request: Map<String, String>
    ): Response<UserResponse>

    @POST("api/emails/send")
    suspend fun sendMail(
        @Body request: SendMailRequest
    ): Response<SendMailResponse>

    @PUT("api/users/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ) : Response<ForgotPasswordResponse>

    @PUT("api/users/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ) : Response<ChangePasswordResponse>

}
