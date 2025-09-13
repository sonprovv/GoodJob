package com.project.job.data.source.remote.api.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class ServiceCleaningResponse (
    val success: Boolean,
    val message: String,
    val data: CleaningData? = null
)

data class CleaningData (
    val services: List<CleaningService> = emptyList(),
    val durations: List<CleaningDuration> = emptyList()
)
@Parcelize
data class CleaningService(
    val uid: String,
    val serviceType: String,
    val serviceName: String,
    val image: String,
    val tasks: List<String>
) : Parcelable
@Parcelize
data class CleaningDuration (
    val uid: String,
    val workingHour: Int,
    val fee: Int,
    val description: String
) : Parcelable