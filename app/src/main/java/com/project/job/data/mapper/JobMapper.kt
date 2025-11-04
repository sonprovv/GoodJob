package com.project.job.data.mapper

import com.google.gson.Gson
import com.project.job.data.source.local.room.entity.JobEntity
import com.project.job.data.source.remote.api.response.DataJobs

object JobMapper {
    private val gson = Gson()

    /**
     * Convert DataJobs (from API) to JobEntity (for Room database)
     */
    fun toEntity(dataJobs: DataJobs): JobEntity {
        return JobEntity(
            uid = dataJobs.uid,
            startTime = dataJobs.startTime,
            serviceType = dataJobs.serviceType,
            workerQuantity = dataJobs.workerQuantity,
            price = dataJobs.price,
            listDays = dataJobs.listDays,
            status = dataJobs.status,
            location = dataJobs.location,
            isCooking = dataJobs.isCooking,
            isIroning = dataJobs.isIroning,
            createdAt = dataJobs.createdAt,
            userId = dataJobs.user.uid,
            duration = dataJobs.duration?.let { gson.toJson(it) },
            services = dataJobs.services?.let { gson.toJson(it) },
            shift = dataJobs.shift?.let { gson.toJson(it) },
            userName = dataJobs.user.username,
            userAvatar = dataJobs.user.avatar,
            userPhone = dataJobs.user.tel
        )
    }

    /**
     * Convert list of DataJobs to list of JobEntity
     */
    fun toEntityList(dataJobsList: List<DataJobs>): List<JobEntity> {
        return dataJobsList.map { toEntity(it) }
    }

    /**
     * Convert JobEntity back to DataJobs (if needed for UI)
     * Note: This requires parsing JSON strings back to objects
     */
    fun toDataJobs(entity: JobEntity, user: com.project.job.data.source.remote.api.response.UserInfo): DataJobs {
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
            user = user,
            duration = entity.duration?.let { 
                try {
                    gson.fromJson(it, com.project.job.data.source.remote.api.response.CleaningDuration::class.java)
                } catch (e: Exception) {
                    null
                }
            },
            services = entity.services?.let {
                try {
                    gson.fromJson(it, Array<com.project.job.data.source.remote.api.response.ServiceHealthcare>::class.java).toList()
                } catch (e: Exception) {
                    null
                }
            },
            shift = entity.shift?.let {
                try {
                    gson.fromJson(it, com.project.job.data.source.remote.api.response.HealthcareShift::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        )
    }
}
