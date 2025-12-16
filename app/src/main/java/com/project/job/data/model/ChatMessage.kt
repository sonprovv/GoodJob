package com.project.job.data.model

import com.project.job.data.source.remote.api.response.QueryJobs

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    val isTyping: Boolean = false,
    val messageType: ChatMessageType = ChatMessageType.TEXT,
    val jobList: List<QueryJobs>? = null
)

enum class ChatMessageType {
    TEXT,      // Dạng text thông thường
    JOB_LIST,  // Dạng danh sách job
    INFO       // Dạng thông tin từ AI
}
