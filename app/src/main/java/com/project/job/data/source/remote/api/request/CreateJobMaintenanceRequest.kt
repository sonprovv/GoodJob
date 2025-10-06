package com.project.job.data.source.remote.api.request


data class CreateJobMaintenanceRequest(
    val userID: String,
    val serviceType: String,
    val startTime: String,
    val price: Int,
    val listDays: List<String>,
    val location: String,
    val services: List<ServicePowerInfo>
)

data class ServicePowerInfo (
    val uid: String,
    val powers: List<PowersInfoQuantity>,
)

data class PowersInfoQuantity(
    val uid: String,
    val quantity: Int,
    val quantityAction: Int
)