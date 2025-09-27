package com.project.job.data.model

data class OpenAIResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage
) {
    data class Choice(
        val message: Message,
        val finish_reason: String
    ) {
        data class Message(
            val role: String,
            val content: String
        )
    }

    data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )
}
