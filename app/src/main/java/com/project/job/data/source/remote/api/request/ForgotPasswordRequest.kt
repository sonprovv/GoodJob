package com.project.job.data.source.remote.api.request


data class ForgotPasswordRequest(
    val email: String,
    val newPassword: String,
    val confirmPassword: String,
    val code: String,
    val codeEnter: String
)
