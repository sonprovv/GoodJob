package com.project.job.data.source.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ChatEntity(
    @PrimaryKey
    val id: String,
    val senderId: String,
    val senderUsername: String,
    val senderName: String,
    val senderAvatar: String,
    val lastMessage: String?,
    val lastMessageTime: Long,
    val unreadCount: Int = 0,
    val updatedAt: String
)
