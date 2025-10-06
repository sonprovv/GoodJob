package com.project.job.data.source.remote.api.request

data class CreateJobHealthcareRequest(
    val userID: String,
    val serviceType: String,
    val startTime: String,
    val price: Int,
    val workerQuantity: Int,
    val listDays: List<String>,
    val shift: ShiftInfo,
    val location: String,
    val services: List<ServiceInfoHealthcare>,
)

data class ShiftInfo(
    val uid: String,
    val workingHour: Int,
    val fee: Int
)

data class ServiceInfoHealthcare(
    val uid: String,
    val quantity: Int
)
