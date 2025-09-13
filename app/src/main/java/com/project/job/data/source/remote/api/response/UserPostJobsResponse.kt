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
    val serviceType : String,
    val workerQuantity: Int,
    val price: Int,
    val listDays: List<String>,
    val status: String,
    val location: String,
    val isCooking: Boolean,
    val isIroning: Boolean,
    val createdAt: String,
    val user : UserInfo,
    val duration: CleaningDuration,
    val services : List<CleaningService> = emptyList()
//    val serviceHeathCare: ServiceHeathCare? = null,
//    val serviceMaintenance: ServiceMaintenance? = null
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
