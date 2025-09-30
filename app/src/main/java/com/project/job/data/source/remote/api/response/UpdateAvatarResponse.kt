package com.project.job.data.source.remote.api.response

data class UpdateAvatarResponse(
    val success: Boolean,
    val message: String? = null,
    val url: String? = null,
    val data: String? = null // Backend có thể trả về data thay vì url
)
