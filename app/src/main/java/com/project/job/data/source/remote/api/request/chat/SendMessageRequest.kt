package com.project.job.data.source.remote.api.request.chat

data class SendMessageRequest(
    val receiverId: String,
    val message: String,
    val type: String = "text"
)
