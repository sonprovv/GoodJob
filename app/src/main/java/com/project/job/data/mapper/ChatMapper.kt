package com.project.job.data.mapper

import com.project.job.data.source.local.room.entity.ChatEntity
import com.project.job.data.source.remote.api.response.chat.ConversationData
import com.project.job.data.source.remote.api.response.chat.SenderData

object ChatMapper {
    /**
     * Convert ConversationData (from API) to ChatEntity (for Room database)
     */
    fun toEntity(conversationData: ConversationData): ChatEntity {
        return ChatEntity(
            id = conversationData.id,
            senderId = conversationData.senderId,
            senderUsername = conversationData.sender.username,
            senderName = conversationData.sender.name,
            senderAvatar = conversationData.sender.avatar,
            lastMessage = conversationData.lastMessage,
            lastMessageTime = conversationData.lastMessageTime,
            unreadCount = conversationData.unreadCount,
            updatedAt = conversationData.updatedAt
        )
    }

    /**
     * Convert list of ConversationData to list of ChatEntity
     */
    fun toEntityList(conversationDataList: List<ConversationData>): List<ChatEntity> {
        return conversationDataList.map { toEntity(it) }
    }

    /**
     * Convert ChatEntity back to ConversationData (if needed for UI)
     */
    fun toConversationData(entity: ChatEntity): ConversationData {
        return ConversationData(
            id = entity.id,
            senderId = entity.senderId,
            lastMessage = entity.lastMessage,
            lastMessageTime = entity.lastMessageTime,
            unreadCount = entity.unreadCount,
            updatedAt = entity.updatedAt,
            sender = SenderData(
                id = entity.senderId,
                username = entity.senderUsername,
                name = entity.senderName,
                avatar = entity.senderAvatar,
                dob = "",
                tel = "",
                email = "",
                location = "",
                gender = "",
                userType = ""
            )
        )
    }

    /**
     * Convert list of ChatEntity to list of ConversationData
     */
    fun toConversationDataList(entities: List<ChatEntity>): List<ConversationData> {
        return entities.map { toConversationData(it) }
    }
}
