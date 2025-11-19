package com.project.job.data.source.remote.api.request

import com.google.gson.annotations.SerializedName

data class ChatBotRequest(
    val query: String,
    val reference: ReferenceData
)

data class ReferenceData(
    val location: LocationData,
    val experiences: ExperienceData
)

data class LocationData(
    val name : String,
    val lat: Double,
    val lon: Double
)

data class ExperienceData(
    @SerializedName("CLEANING")
    val cleaning: Double = 2.0,
    @SerializedName("HEALTHCARE")
    val healthcare: Double = 2.0,
    @SerializedName("MAINTENANCE")
    val maintenance: Double = 1.0
)
