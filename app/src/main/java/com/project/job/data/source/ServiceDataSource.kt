package com.project.job.data.source

import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.ServiceInfoHealthcare
import com.project.job.data.source.remote.api.request.ServicePowerInfo
import com.project.job.data.source.remote.api.request.ShiftInfo
import com.project.job.data.source.remote.api.response.CancelJobResponse
import com.project.job.data.source.remote.api.response.ChoiceWorkerResponse
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CreateJobHealthcareResponse
import com.project.job.data.source.remote.api.response.CreateJobMaintenanceResponse
import com.project.job.data.source.remote.api.response.CreateJobResponse
import com.project.job.data.source.remote.api.response.GetReviewWorkerResponse
import com.project.job.data.source.remote.api.response.ReviewWorkerResponse
import com.project.job.data.source.remote.api.response.ServiceCleaningResponse
import com.project.job.data.source.remote.api.response.ServiceHealthcareResponse
import com.project.job.data.source.remote.api.response.ServiceMaintenanceResponse
import com.project.job.data.source.remote.api.response.UserPostJobsResponse
import com.project.job.data.source.remote.api.response.WorkerOrderJobResponse

interface ServiceDataSource {
//    interface Local{
//        suspend fun getServiceCleaning(): Flow<CleaningData?>
//    }
//
//    interface Remote{
//        suspend fun getServiceCleaning(): NetworkResult<CleaningData?>
//    }

    suspend fun getServiceCleaning(): NetworkResult<ServiceCleaningResponse>
    suspend fun getServiceMaintenance(): NetworkResult<ServiceMaintenanceResponse>
    suspend fun getServiceHealthcare(): NetworkResult<ServiceHealthcareResponse>

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
    ): NetworkResult<CreateJobHealthcareResponse?>

    suspend fun postJobMaintenance(
        userID: String,
        serviceType: String,
        startTime: String,
        price: Int,
        listDays: List<String>,
        services: List<ServicePowerInfo>,
        location: String
    ): NetworkResult<CreateJobMaintenanceResponse>

    suspend fun cancelJob( serviceType : String, jobID : String) : NetworkResult<CancelJobResponse>

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