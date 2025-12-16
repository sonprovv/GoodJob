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
}
