package com.project.job.data.source.remote.api.response

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import java.lang.reflect.Type

data class WorkerOrderJobResponse(
    val success: Boolean,
    val message: String,
    val orders: List<WorkerOrder>
)

data class WorkerOrder(
    val uid : String,
    val worker: WorkerInfo,
    val isReview: Boolean,
    val status: String,
    val createdAt: String,
    val serviceType: String,
    val review: ReviewWorker?=null
)

data class ReviewWorker(
    val uid: String,
    val rating: Int,
    val comment: String
)

data class WorkerInfo(
    val uid : String,
    val gender: String,
    val avatar: String,
    val tel: String,
    val location: String,
    @JsonAdapter(DobDeserializer::class)
    val dob: String,
    val description: String,
    val username : String,
    val email: String,
    val role: String
)

class DobDeserializer : JsonDeserializer<String> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): String {
        return when {
            json?.isJsonPrimitive == true && json.asJsonPrimitive.isString -> {
                // Nếu là string, trả về trực tiếp
                json.asString
            }
            json?.isJsonObject == true -> {
                // Nếu là object với _seconds, convert thành date string
                val jsonObject = json.asJsonObject
                val seconds = jsonObject.get("_seconds")?.asLong ?: 0L
                
                // Convert timestamp to date string (dd/MM/yyyy format)
                val date = java.util.Date(seconds * 1000)
                val formatter = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                formatter.format(date)
            }
            else -> {
                // Fallback
                "Chưa cập nhật"
            }
        }
    }
}
