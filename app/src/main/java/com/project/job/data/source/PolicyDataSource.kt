package com.project.job.data.source

import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.response.GetPoliciesResponse

interface PolicyDataSource {
    suspend fun getPrivacyPolicy(): NetworkResult<GetPoliciesResponse>
}