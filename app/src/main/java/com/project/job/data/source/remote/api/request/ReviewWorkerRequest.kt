package com.project.job.data.source.remote.api.request

data class ReviewWorkerRequest(
    val userID: String,
    val workerID: String,
    val orderID: String,
    val rating: Int,
    val comment: String,
    val serviceType: String
)
