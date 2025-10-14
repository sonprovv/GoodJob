package com.project.job.data.source.remote.api.response

data class PaymentResponse(
    val success: String,
    val message: String,
    val payments : List<PaymentData>
)

data class PaymentData(
    val uid: String,
    val jobID: String,
    val userID: String,
    val amount: Int,
    val serviceType: String,
    val createdAt: String
)
