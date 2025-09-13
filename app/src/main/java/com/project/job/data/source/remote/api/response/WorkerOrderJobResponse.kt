package com.project.job.data.source.remote.api.response

data class WorkerOrderJobResponse(
    val success: Boolean,
    val message: String,
    val orders: List<WorkerOrder>
)

data class WorkerOrder(
    val uid : String,
    val worker: WorkerInfo,
    val isReview: Boolean,
    val status: String,
    val createdAt: String,
    val serviceType: String,
)

data class WorkerInfo(
    val uid : String,
    val gender: String,
    val avatar: String,
    val tel: String,
    val location: String,
    val dob: String,
    val description: String,
    val username : String,
    val email: String,
    val role: String
)
