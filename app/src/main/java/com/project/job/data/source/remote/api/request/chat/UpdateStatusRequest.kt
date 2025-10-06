package com.project.job.data.source.remote.api.request.chat

/** state must be one of: "online" | "offline" */
data class UpdateStatusRequest(
    val state: String
)
