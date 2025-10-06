package com.project.job.data.repository

import com.project.job.data.network.RetrofitClient
import com.project.job.data.repository.implement.ServiceRepositoryImpl
import com.project.job.data.source.remote.api.request.ChoiceWorkerRequest
import com.project.job.data.source.remote.api.request.CreateJobHealthcareRequest
import com.project.job.data.source.remote.api.request.CreateJobRequest
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

class ServiceRepository (
//    private val local: ServiceLocal,
//    private val remote : ServiceRemote
) : ServiceRepositoryImpl {
    // Implement user-related data operations here
    private val apiService = RetrofitClient.apiService


    override suspend fun getServiceCleaning(): Result<CleaningData?> {

        /*
        * // check xem local co khong
        * If (local.getCleaning == true){ //ko co mang , neu co mang thi cap nhat
        *   return Result.success(local.getCleaning)
        * } else {
        *remote. call api
        *   try{
        * remote.getCleaning()
        * return Result.succs()}
        * catch(){return Result.error()}
        * }
        *
        *
        * */
        return try {
            val response = apiService.getCleaningServices()
            if (response.isSuccessful) {
                val serviceResponse = response.body()
                if (serviceResponse?.success == true) {
                    Result.success(serviceResponse.data)
                } else {
                    Result.failure(Exception(serviceResponse?.message ?: "Failed to get cleaning service data"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getServiceMaintenance(): Result<List<MaintenanceData>?> {
        return try {
            val response = apiService.getMaintenanceServices()
            if (response.isSuccessful) {
                val serviceResponse = response.body()
                if (serviceResponse?.success == true) {
                    Result.success(serviceResponse.data)
                } else {
                    Result.failure(Exception(serviceResponse?.message ?: "Failed to get maintenance service data"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getServiceHealthcare(): Result<HealthcareData?> {
        return try {
            val response = apiService.getHealthcareServices()
            if (response.isSuccessful) {
                val serviceResponse = response.body()
                if (serviceResponse?.success == true) {
                    Result.success(serviceResponse.data)
                } else {
                    Result.failure(Exception(serviceResponse?.message ?: "Failed to get healthcare service data"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
    ): Result<CreateJobResponse> {
        return try {
            val response = apiService.postJobCleaning(
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
            if (response.isSuccessful) {
                val jobResponse = response.body()
                if (jobResponse?.success == true) {
                    Result.success(jobResponse)
                } else {
                    Result.failure(Exception(jobResponse?.message ?: "Failed to post cleaning job"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
    ): Result<CreateJobHealthcareResponse> {
        return try {
            val response = apiService.postJobHealthcare(
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
            if (response.isSuccessful) {
                val jobResponse = response.body()
                if (jobResponse?.success == true) {
                    Result.success(jobResponse)
                } else {
                    Result.failure(Exception(jobResponse?.message ?: "Failed to post healthcare job"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserPostJobs(uid : String): Result<UserPostJobsResponse> {
        return try {
            val response = apiService.getUserPostJobs(uid)
            if (response.isSuccessful) {
                val jobsResponse = response.body()
                if (jobsResponse?.success == true) {
                    Result.success(jobsResponse)
                } else {
                    Result.failure(Exception(jobsResponse?.message ?: "Failed to get user post jobs"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWorkerOrderJob(jobID : String): Result<WorkerOrderJobResponse> {
        return try {
            val response = apiService.getWorkerInJob(jobID)
            if (response.isSuccessful) {
                val workerResponse = response.body()
                if (workerResponse?.success == true) {
                    Result.success(workerResponse)
                } else {
                    Result.failure(Exception(workerResponse?.message ?: "Failed to get workers in job"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateChoiceWorker(uid : String, status : String) : Result<ChoiceWorkerResponse> {
        return try {
            val response = apiService.choiceWorker(
                ChoiceWorkerRequest(
                    uid = uid,
                    status = status
                )
            )
            if (response.isSuccessful) {
                val choiceResponse = response.body()
                if (choiceResponse?.success == true) {
                    Result.success(choiceResponse)
                } else {
                    Result.failure(Exception(choiceResponse?.message ?: "Failed to update choice worker"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWorkerReviews(workerID: String): Result<GetReviewWorkerResponse> {
        return try {
            val response = apiService.getWorkerReviews(workerID)
            if (response.isSuccessful) {
                val reviewResponse = response.body()
                if (reviewResponse?.success == true) {
                    Result.success(reviewResponse)
                } else {
                    Result.failure(Exception(reviewResponse?.message ?: "Failed to get worker reviews"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun postReviewWorker(
        userID: String,
        workerID: String,
        orderID: String,
        rating: Int,
        comment: String,
        serviceType: String
    ) : Result<ReviewWorkerResponse> {
        return try {
            val response = apiService.reviewWorker(
                com.project.job.data.source.remote.api.request.ReviewWorkerRequest(
                    userID = userID,
                    workerID = workerID,
                    orderID = orderID,
                    rating = rating,
                    comment = comment,
                    serviceType = serviceType
                )
            )
            if (response.isSuccessful) {
                val reviewResponse = response.body()
                if (reviewResponse?.success == true) {
                    Result.success(reviewResponse)
                } else {
                    Result.failure(Exception(reviewResponse?.message ?: "Failed to post worker review"))
                }
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}