package com.project.job.data.source.remote.api.response

import com.project.job.data.source.remote.api.request.ServicePowerInfo

data class CreateJobMaintenanceResponse(
    val success: Boolean,
    val message: String,
    val newJob: NewJobMaintenance
)

data class NewJobMaintenance (
    val userID: String,
    val uid: String,
    val serviceType: String,
    val startTime: String,
    val price: Int,
    val listDays: List<String>,
    val location: String,
    val services: List<ServicePowerInfo>,
    val status: String,
    val createdAt: String
)
