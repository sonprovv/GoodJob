package com.project.job.data.repository

import retrofit2.Response
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.remote.api.request.ChoiceWorkerRequest
import com.project.job.data.source.remote.api.request.CreateJobHealthcareRequest
import com.project.job.data.source.remote.api.request.CreateJobRequest
import com.project.job.data.source.remote.api.request.ServiceInfoHealthcare
import com.project.job.data.source.remote.api.request.ShiftInfo
import com.project.job.data.source.remote.api.response.ChoiceWorkerResponse
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CleaningService
import com.project.job.data.source.remote.api.response.CreateJobResponse
import com.project.job.data.source.remote.api.response.ServiceCleaningResponse
import com.project.job.data.source.remote.api.response.ServiceHealthcareResponse
import com.project.job.data.source.remote.api.response.ServiceMaintenanceResponse
import com.project.job.data.source.remote.api.response.UserPostJobsResponse
import com.project.job.data.source.remote.api.response.WorkerOrderJobResponse

class ServiceRepository {
    // Implement user-related data operations here
    private val apiService = RetrofitClient.apiService

    suspend fun getServiceCleaning(): Response<ServiceCleaningResponse> {
        return apiService.getCleaningServices()
    }

    suspend fun getServiceHealthcare(): Response<ServiceHealthcareResponse> {
        return apiService.getHealthcareServices()
    }

    suspend fun getServiceMaintenance(): Response<ServiceMaintenanceResponse> {
        return apiService.getMaintenanceServices()
    }

    suspend fun postJobCleaning(
        token: String,
        userID: String,
        serviceType: String,
        startTime: String,
        price: Int,
        listDays: List<String>,
        duration: CleaningDuration,
        isCooking: Boolean,
        isIroning: Boolean,
        location: String
    ): Response<CreateJobResponse> {
        return apiService.postJobCleaning(
            "Bearer $token", CreateJobRequest(
                userID = userID,
                serviceType = serviceType,
                startTime = startTime,
//                workerQuantity = workerQuantity,
                price = price,
                listDays = listDays,
                duration = duration,
                isCooking = isCooking,
                isIroning = isIroning,
                location = location
//                services = services
            )
        )
    }

    suspend fun postJobHealthcare(
        token: String,
        userID: String,
        serviceType: String,
        startTime: String,
        price: Int,
        workerQuantity: Int,
        listDays: List<String>,
        location: String,
        shift: ShiftInfo,
        services: List<ServiceInfoHealthcare>
    ): Response<CreateJobResponse> {
        return apiService.postJobHealthcare(
            "Bearer $token", CreateJobHealthcareRequest(
                userID = userID,
                serviceType = serviceType,
                startTime = startTime,
                price = price,
                workerQuantity = workerQuantity,
                listDays = listDays,
                shift = shift,
                location = location,
                services = services
            )
        )
    }

    suspend fun getUserPostJobs(token: String, uid : String): Response<UserPostJobsResponse> {
        return apiService.getUserPostJobs("Bearer $token", uid)
    }

    suspend fun getWorkerOrderJob(token: String, jobID : String): Response<WorkerOrderJobResponse> {
        return apiService.getWorkerInJob("Bearer $token", jobID)
    }

    suspend fun updateChoiceWorker(token: String, uid : String, status : String) : Response<ChoiceWorkerResponse> {
        return apiService.choiceWorker("Bearer $token", ChoiceWorkerRequest(uid, status))
    }

    suspend fun postReviewWorker(
        token: String,
        userID: String,
        workerID: String,
        orderID: String,
        rating: Int,
        comment: String,
        serviceType: String
    ) : Response<com.project.job.data.source.remote.api.response.ReviewWorkerResponse> {
        return apiService.reviewWorker("Bearer $token", com.project.job.data.source.remote.api.request.ReviewWorkerRequest(
            userID = userID,
            workerID = workerID,
            orderID = orderID,
            rating = rating,
            comment = comment,
            serviceType = serviceType
        ))
    }
}