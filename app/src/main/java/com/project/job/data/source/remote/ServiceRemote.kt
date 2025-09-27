package com.project.job.data.source.remote

import com.project.job.data.network.ApiService
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.ServiceDataSource
import com.project.job.data.source.remote.api.request.ChoiceWorkerRequest
import com.project.job.data.source.remote.api.request.CreateJobHealthcareRequest
import com.project.job.data.source.remote.api.request.CreateJobRequest
import com.project.job.data.source.remote.api.request.ReviewWorkerRequest
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

class ServiceRemote(private val apiServie: ApiService) : ServiceDataSource {
//    override suspend fun getServiceCleaning(): NetworkResult<CleaningData?> {
//        try{
//            api.getcleaning()
//            return NetworkResult.Success
//        }catch (e:Exception){
//            return NetworkResult.Error(e.message ?: "Something went wrong")
//        }
//
//    }

    override suspend fun getServiceCleaning(): NetworkResult<CleaningData?> {
        try{
            val result = apiServie.getCleaningServices()
            if(result.isSuccessful){
                return NetworkResult.Success(result.body()?.data)
            }
            else{
                return NetworkResult.Error("Something went wrong")
            }
        }
        catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun getServiceMaintenance(): NetworkResult<List<MaintenanceData>?> {
        try{
            val result = apiServie.getMaintenanceServices()
            if(result.isSuccessful){
                return NetworkResult.Success(result.body()?.data)
            }
            else{
                return NetworkResult.Error("Something went wrong")
            }
        }
        catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun getServiceHealthcare(): NetworkResult<HealthcareData?> {
        try{
            val result = apiServie.getHealthcareServices()
            if(result.isSuccessful){
                return NetworkResult.Success(result.body()?.data)
            }
            else{
                return NetworkResult.Error("Something went wrong")
            }
        }
        catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Something went wrong")
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
        try{
            val result = apiServie.postJobCleaning(
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
            if(result.isSuccessful){
                return NetworkResult.Success(result.body())
            }
            else{
                return NetworkResult.Error("Something went wrong")
            }
        }
        catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Something went wrong")
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
    ): NetworkResult<CreateJobResponse> {
        try{
            val result = apiServie.postJobHealthcare(
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
            if(result.isSuccessful){
                return NetworkResult.Success(result.body()!!)
            }
            else{
                return NetworkResult.Error("Something went wrong")
            }
        }
        catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun getUserPostJobs(uid: String): NetworkResult<UserPostJobsResponse> {
        try{
            val result = apiServie.getUserPostJobs(uid)
            if(result.isSuccessful){
                return NetworkResult.Success(result.body()!!)
            }
            else{
                return NetworkResult.Error("Something went wrong")
            }
        }
        catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun getWorkerOrderJob(jobID: String): NetworkResult<WorkerOrderJobResponse> {
        try{
            val result = apiServie.getWorkerInJob(jobID)
            if(result.isSuccessful){
                return NetworkResult.Success(result.body()!!)
            }
            else{
                return NetworkResult.Error("Something went wrong")
            }
        }
        catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun updateChoiceWorker(
        uid: String,
        status: String
    ): NetworkResult<ChoiceWorkerResponse> {
        try{
            val result = apiServie.choiceWorker(ChoiceWorkerRequest(
                uid = uid,
                status = status
            ))
            if(result.isSuccessful){
                return NetworkResult.Success(result.body()!!)
            }
            else{
                return NetworkResult.Error("Something went wrong")
            }
        }
        catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    override suspend fun getWorkerReviews(workerID: String): NetworkResult<GetReviewWorkerResponse> {
        try{
            val result = apiServie.getWorkerReviews(workerID)
            if(result.isSuccessful){
                return NetworkResult.Success(result.body()!!)
            }
            else{
                return NetworkResult.Error("Something went wrong")
            }
        }
        catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Something went wrong")
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
        try{
            val result = apiServie.reviewWorker(
                ReviewWorkerRequest(
                userID = userID,
                workerID = workerID,
                orderID = orderID,
                rating = rating,
                comment = comment,
                serviceType = serviceType
                )
            )
            if(result.isSuccessful){
                return NetworkResult.Success(result.body()!!)
            }
            else{
                return NetworkResult.Error("Something went wrong")
            }
        }
        catch (e: Exception){
            return NetworkResult.Error(e.message ?: "Something went wrong")
        }
    }

    companion object{
        private var instance: ServiceRemote? = null
        fun getInstance(): ServiceRemote {
            if (instance == null) {
                instance = ServiceRemote(RetrofitClient.apiService)
            }
            return instance!!
        }
    }
}