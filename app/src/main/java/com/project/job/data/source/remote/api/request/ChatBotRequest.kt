package com.project.job.data.source.remote.api.request

data class ChatBotRequest(
    val query: String,
    val reference: ReferenceData
)

data class ReferenceData(
    val location: LocationData,
)

data class LocationData(
    val name : String,
    val lat: Double,
    val lon: Double
)