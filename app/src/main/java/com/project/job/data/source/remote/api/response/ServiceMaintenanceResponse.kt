package com.project.job.data.source.remote.api.response

data class ServiceMaintenanceResponse(
    val success: Boolean,
    val message: String,
    val data: List<MaintenanceData>
)

data class MaintenanceData(
    val uid: String,
    val image: String,
    val serviceType: String,
    val serviceName: String,
    val powers: List<String>,
    val maintenance: String
)