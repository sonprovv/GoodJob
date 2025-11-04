package com.project.job.data.mapper

import com.google.gson.Gson
import com.project.job.data.source.local.room.entity.JobEntity
import com.project.job.data.source.remote.api.response.*

object JobEntityToDataJobsMapper {
    private val gson = Gson()

    /**
     * Convert JobEntity to DataJobs for UI display
     */
    fun toDataJobs(entity: JobEntity): DataJobs {
        // Create UserInfo from stored user data
        val userInfo = UserInfo(
            uid = entity.userId,
            gender = "", // Not stored in JobEntity
            dob = "", // Not stored in JobEntity
            location = "", // Not stored in JobEntity
            avatar = entity.userAvatar ?: "",
            tel = entity.userPhone ?: "",
            username = entity.userName ?: "",
            email = "", // Not stored in JobEntity
            role = "" // Not stored in JobEntity
        )

        // Parse JSON strings back to objects
        val duration = entity.duration?.let {
            try {
                gson.fromJson(it, CleaningDuration::class.java)
            } catch (e: Exception) {
                null
            }
        }

        val services = entity.services?.let {
            try {
                gson.fromJson(it, Array<ServiceHealthcare>::class.java).toList()
            } catch (e: Exception) {
                null
            }
        }

        val shift = entity.shift?.let {
            try {
                gson.fromJson(it, HealthcareShift::class.java)
            } catch (e: Exception) {
                null
            }
        }

        return DataJobs(
            uid = entity.uid,
            startTime = entity.startTime,
            serviceType = entity.serviceType,
            workerQuantity = entity.workerQuantity,
            price = entity.price,
            listDays = entity.listDays,
            status = entity.status,
            location = entity.location,
            isCooking = entity.isCooking,
            isIroning = entity.isIroning,
            createdAt = entity.createdAt,
            user = userInfo,
            duration = duration,
            services = services,
            shift = shift
        )
    }

    /**
     * Convert list of JobEntity to list of DataJobs
     */
    fun toDataJobsList(entities: List<JobEntity>): List<DataJobs> {
        return entities.map { toDataJobs(it) }
    }
}
