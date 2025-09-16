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
    val workerQuantity: Int?=null,
    val price: Int,
    val listDays: List<String>,
    val status: String,
    val location: String,
    val isCooking: Boolean ?= null,
    val isIroning: Boolean ?= null,
    val createdAt: String,
    val user : UserInfo,
    val duration: CleaningDuration ?= null,
    val services : List<ServiceHealthcare> ?= null,
    val shift : HealthcareShift ?= null
//    val serviceHeathCare: ServiceHeathCare? = null,
//    val serviceMaintenance: ServiceMaintenance? = null
) : Parcelable
@Parcelize
data class ServiceHealthcare(
    val serviceID : String?=null,
    val quantity : Int?=null,
    val uid : String?=null,
    val powers : List<PowersInfo>?=null,
    val isMaintenance : Boolean ?=null,
    val maintenance : String ?=null,
) : Parcelable

@Parcelize
data class PowersInfo(
    val powerName: String,
    val quantity: Int
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
