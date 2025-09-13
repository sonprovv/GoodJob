package com.project.job.data.source.remote.api.request

data class ChangePasswordRequest(
    val newPassword: String,
    val confirmPassword: String

)
