package com.project.job.data.source.remote

import com.project.job.data.network.ApiService
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.ServiceDataSource
import com.project.job.data.source.remote.api.request.ChoiceWorkerRequest
import com.project.job.data.source.remote.api.request.CreateJobHealthcareRequest
import com.project.job.data.source.remote.api.request.CreateJobMaintenanceRequest
import com.project.job.data.source.remote.api.request.CreateJobRequest
import com.project.job.data.source.remote.api.request.ReviewWorkerRequest
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

class ServiceRemote(private val apiService: ApiService) : ServiceDataSource {
//    override suspend fun getServiceCleaning(): NetworkResult<CleaningData?> {
//        try{
//            api.getcleaning()
//            return NetworkResult.Success
//        }catch (e:Exception){
//            return NetworkResult.Error(e.message ?: "Something went wrong")
//        }
//
//    }

    override suspend fun getServiceCleaning(): NetworkResult<ServiceCleaningResponse> {
        return safeApiCall {
            apiService.getCleaningServices()
        }
    }

    override suspend fun getServiceMaintenance(): NetworkResult<ServiceMaintenanceResponse> {
        return safeApiCall {
            apiService.getMaintenanceServices()
        }
    }

    override suspend fun getServiceHealthcare(): NetworkResult<ServiceHealthcareResponse> {
        return safeApiCall {
            apiService.getHealthcareServices()
        }
    }

    override suspend fun postJobCleaning(
        userID: String,
        serviceType: String,
        startTime: String,
        price: Int,
        listDays: List<String>,
        duration: CleaningDuration,
        isCooking: Boolean,
        isIroning: Boolean,
        location: String
    ): NetworkResult<CreateJobResponse?> {
        return safeApiCall {
            apiService.postJobCleaning(
                CreateJobRequest(
                    userID = userID,
                    serviceType = serviceType,
                    startTime = startTime,
                    price = price,
                    listDays = listDays,
                    duration = duration,
                    isCooking = isCooking,
                    isIroning = isIroning,
                    location = location
                )
            )
        }
    }

    override suspend fun postJobHealthcare(
        userID: String,
        serviceType: String,
        startTime: String,
        price: Int,
        workerQuantity: Int,
        listDays: List<String>,
        location: String,
        shift: ShiftInfo,
        services: List<ServiceInfoHealthcare>
    ): NetworkResult<CreateJobHealthcareResponse> {
        return safeApiCall {
            apiService.postJobHealthcare(
                CreateJobHealthcareRequest(
                    userID = userID,
                    serviceType = serviceType,
                    startTime = startTime,
                    price = price,
                    workerQuantity = workerQuantity,
                    listDays = listDays,
                    location = location,
                    shift = shift,
                    services = services
                )
            )
        }

    }

    override suspend fun postJobMaintenance(
        userID: String,
        serviceType: String,
        startTime: String,
        price: Int,
        listDays: List<String>,
        services: List<ServicePowerInfo>,
        location: String
    ): NetworkResult<CreateJobMaintenanceResponse> {
        return safeApiCall {
            apiService.postJobMaintenance(
                CreateJobMaintenanceRequest(
                    userID = userID,
                    serviceType = serviceType,
                    startTime = startTime,
                    price = price,
                    listDays = listDays,
                    services = services,
                    location = location
                )
            )
        }
    }

    override suspend fun cancelJob(
        serviceType: String,
        jobID: String
    ): NetworkResult<CancelJobResponse> {
        return safeApiCall {
            apiService.cancelJob(serviceType = serviceType, jobID = jobID)
        }
    }


    override suspend fun getUserPostJobs(uid: String): NetworkResult<UserPostJobsResponse> {
        return safeApiCall {
            apiService.getUserPostJobs(uid)
        }
    }

    override suspend fun getWorkerOrderJob(jobID: String): NetworkResult<WorkerOrderJobResponse> {
        return safeApiCall {
            apiService.getWorkerInJob(jobID)
        }
    }

    override suspend fun updateChoiceWorker(
        uid: String,
        status: String
    ): NetworkResult<ChoiceWorkerResponse> {
        return safeApiCall {
            apiService.choiceWorker(
                ChoiceWorkerRequest(
                    uid = uid,
                    status = status
                )
            )
        }
    }

    override suspend fun getWorkerReviews(workerID: String): NetworkResult<GetReviewWorkerResponse> {
        return safeApiCall {
            apiService.getWorkerReviews(workerID)
        }
    }

    override suspend fun postReviewWorker(
        userID: String,
        workerID: String,
        orderID: String,
        rating: Int,
        comment: String,
        serviceType: String
    ): NetworkResult<ReviewWorkerResponse> {
        return safeApiCall {
            apiService.reviewWorker(
                ReviewWorkerRequest(
                    userID = userID,
                    workerID = workerID,
                    orderID = orderID,
                    rating = rating,
                    comment = comment,
                    serviceType = serviceType
                )
            )
        }
    }

    companion object {
        private var instance: ServiceRemote? = null
        fun getInstance(): ServiceRemote {
            if (instance == null) {
                instance = ServiceRemote(RetrofitClient.apiService)
            }
            return instance!!
        }
    }
}