package com.project.job.data.source.remote.api.response

data class UpdateAvatarResponse(
    val success: Boolean,
    val message: String,
    val url: String? = null
)
