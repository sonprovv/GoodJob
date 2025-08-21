package com.project.job.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String = "",
    // Add any additional user properties you need
    val role: String = "candidate" // Default role
)
