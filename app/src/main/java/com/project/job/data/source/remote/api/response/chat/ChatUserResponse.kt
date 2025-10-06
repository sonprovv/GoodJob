package com.project.job.data.source.remote.api.response.chat

data class ChatUserResponse(
    val userId: String,
    val username: String?,
    val avatar: String?,
    val isOnline: Boolean? = null
)
