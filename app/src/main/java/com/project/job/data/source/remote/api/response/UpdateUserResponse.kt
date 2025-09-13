package com.project.job.data.source.remote.api.response

data class UpdateUserResponse (
    val success: Boolean,
    val message: String,
    val data: DataUser? = null,
    val user: User? = null
)
