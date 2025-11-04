package com.project.job.data.source.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.project.job.data.source.local.room.converter.ListStringConverter

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey
    val uid: String,
    val startTime: String,
    val serviceType: String,
    val workerQuantity: Int? = null,
    val price: Int,
    @field:TypeConverters(ListStringConverter::class)
    val listDays: List<String>,
    val status: String,
    val location: String,
    val isCooking: Boolean? = null,
    val isIroning: Boolean? = null,
    val createdAt: String,
    val userId: String,
    val duration: String? = null,
    val services: String? = null, // Will store JSON string of services
    val shift: String? = null,    // Will store JSON string of shift
    val userName: String? = null,
    val userAvatar: String? = null,
    val userPhone: String? = null
) {
    // Helper property to match the old field name
    val id: String get() = uid
    
    // Helper property to match the old field name
    val typeService: String get() = serviceType
    
    // Helper property to match the old field name
    val createAt: String get() = createdAt
}
