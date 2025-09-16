package com.project.job.data.network

import com.project.job.data.source.remote.api.request.ChangePasswordRequest
import com.project.job.data.source.remote.api.request.ChoiceWorkerRequest
import com.project.job.data.source.remote.api.request.CreateJobHealthcareRequest
import com.project.job.data.source.remote.api.request.CreateJobRequest
import com.project.job.data.source.remote.api.request.FCMTokenRequest
import com.project.job.data.source.remote.api.request.ForgotPasswordRequest
import com.project.job.data.source.remote.api.request.LoginRequest
import com.project.job.data.source.remote.api.request.RegisterRequest
import com.project.job.data.source.remote.api.request.ReviewWorkerRequest
import com.project.job.data.source.remote.api.request.SendMailRequest
import com.project.job.data.source.remote.api.request.UpdateUserRequest
import com.project.job.data.source.remote.api.response.ChangePasswordResponse
import com.project.job.data.source.remote.api.response.ChoiceWorkerResponse
import com.project.job.data.source.remote.api.response.CreateJobResponse
import com.project.job.data.source.remote.api.response.FCMTokenResponse
import com.project.job.data.source.remote.api.response.ForgotPasswordResponse
import com.project.job.data.source.remote.api.response.ReviewWorkerResponse
import com.project.job.data.source.remote.api.response.SendMailResponse
import com.project.job.data.source.remote.api.response.ServiceCleaningResponse
import com.project.job.data.source.remote.api.response.ServiceHealthcareResponse
import com.project.job.data.source.remote.api.response.ServiceMaintenanceResponse
import com.project.job.data.source.remote.api.response.UpdateAvatarResponse
import com.project.job.data.source.remote.api.response.UpdateUserResponse
import com.project.job.data.source.remote.api.response.UserPostJobsResponse
import com.project.job.data.source.remote.api.response.UserResponse
import com.project.job.data.source.remote.api.response.WorkerOrderJobResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    // fcm token
    @POST("api/devices/{clientID")
    suspend fun postFcmToken(
        @Path("clientID") clientID: String,
        @Body request: FCMTokenRequest
    ): Response<FCMTokenResponse>

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
    ): Response<ForgotPasswordResponse>

    @PUT("api/users/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    @PUT("api/users/update")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateUserRequest
    ): Response<UpdateUserResponse>

    @Multipart
    @POST("api/images/upload")
    suspend fun updateAvatar(
        @Header("Authorization") token: String,
        @Part image: MultipartBody.Part
    ): Response<UpdateAvatarResponse>


    @GET("api/services/cleaning")
    suspend fun getCleaningServices(): Response<ServiceCleaningResponse>

    @GET("api/services/healthcare")
    suspend fun getHealthcareServices(): Response<ServiceHealthcareResponse>

    @GET("api/services/maintenance")
    suspend fun getMaintenanceServices(): Response<ServiceMaintenanceResponse>

    @POST("api/jobs/cleaning")
    suspend fun postJobCleaning(
        @Header("Authorization") token: String,
        @Body request: CreateJobRequest
    ): Response<CreateJobResponse>

    @POST("api/jobs/healthcare")
    suspend fun postJobHealthcare(
        @Header("Authorization") token: String,
        @Body request: CreateJobHealthcareRequest
    ): Response<CreateJobResponse>

    // get list job posted by user
    @GET("api/jobs/user/{uid}/job")
    suspend fun getUserPostJobs(
        @Header("Authorization") token: String,
        @Path("uid") uid: String
    ): Response<UserPostJobsResponse>

    // get list worker in 1 job
    @GET("api/orders/{jobID}")
    suspend fun getWorkerInJob(
        @Header("Authorization") token: String,
        @Path("jobID") jobID: String
    ): Response<WorkerOrderJobResponse>

    // choice worker (accept or reject)
    @PUT("api/orders/update")
    suspend fun choiceWorker(
        @Header("Authorization") token: String,
        @Body request: ChoiceWorkerRequest
    ) : Response<ChoiceWorkerResponse>

    // review worker
    @POST("api/reviews/create")
    suspend fun reviewWorker(
        @Header("Authorization") token: String,
        @Body request: ReviewWorkerRequest
    ) : Response<ReviewWorkerResponse>


}
