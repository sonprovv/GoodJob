package com.project.job.data.repository

import retrofit2.Response
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.remote.api.request.CreateJobRequest
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CleaningService
import com.project.job.data.source.remote.api.response.CreateJobResponse
import com.project.job.data.source.remote.api.response.ServiceCleaningResponse
import com.project.job.data.source.remote.api.response.UserPostJobsResponse
import com.project.job.data.source.remote.api.response.WorkerOrderJobResponse

class ServiceRepository {
    // Implement user-related data operations here
    private val apiService = RetrofitClient.apiService

    suspend fun getServiceCleaning(): Response<ServiceCleaningResponse> {
        return apiService.getCleaningServices()
    }

    suspend fun postJobCleaning(
        token: String,
        userID: String,
        serviceType: String,
        startTime: String,
        workerQuantity: Int,
        price: Int,
        listDays: List<String>,
        duration: CleaningDuration,
        isCooking: Boolean,
        isIroning: Boolean,
        location: String,
        services: List<CleaningService>
    ): Response<CreateJobResponse> {
        return apiService.postJobCleaning(
            "Bearer $token", CreateJobRequest(
                userID = userID,
                serviceType = serviceType,
                startTime = startTime,
                workerQuantity = workerQuantity,
                price = price,
                listDays = listDays,
                duration = duration,
                isCooking = isCooking,
                isIroning = isIroning,
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
}