package com.project.job.data.source.remote

import com.project.job.data.network.ApiService
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.PaymentDataSource
import com.project.job.data.source.remote.api.response.PaymentResponse

class PaymentRemote(private val apiService: ApiService) : PaymentDataSource {
    override suspend fun getHistoryPayment(): NetworkResult<PaymentResponse> {
        return safeApiCall {
            apiService.getHistoryPayment()
        }
    }

    companion object {
        private var instance: PaymentRemote? = null
        fun getInstance(): PaymentRemote {
            if (instance == null) {
                instance = PaymentRemote(RetrofitClient.apiService)
            }
            return instance!!
        }
    }
}