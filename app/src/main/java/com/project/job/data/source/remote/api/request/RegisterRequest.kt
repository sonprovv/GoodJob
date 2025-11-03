package com.project.job.data.source.remote.api.request

data class RegisterRequest(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val role: String
)