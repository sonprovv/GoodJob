package com.project.job.data.source.remote.api.request

import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CleaningService
import com.project.job.data.source.remote.api.response.User

data class CreateJobRequest(
    val userID: String,
    val serviceType: String,
    val startTime: String,
//    val workerQuantity: Int,
    val price : Int,
    val listDays : List<String>,
    val duration : CleaningDuration,
    val isCooking: Boolean,
    val isIroning: Boolean,
    val location: String
//    val services : List<CleaningService>
)
