package com.project.job.data.source.remote.api.response
import com.google.gson.JsonElement

data class ChatBotResponse(
    val data : JsonElement,
    val message : String,
    val success : Boolean,
    val type: String // "Job" hoặc "Info"
)

// Dùng cho response type = "Job" 
data class QueryJobs(
    val createdAt: String,
    val jobID: String,
    val listDays: List<String>,
    val location: String,
    val price: Double,
    val serviceType: String,
    val startTime: String,
    val status: String,
    val userID: String
)
