package com.project.job.data.repository.implement

import android.content.Context
import android.util.Log
import com.project.job.data.mapper.JobMapper
import com.project.job.data.repository.JobRepository
import com.project.job.data.source.local.room.AppDatabase
import com.project.job.data.source.local.room.JobDAO
import com.project.job.data.source.local.room.entity.JobEntity
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.ServiceRemote
import com.project.job.data.source.remote.api.response.CancelJobResponse
import kotlinx.coroutines.flow.Flow

class JobRepositoryImpl(context: Context) : JobRepository {
    private val jobDao: JobDAO = AppDatabase(context).getJobDAO()
    private val serviceRemote = ServiceRemote.getInstance()

    companion object {
        private const val TAG = "JobRepositoryImpl"
        
        @Volatile
        private var instance: JobRepositoryImpl? = null

        fun getInstance(context: Context): JobRepositoryImpl {
            return instance ?: synchronized(this) {
                instance ?: JobRepositoryImpl(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Get all jobs from local database as Flow
     * UI will automatically update when data changes
     */
    override fun getAllJobsLocal(): Flow<List<JobEntity>> {
        Log.d(TAG, "Getting all jobs from local database")
        return jobDao.getAllJobs()
    }

    /**
     * Get jobs by user ID from local database as Flow
     */
    override fun getJobsByUserLocal(userId: String): Flow<List<JobEntity>> {
        Log.d(TAG, "Getting jobs for user: $userId from local database")
        return jobDao.getJobsByUser(userId)
    }

    /**
     * Get jobs by status from local database as Flow
     */
    override fun getJobsByStatusLocal(status: String): Flow<List<JobEntity>> {
        Log.d(TAG, "Getting jobs with status: $status from local database")
        return jobDao.getJobsByStatus(status)
    }

    /**
     * Fetch jobs from remote API and save to local database
     * This will automatically trigger UI update via Flow
     */
    override suspend fun fetchAndSaveJobs(userId: String): NetworkResult<Unit> {
        return try {
            Log.d(TAG, "Fetching jobs from remote API for user: $userId")
            val response = serviceRemote.getUserPostJobs(userId)
            
            when (response) {
                is NetworkResult.Success -> {
                    val jobs = response.data.jobs
                    Log.d(TAG, "Fetched ${jobs.size} jobs from API")
                    
                    // Convert API response to Room entities
                    val jobEntities = JobMapper.toEntityList(jobs)
                    
                    // Xóa tất cả jobs cũ của user trước khi insert jobs mới
                    jobDao.deleteJobsByUser(userId)
                    Log.d(TAG, "Deleted old jobs for user: $userId")
                    
                    // Save to local database
                    jobDao.insertJobs(jobEntities)
                    Log.d(TAG, "Saved ${jobEntities.size} jobs to local database")
                    
                    NetworkResult.Success(Unit)
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Error fetching jobs: ${response.message}")
                    NetworkResult.Error(response.message)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception fetching jobs: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Cancel a job (both remote and local)
     */
    override suspend fun cancelJob(serviceType: String, jobId: String): NetworkResult<CancelJobResponse> {
        return try {
            Log.d(TAG, "Cancelling job: $jobId, serviceType: $serviceType")
            val response = serviceRemote.cancelJob(serviceType, jobId)
            
            when (response) {
                is NetworkResult.Success -> {
                    // Update local database
                    updateJobStatus(jobId, "Cancel")
                    Log.d(TAG, "Job cancelled successfully: $jobId")
                    response
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Error cancelling job: ${response.message}")
                    response
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception cancelling job: ${e.message}", e)
            NetworkResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Update job status in local database
     */
    override suspend fun updateJobStatus(jobId: String, status: String) {
        try {
            Log.d(TAG, "Updating job status: $jobId -> $status")
            jobDao.updateJobStatus(jobId, status)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating job status: ${e.message}", e)
        }
    }

    /**
     * Delete all jobs from local database
     */
    override suspend fun clearLocalJobs() {
        try {
            Log.d(TAG, "Clearing all local jobs")
            jobDao.deleteAllJobs()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing local jobs: ${e.message}", e)
        }
    }

    /**
     * Insert jobs into local database
     */
    override suspend fun insertJobs(jobs: List<JobEntity>) {
        try {
            Log.d(TAG, "Inserting ${jobs.size} jobs into local database")
            jobDao.insertJobs(jobs)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting jobs: ${e.message}", e)
        }
    }

    /**
     * Get a single job by ID
     */
    override suspend fun getJobById(jobId: String): JobEntity? {
        return try {
            Log.d(TAG, "Getting job by ID: $jobId")
            jobDao.getJobById(jobId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting job by ID: ${e.message}", e)
            null
        }
    }
}
