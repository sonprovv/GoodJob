package com.project.job.data.source

import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.response.PaymentResponse

interface PaymentDataSource {
    suspend fun getHistoryPayment() : NetworkResult<PaymentResponse>
}