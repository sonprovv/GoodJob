package com.project.job.data.source.local.room

import androidx.room.*
import com.project.job.data.source.local.room.entity.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDAO {
    // Insert or update a single job
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity): Long

    // Insert or update multiple jobs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    // Delete a job
    @Delete
    suspend fun deleteJob(job: JobEntity)

    // Get all jobs
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    // Get jobs by status
    @Query("SELECT * FROM jobs WHERE status = :status ORDER BY createdAt DESC")
    fun getJobsByStatus(status: String): Flow<List<JobEntity>>

    // Get jobs by service type
    @Query("SELECT * FROM jobs WHERE serviceType = :serviceType ORDER BY createdAt DESC")
    fun getJobsByServiceType(serviceType: String): Flow<List<JobEntity>>

    // Get jobs by user ID
    @Query("SELECT * FROM jobs WHERE userId = :userId ORDER BY createdAt DESC")
    fun getJobsByUser(userId: String): Flow<List<JobEntity>>

    // Get a single job by ID
    @Query("SELECT * FROM jobs WHERE uid = :jobId LIMIT 1")
    suspend fun getJobById(jobId: String): JobEntity?

    // Get jobs within a date range
    @Query("SELECT * FROM jobs WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getJobsByDateRange(startDate: String, endDate: String): Flow<List<JobEntity>>

    // Get jobs by location
    @Query("SELECT * FROM jobs WHERE location LIKE '%' || :location || '%' ORDER BY createdAt DESC")
    fun searchJobsByLocation(location: String): Flow<List<JobEntity>>

    // Get jobs by price range
    @Query("SELECT * FROM jobs WHERE price BETWEEN :minPrice AND :maxPrice ORDER BY price ASC")
    fun getJobsByPriceRange(minPrice: Int, maxPrice: Int): Flow<List<JobEntity>>

    // Update job status
    @Query("UPDATE jobs SET status = :status WHERE uid = :jobId")
    suspend fun updateJobStatus(jobId: String, status: String)

    // Delete all jobs
    @Query("DELETE FROM jobs")
    suspend fun deleteAllJobs()

    // Delete jobs by user ID
    @Query("DELETE FROM jobs WHERE userId = :userId")
    suspend fun deleteJobsByUser(userId: String)

    // Get count of jobs by status
    @Query("SELECT COUNT(*) FROM jobs WHERE status = :status")
    fun getJobCountByStatus(status: String): Flow<Int>

    // Search jobs by multiple criteria
    @Query("""
        SELECT * FROM jobs 
        WHERE (serviceType = :serviceType OR :serviceType IS NULL)
        AND (status = :status OR :status IS NULL)
        AND (price >= :minPrice OR :minPrice IS NULL)
        AND (price <= :maxPrice OR :maxPrice IS NULL)
        AND (location LIKE '%' || :location || '%' OR :location IS NULL)
        ORDER BY createdAt DESC
    """)
    fun searchJobs(
        serviceType: String? = null,
        status: String? = null,
        minPrice: Int? = null,
        maxPrice: Int? = null,
        location: String? = null
    ): Flow<List<JobEntity>>
}