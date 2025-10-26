package com.project.job.data.source.remote

import com.project.job.data.network.ApiService
import com.project.job.data.network.RetrofitClient
import com.project.job.data.source.PolicyDataSource
import com.project.job.data.source.remote.api.response.GetPoliciesResponse

class PolicyRemote (private val apiService: ApiService) : PolicyDataSource {
    override suspend fun getPrivacyPolicy() : NetworkResult<GetPoliciesResponse> = safeApiCall {
        apiService.getPolicies()
    }

    companion object {
        private var instance: PolicyRemote? = null
        fun getInstance(): PolicyRemote {
            if (instance == null) {
                instance = PolicyRemote(RetrofitClient.apiService)
            }
            return instance!!
        }
    }
}