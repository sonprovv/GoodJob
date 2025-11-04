package com.project.job.data.source.remote.api.response.chat

data class MediaResponse(
    val id: String,
    val messageId: String,
    val mediaType: String,
    val url: String,
    val thumbnailUrl: String?,
    val createdAt: String
)
