package com.project.job.data.network

import com.project.job.data.source.remote.BaseResponse
import com.project.job.data.source.remote.api.request.ChangePasswordRequest
import com.project.job.data.source.remote.api.request.ChatBotRequest
import com.project.job.data.source.remote.api.request.ChoiceWorkerRequest
import com.project.job.data.source.remote.api.request.CreateJobHealthcareRequest
import com.project.job.data.source.remote.api.request.CreateJobMaintenanceRequest
import com.project.job.data.source.remote.api.request.CreateJobRequest
import com.project.job.data.source.remote.api.request.FCMTokenRequest
import com.project.job.data.source.remote.api.request.ForgotPasswordRequest
import com.project.job.data.source.remote.api.request.GoogleSignInRequest
import com.project.job.data.source.remote.api.request.LoginRequest
import com.project.job.data.source.remote.api.request.RefreshTokenRequest
import com.project.job.data.source.remote.api.request.RegisterRequest
import com.project.job.data.source.remote.api.request.ReviewWorkerRequest
import com.project.job.data.source.remote.api.request.SendMailRequest
import com.project.job.data.source.remote.api.request.UpdateUserRequest
import com.project.job.data.source.remote.api.request.chat.SendMessageRequest
import com.project.job.data.source.remote.api.request.chat.UpdateStatusRequest
import com.project.job.data.source.remote.api.response.CancelJobResponse
import com.project.job.data.source.remote.api.response.ChangePasswordResponse
import com.project.job.data.source.remote.api.response.ChatBotResponse
import com.project.job.data.source.remote.api.response.ChoiceWorkerResponse
import com.project.job.data.source.remote.api.response.CreateJobHealthcareResponse
import com.project.job.data.source.remote.api.response.CreateJobMaintenanceResponse
import com.project.job.data.source.remote.api.response.CreateJobResponse
import com.project.job.data.source.remote.api.response.FCMTokenResponse
import com.project.job.data.source.remote.api.response.ForgotPasswordResponse
import com.project.job.data.source.remote.api.response.GetNotificationResponse
import com.project.job.data.source.remote.api.response.GetNotificationsResponse
import com.project.job.data.source.remote.api.response.GetReviewWorkerResponse
import com.project.job.data.source.remote.api.response.PaymentResponse
import com.project.job.data.source.remote.api.response.RefreshTokenResponse
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
import com.project.job.data.source.remote.api.response.chat.ChatUserResponse
import com.project.job.data.source.remote.api.response.chat.ConversationResponse
import com.project.job.data.source.remote.api.response.chat.MessageResponse
import com.project.job.data.source.remote.api.response.chat.UserStatusResponse
import com.project.job.utils.AuthRequired
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE

interface ApiService {
    // ----------------- fcm token ---------------------
    @AuthRequired
    @POST("api/devices")
    suspend fun postFcmToken(
        @Body request: FCMTokenRequest
    ): Response<FCMTokenResponse>

    @AuthRequired
    @PUT("api/devices/logout")
    suspend fun deleteFcmToken(
        @Body request: FCMTokenRequest
    ): Response<FCMTokenResponse>
    // ------------------ end fcm token -----------------------------
    // ------------------ notification ------------------------------
    @AuthRequired
    @GET("api/notifications")
    suspend fun getNotifications(
    ): Response<GetNotificationsResponse>
    @AuthRequired
    @PUT("api/notifications/{notificationID}")
    suspend fun markNotificationAsRead(
        @Path("notificationID") notificationID: String
    ): Response<GetNotificationResponse>
    // ------------------- end notification ---------------------------

    // ------------------- auth -----------------------------------
    @POST("api/auth/client/refreshToken")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<RefreshTokenResponse>

    @POST("api/auth/me")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<UserResponse>

    @POST("api/auth/create")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<UserResponse>

    @POST("api/auth/loginGG")
    suspend fun googleSignIn(
        @Body request: GoogleSignInRequest
    ): Response<UserResponse>

    @POST("api/emails/send")
    suspend fun sendMail(
        @Body request: SendMailRequest
    ): Response<SendMailResponse>

    @PUT("api/users/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ForgotPasswordResponse>

    @AuthRequired
    @PUT("api/users/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    @AuthRequired
    @PUT("api/users/update")
    suspend fun updateProfile(
        @Body request: UpdateUserRequest
    ): Response<UpdateUserResponse>

    @Multipart
    @POST("api/images/upload")
    suspend fun updateAvatar(
        @Part image: MultipartBody.Part
    ): Response<UpdateAvatarResponse>
    // --------------------- end auth ----------------------
    // --------------------- services ----------------------
    @GET("api/services/cleaning")
    suspend fun getCleaningServices(): Response<ServiceCleaningResponse>

    @GET("api/services/healthcare")
    suspend fun getHealthcareServices(): Response<ServiceHealthcareResponse>

    @GET("api/services/maintenance")
    suspend fun getMaintenanceServices(): Response<ServiceMaintenanceResponse>

    @AuthRequired
    @POST("api/jobs/cleaning")
    suspend fun postJobCleaning(
        @Body request: CreateJobRequest
    ): Response<CreateJobResponse>

    @AuthRequired
    @POST("api/jobs/healthcare")
    suspend fun postJobHealthcare(
        @Body request: CreateJobHealthcareRequest
    ): Response<CreateJobHealthcareResponse>

    @AuthRequired
    @POST("api/jobs/maintenance")
    suspend fun postJobMaintenance(
        @Body request: CreateJobMaintenanceRequest
    ): Response<CreateJobMaintenanceResponse>
    // --------------- end service --------------------
    // ---------------- cancel job ---------------------
    @AuthRequired
    @PUT("api/jobs/{serviceType}/{jobID}/cancel")
    suspend fun cancelJob(
        @Path("serviceType")
        serviceType: String,
        @Path("jobID")
        jobID: String
    ): Response<CancelJobResponse>
    // --------------- end cancel job --------------------
    // --------- get list job posted by user ---------------
    @AuthRequired
    @GET("api/jobs/user/{uid}/job")
    suspend fun getUserPostJobs(
        @Path("uid") uid: String
    ): Response<UserPostJobsResponse>
    // ------- end get list job posted by user --------------
    // -------- history payment ----------------------
    @AuthRequired
    @GET("api/payments")
    suspend fun getHistoryPayment(): Response<PaymentResponse>
    // -------- history payment ----------------------
    // ------- get list worker in 1 job -------------------
    @AuthRequired
    @GET("api/orders/{jobID}")
    suspend fun getWorkerInJob(
        @Path("jobID") jobID: String
    ): Response<WorkerOrderJobResponse>
    // ------- end get list worker in 1 job ----------------
    // --------- choice worker (accept or reject) --------------
    @AuthRequired
    @PUT("api/orders/update")
    suspend fun choiceWorker(
        @Body request: ChoiceWorkerRequest
    ) : Response<ChoiceWorkerResponse>
    // ----------- end choice worker (accept or reject) -----------------
    // ----------- review worker --------------------------------------
    @AuthRequired
    @POST("api/reviews/create")
    suspend fun reviewWorker(
        @Body request: ReviewWorkerRequest
    ) : Response<ReviewWorkerResponse>

    @AuthRequired
    @GET("api/reviews/worker/{workerID}/experience")
    suspend fun getWorkerReviews(
        @Path("workerID") workerID: String
    ): Response<GetReviewWorkerResponse>
    // ----------- end review worker --------------------------------------
    // ----------- AI chat bot --------------------------------------
    @AuthRequired
    @POST("api/chatbox")
    suspend fun chatBot(
        @Body request : ChatBotRequest
    ) : Response<ChatBotResponse>
    // ----------- end AI chat bot --------------------------------------
    // ---------------- Chat ----------------
    @AuthRequired
    @POST("api/chat/send")
    suspend fun chatSendMessage(
        @Body request: SendMessageRequest
    ): Response<BaseResponse<MessageResponse>>

    @AuthRequired
    @GET("api/chat/messages/{userId}")
    suspend fun chatGetMessages(
        @Path("userId") userId: String,
        @Query("limit") limit: Int = 50
    ): Response<BaseResponse<List<MessageResponse>>>

    @AuthRequired
    @GET("api/chat/conversations")
    suspend fun chatGetConversations(): Response<BaseResponse<List<ConversationResponse>>>

    @AuthRequired
    @GET("api/chat/available-users")
    suspend fun chatGetAvailableUsers(): Response<BaseResponse<List<ChatUserResponse>>>

    @AuthRequired
    @PUT("api/chat/read/{userId}")
    suspend fun chatMarkAsRead(
        @Path("userId") userId: String
    ): Response<BaseResponse<Unit>>

    @AuthRequired
    @DELETE("api/chat/message/{conversationId}/{messageId}")
    suspend fun chatDeleteMessage(
        @Path("conversationId") conversationId: String,
        @Path("messageId") messageId: String
    ): Response<BaseResponse<Unit>>

    @AuthRequired
    @DELETE("api/chat/conversation/{userId}")
    suspend fun chatDeleteConversation(
        @Path("userId") userId: String
    ): Response<BaseResponse<Unit>>

    @GET("api/chat/status/{userId}")
    suspend fun chatGetUserStatus(
        @Path("userId") userId: String
    ): Response<BaseResponse<UserStatusResponse>>

    @AuthRequired
    @POST("api/chat/status")
    suspend fun chatUpdateStatus(
        @Body request: UpdateStatusRequest
    ): Response<BaseResponse<UserStatusResponse>>
}
