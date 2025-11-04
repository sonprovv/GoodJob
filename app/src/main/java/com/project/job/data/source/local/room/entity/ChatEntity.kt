package com.project.job.data.source.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ChatEntity(
    @PrimaryKey
    val id: String,
    val senderId: String,
    val receiverId: String,
    val message: String,
    val timestamp: Long,
    val avatar: String,
    val name: String,
    val status: String
)
