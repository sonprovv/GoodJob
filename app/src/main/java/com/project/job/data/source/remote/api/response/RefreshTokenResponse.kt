package com.project.job.data.source.remote.api.response

data class RefreshTokenResponse(
    val success: Boolean,
    val message: String,
    val data : DataRefreshToken
)

data class DataRefreshToken(
    val idToken: String,
    val refreshToken: String
)
