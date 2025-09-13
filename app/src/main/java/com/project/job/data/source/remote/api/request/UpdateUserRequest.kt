package com.project.job.data.source.remote.api.request

import com.google.gson.annotations.SerializedName
import com.project.job.data.source.remote.api.response.User

data class UpdateUserRequest(
    @field:SerializedName("uid") val uid: String,
    @field:SerializedName("username") val username: String,
    @field:SerializedName("gender") val gender: String,
    @field:SerializedName("dob") val dob: String,
    @field:SerializedName("avatar") val avatar: String,
    @field:SerializedName("email") val email: String,
    @field:SerializedName("tel") val tel: String,
    @field:SerializedName("location") val location: String,
    @field:SerializedName("role") val role: String? = null,
    @field:SerializedName("provider") val provider: String? = null
)

// Extension function to convert User to UpdateUserRequest
fun User.toUpdateRequest(): UpdateUserRequest {
    return UpdateUserRequest(
        uid = this.uid,
        username = this.username,
        gender = this.gender,
        dob = this.dob,
        avatar = this.avatar,
        email = this.email,
        tel = this.tel,
        location = this.location,
        role = this.role,
        provider = this.provider
    )
}