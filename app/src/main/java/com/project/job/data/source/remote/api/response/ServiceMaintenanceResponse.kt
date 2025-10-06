package com.project.job.data.source.remote.api.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ServiceMaintenanceResponse(
    val success: Boolean,
    val message: String,
    val data: List<MaintenanceData>
)
@Parcelize
data class MaintenanceData(
    val uid: String,
    val image: String,
    val serviceType: String,
    val serviceName: String,
    val powers: List<PowersInfo>,
    val maintenance: String
) : Parcelable