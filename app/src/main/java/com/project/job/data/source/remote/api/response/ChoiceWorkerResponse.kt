package com.project.job.data.source.remote.api.response

data class ChoiceWorkerResponse(
    val success: Boolean,
    val message: String,
    val updatedOrder : UpdatedOrder
)

data class UpdatedOrder(
    val uid : String,
    val workerID : String,
    val jobID : String,
    val serviceType : String,
    val createdAt : String,
    val isReview : Boolean,
    val status : String
)
