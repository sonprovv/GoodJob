package com.project.job.data.source.remote.api.response

data class UserResponse (
    val success: Boolean,
    val message: String,
    val data: DataUser
)
data class DataUser(
    val user: User,
    val token: String,
    val refreshToken: String
)
data class User(
    val uid: String = "",
    val username: String = "",
    val gender: String = "",
    val dob: String = "",
    val avatar: String = "",
    val email: String = "",
    val tel: String = "",
    val location: String = "Chưa cập nhật",
    val role: String = "user",
    val provider: String
)