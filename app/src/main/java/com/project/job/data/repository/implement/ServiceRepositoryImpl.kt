package com.project.job.data.repository.implement

import com.project.job.data.source.remote.api.request.ServiceInfoHealthcare
import com.project.job.data.source.remote.api.request.ShiftInfo
import com.project.job.data.source.remote.api.response.ChoiceWorkerResponse
import com.project.job.data.source.remote.api.response.CleaningData
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CreateJobHealthcareResponse
import com.project.job.data.source.remote.api.response.CreateJobResponse
import com.project.job.data.source.remote.api.response.GetReviewWorkerResponse
import com.project.job.data.source.remote.api.response.HealthcareData
import com.project.job.data.source.remote.api.response.MaintenanceData
import com.project.job.data.source.remote.api.response.ReviewWorkerResponse
import com.project.job.data.source.remote.api.response.UserPostJobsResponse
import com.project.job.data.source.remote.api.response.WorkerOrderJobResponse
import retrofit2.Response

interface ServiceRepositoryImpl {
    suspend fun getServiceCleaning(): Result<CleaningData?>
    suspend fun getServiceMaintenance(): Result<List<MaintenanceData>?>
    suspend fun getServiceHealthcare(): Result<HealthcareData?>

    suspend fun postJobCleaning(
        userID: String,
        serviceType: String,
        startTime: String,
        price: Int,
        listDays: List<String>,
        duration: CleaningDuration,
        isCooking: Boolean,
        isIroning: Boolean,
        location: String
    ): Result<CreateJobResponse>

    suspend fun postJobHealthcare(
        userID: String,
        serviceType: String,
        startTime: String,
        price: Int,
        workerQuantity: Int,
        listDays: List<String>,
        location: String,
        shift: ShiftInfo,
        services: List<ServiceInfoHealthcare>
    ): Result<CreateJobHealthcareResponse>

    suspend fun getUserPostJobs( uid : String): Result<UserPostJobsResponse>

    suspend fun getWorkerOrderJob( jobID : String): Result<WorkerOrderJobResponse>

    suspend fun updateChoiceWorker( uid : String, status : String) : Result<ChoiceWorkerResponse>

    suspend fun getWorkerReviews( workerID: String): Result<GetReviewWorkerResponse>

    suspend fun postReviewWorker(
        userID: String,
        workerID: String,
        orderID: String,
        rating: Int,
        comment: String,
        serviceType: String
    ) : Result<ReviewWorkerResponse>
}