package com.project.job.data.source.remote.api.response.chat

data class GetMessagesResponse(
    val success: Boolean,
    val message: String,
    val messages: List<MessageData>,
)
