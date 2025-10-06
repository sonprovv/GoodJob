package com.project.job.data.source.remote.api.response

data class CreateJobHealthcareResponse(
    val success: Boolean,
    val message: String,
    val newJob: NewJobHealthcare
)

data class NewJobHealthcare(
    val listDays: List<String>,
    val location: String,
    val price: Int,
    val serviceType: String,
    val services: List<ServiceHealthcare>,
    val shift: HealthcareShift,
    val startTime: String,
    val userID: String,
    val workerQuantity: Int,
    val status: String,
    val uid: String,
    val createdAt: String
)

