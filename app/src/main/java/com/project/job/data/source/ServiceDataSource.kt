package com.project.job.data.source

import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.ServiceInfoHealthcare
import com.project.job.data.source.remote.api.request.ShiftInfo
import com.project.job.data.source.remote.api.response.ChoiceWorkerResponse
import com.project.job.data.source.remote.api.response.CleaningData
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CreateJobResponse
import com.project.job.data.source.remote.api.response.GetReviewWorkerResponse
import com.project.job.data.source.remote.api.response.HealthcareData
import com.project.job.data.source.remote.api.response.MaintenanceData
import com.project.job.data.source.remote.api.response.ReviewWorkerResponse
import com.project.job.data.source.remote.api.response.UserPostJobsResponse
import com.project.job.data.source.remote.api.response.WorkerOrderJobResponse

interface ServiceDataSource {
//    interface Local{
//        suspend fun getServiceCleaning(): Flow<CleaningData?>
//    }
//
    interface Remote{
        suspend fun getServiceCleaning(): NetworkResult<CleaningData?>
    }

    suspend fun getServiceCleaning(): NetworkResult<CleaningData?>
    suspend fun getServiceMaintenance(): NetworkResult<List<MaintenanceData>?>
    suspend fun getServiceHealthcare(): NetworkResult<HealthcareData?>

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
    ): NetworkResult<CreateJobResponse?>

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
    ): NetworkResult<CreateJobResponse?>

    suspend fun getUserPostJobs( uid : String): NetworkResult<UserPostJobsResponse>

    suspend fun getWorkerOrderJob( jobID : String): NetworkResult<WorkerOrderJobResponse>

    suspend fun updateChoiceWorker( uid : String, status : String) : NetworkResult<ChoiceWorkerResponse>

    suspend fun getWorkerReviews( workerID: String): NetworkResult<GetReviewWorkerResponse>

    suspend fun postReviewWorker(
        userID: String,
        workerID: String,
        orderID: String,
        rating: Int,
        comment: String,
        serviceType: String
    ) : NetworkResult<ReviewWorkerResponse>
}