package com.project.job.data.source.remote.api.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class UserPostJobsResponse(
    val success: Boolean,
    val message: String,
    val jobs: List<DataJobs>
)

@Parcelize
data class DataJobs(
    val uid: String,
    val startTime: String,
    val serviceType: String,
    val workerQuantity: Int? = null,
    val price: Int,
    val listDays: List<String>,
    val status: String,
    val location: String,
    val isCooking: Boolean? = null,
    val isIroning: Boolean? = null,
    val createdAt: String,
    val user: UserInfo,
    val duration: CleaningDuration? = null,
    val services: List<ServiceHealthcare>? = null,
    val shift: HealthcareShift? = null
) : Parcelable

@Parcelize
data class ServiceHealthcare(
    val quantity: Int? = null,
    val uid: String? = null,
    val powers: List<PowersInfo>? = null,
) : Parcelable

@Parcelize
data class PowersInfo(
    val uid: String,
    val name: String?,
    val quantity: Int,
    val price: Int? = null,
    val priceAction: Int? = null,
    val quantityAction: Int? = null
) : Parcelable

@Parcelize
data class UserInfo(
    val uid: String,
    val gender: String,
    val dob: String,
    val location: String,
    val avatar: String,
    val tel: String,
    val username: String,
    val email: String,
    val role: String
) : Parcelable
