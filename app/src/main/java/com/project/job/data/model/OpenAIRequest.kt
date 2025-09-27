package com.project.job.data.model

data class OpenAIRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>,
    val max_tokens: Int = 500,
    val temperature: Double = 0.7
) {
    data class Message(
        val role: String,
        val content: String
    )
}
