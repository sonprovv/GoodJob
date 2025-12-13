package com.project.job.data.source.remote.api.response
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

// New API response format (v2.0)
data class ChatBotResponse(
    val context: String,  // Natural language response with [jobID] format
    val jobs: List<QueryJobs>? = null,  // Job list (for job intent)
    val intent: String,  // "job", "info", "policy", "general"
    @SerializedName("session_id")
    val sessionId: String? = null,
    val metadata: ChatBotMetadata? = null,
    
    // Legacy fields for backward compatibility
    val data: JsonElement? = null,
    val message: String? = null,
    val success: Boolean? = null,
    val type: String? = null
)

data class ChatBotMetadata(
    @SerializedName("total_candidates")
    val totalCandidates: Int? = null,
    @SerializedName("final_count")
    val finalCount: Int? = null,
    val method: String? = null,
    val query: String? = null,
    @SerializedName("conversation_length")
    val conversationLength: Int? = null,
    val error: String? = null  // For access_denied and other errors
)

// Job model
data class QueryJobs(
    val context: String? = null,  // Deprecated, use ChatBotResponse.context instead
    val createdAt: String? = null,
    val jobID: String,
    val lat: Double? = null,
    val listDays: List<String>? = null,
    val location: String? = null,
    val lon: Double? = null,
    val price: Double? = null,
    val serviceType: String? = null,
    @SerializedName("similarity_score")
    val similarityScore: Double? = null,
    val startTime: String? = null,
    val status: String? = null,
    val userID: String? = null,
    val extraServices: List<String>? = null  // New field
)
