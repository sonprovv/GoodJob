package com.project.job.data.source.remote.api.response

data class GetPoliciesResponse(
    val success: Boolean,
    val message: String,
    val data: PolicyData
)

data class PolicyData(
    val markdownContent: String,
    val htmlContent: String
)
