package com.project.job.data.source.remote.api.response

data class CreateJobResponse(
    val success: Boolean,
    val message: String,
    val newJob: NewJobCleaning
)

data class NewJobCleaning(
    val duration: CleaningDuration,
    val isCooking: Boolean,
    val isIroning: Boolean,
    val listDays: List<String>,
    val location: String,
    val price: Int,
    val serviceType: String,
    val startTime: String,
    val userID: String,
    val status: String,
    val uid: String,
    val createdAt: String
)
