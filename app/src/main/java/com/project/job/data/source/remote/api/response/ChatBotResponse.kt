package com.project.job.data.source.remote.api.response
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ChatBotResponse(
    val data : JsonElement,
    val message : String,
    val success : Boolean,
    val type: String // "Job" hoặc "Info"
)

// Dùng cho response type = "Job" 
data class QueryJobs(
    val context: String? = null,
    val createdAt: String,
    val jobID: String,
    val lat: Double? = null,
    val listDays: List<String>,
    val location: String,
    val lon: Double? = null,
    val price: Double,
    val serviceType: String,
    @SerializedName("similarity_score")
    val similarityScore: Double? = null,
    val startTime: String,
    val status: String? = null,
    val userID: String
)
