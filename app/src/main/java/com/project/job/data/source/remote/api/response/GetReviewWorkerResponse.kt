package com.project.job.data.source.remote.api.response

data class GetReviewWorkerResponse(
    val success : Boolean,
    val message : String,
    val experiences : Map<String, ServiceExperience>
)

data class ServiceExperience(
    val rating: Double,
    val reviews: List<Review>
)

data class Review(
    val uid: String,
    val user: UserInfo,
    val rating: Int,
    val comment: String,
) {
    // Extension function to add service type
    fun withServiceType(serviceType: String): ExtendedReview {
        return ExtendedReview(uid, user, rating, comment, serviceType)
    }
}

data class ExtendedReview(
    val uid: String,
    val user: UserInfo,
    val rating: Int,
    val comment: String,
    val serviceType: String
)
