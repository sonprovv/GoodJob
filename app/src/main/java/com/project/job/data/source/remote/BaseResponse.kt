package com.project.job.data.source.remote

data class BaseResponse<T> (
    val success: Boolean,
    val message: String,
    val data: T
)