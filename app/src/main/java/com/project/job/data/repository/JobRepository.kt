package com.project.job.data.repository

import com.project.job.data.source.local.room.entity.JobEntity
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.response.CancelJobResponse
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    /**
     * Get jobs by user ID from local database
     */
    fun getJobsByUserLocal(userId: String): Flow<List<JobEntity>>

    /**
     * Fetch jobs from remote API and save to local database
     * This will trigger UI update automatically via Flow
     */
    suspend fun fetchAndSaveJobs(userId: String): NetworkResult<Unit>

    /**
     * Cancel a job (both remote and local)
     */
    suspend fun cancelJob(serviceType: String, jobId: String): NetworkResult<CancelJobResponse>

    /**
     * Update job status in local database
     */
    suspend fun updateJobStatus(jobId: String, status: String)

    /**
     * Delete all jobs from local database
     */
    suspend fun clearLocalJobs()
}
