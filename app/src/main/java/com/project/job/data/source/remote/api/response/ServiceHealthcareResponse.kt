package com.project.job.data.source.remote.api.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ServiceHealthcareResponse(
    val success: Boolean,
    val message: String,
    val data: HealthcareData
)

data class HealthcareData(
    val services: List<HealthcareService>,
    val shifts: List<HealthcareShift>
)
@Parcelize
data class HealthcareService(
    val uid: String,
    val serviceType: String,
    val serviceName: String,
    val image: String,
    val duties: List<String>,
    val excludedTasks: List<String>
) : Parcelable
@Parcelize
data class HealthcareShift(
    val uid: String,
    val workingHour: Int,
    val fee: Int
) : Parcelable
