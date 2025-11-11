package com.project.job.data.repository

import com.project.job.data.repository.implement.ServiceRepositoryImpl
import com.project.job.data.source.ServiceDataSource
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.ServiceInfoHealthcare
import com.project.job.data.source.remote.api.request.ShiftInfo
import com.project.job.data.source.remote.api.response.CancelJobResponse
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

class ServiceRepository(
//    private val local: ServiceDataSource.Local,
    private val remote: ServiceDataSource.Remote
) : ServiceRepositoryImpl {

    override suspend fun getServiceCleaning(): Result<CleaningData?> {

        return try {
            val response = remote.getServiceCleaning()
            when (response) {
                is NetworkResult.Success -> {
                    val serviceResponse = response.data
                    // luu vao local o day neu can
//                    local.saveCleaning(serviceResponse.data)

                    if (serviceResponse.success == true) {
                        Result.success(serviceResponse.data)
                    } else {
                        Result.failure(
                            Exception(
                                serviceResponse.message
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getServiceMaintenance(): Result<List<MaintenanceData>?> {
        return try {
            val response = remote.getServiceMaintenance()
            when (response) {
                is NetworkResult.Success -> {
                    val serviceResponse = response.data
                    // luu vao local o day neu can
//                    local.saveMaintenance(serviceResponse.data)

                    if (serviceResponse.success == true) {
                        Result.success(serviceResponse.data)
                    } else {
                        Result.failure(
                            Exception(
                                serviceResponse.message
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getServiceHealthcare(): Result<HealthcareData?> {
        return try {
            val response = remote.getServiceHealthcare()
            when (response) {
                is NetworkResult.Success -> {
                    val serviceResponse = response.data
                    // luu vao local o day neu can
//                    local.saveHealthcare(serviceResponse.data)

                    if (serviceResponse.success == true) {
                        Result.success(serviceResponse.data)
                    } else {
                        Result.failure(
                            Exception(
                                serviceResponse.message
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    Result.failure(Exception(response.message))
                }
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
        location: String,
        lat: Double,
        lon: Double
    ): Result<CreateJobResponse> {
        return try {
            val response = remote.postJobCleaning(
                userID = userID,
                serviceType = serviceType,
                startTime = startTime,
                price = price,
                listDays = listDays,
                duration = duration,
                isCooking = isCooking,
                isIroning = isIroning,
                location = location,
                lat = lat,
                lon = lon
            )
            when (response) {
                is NetworkResult.Success -> {
                    val jobResponse = response.data
                    if (jobResponse?.success == true) {
                        Result.success(jobResponse)
                    } else {
                        Result.failure(
                            Exception(
                                jobResponse?.message ?: "Failed to post cleaning job"
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    Result.failure(Exception(response.message))
                }
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
        services: List<ServiceInfoHealthcare>,
        lat: Double,
        lon: Double
    ): Result<CreateJobHealthcareResponse> {
        return try {
            val response = remote.postJobHealthcare(
                userID = userID,
                serviceType = serviceType,
                startTime = startTime,
                price = price,
                workerQuantity = workerQuantity,
                listDays = listDays,
                location = location,
                shift = shift,
                services = services,
                lat = lat,
                lon = lon
            )
            when (response) {
                is NetworkResult.Success -> {
                    val jobResponse = response.data
                    if (jobResponse?.success == true) {
                        Result.success(jobResponse)
                    } else {
                        Result.failure(
                            Exception(
                                jobResponse?.message ?: "Failed to post healthcare job"
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserPostJobs(uid: String): Result<UserPostJobsResponse> {
        return try {
            val response = remote.getUserPostJobs(uid)
            when (response) {
                is NetworkResult.Success -> {
                    val jobsResponse = response.data
                    if (jobsResponse.success == true) {
                        Result.success(jobsResponse)
                    } else {
                        Result.failure(
                            Exception(
                                jobsResponse.message
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWorkerOrderJob(jobID: String): Result<WorkerOrderJobResponse> {
        return try {
            val response = remote.getWorkerOrderJob(jobID)
            when (response) {
                is NetworkResult.Success -> {
                    val workerResponse = response.data
                    if (workerResponse.success == true) {
                        Result.success(workerResponse)
                    } else {
                        Result.failure(
                            Exception(
                                workerResponse.message
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateChoiceWorker(
        uid: String,
        status: String
    ): Result<ChoiceWorkerResponse> {
        return try {
            val response = remote.updateChoiceWorker(
                uid = uid,
                status = status
            )
            when (response) {
                is NetworkResult.Success -> {
                    val choiceResponse = response.data
                    if (choiceResponse.success == true) {
                        Result.success(choiceResponse)
                    } else {
                        Result.failure(
                            Exception(
                                choiceResponse.message
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWorkerReviews(workerID: String): Result<GetReviewWorkerResponse> {
        return try {
            val response = remote.getWorkerReviews(workerID)
            when (response) {
                is NetworkResult.Success -> {
                    val reviewResponse = response.data
                    if (reviewResponse.success == true) {
                        Result.success(reviewResponse)
                    } else {
                        Result.failure(
                            Exception(
                                reviewResponse.message
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    Result.failure(Exception(response.message))
                }
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
    ): Result<ReviewWorkerResponse> {
        return try {
            val response = remote.postReviewWorker(
                userID = userID,
                workerID = workerID,
                orderID = orderID,
                rating = rating,
                comment = comment,
                serviceType = serviceType
            )
            when (response) {
                is NetworkResult.Success -> {
                    val reviewResponse = response.data
                    if (reviewResponse.success == true) {
                        Result.success(reviewResponse)
                    } else {
                        Result.failure(
                            Exception(
                                reviewResponse.message
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelJob(serviceType: String, jobID: String): Result<CancelJobResponse> {
        return try {
            val response = remote.cancelJob(serviceType, jobID)
            when (response) {
                is NetworkResult.Success -> {
                    val cancelResponse = response.data
                    if (cancelResponse.success == "true") {
                        Result.success(cancelResponse)
                    } else {
                        Result.failure(
                            Exception(
                                cancelResponse.message
                            )
                        )
                    }
                }

                is NetworkResult.Error -> {
                    Result.failure(Exception(response.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
