package com.project.job.ui.activity.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.JobRepository
import com.project.job.data.repository.implement.JobRepositoryImpl
import com.project.job.data.source.local.room.entity.JobEntity
import com.project.job.data.source.remote.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val jobRepository: JobRepository = JobRepositoryImpl.getInstance(application)
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Local jobs from Room database - auto-update UI when data changes
    private val _localJobs = MutableStateFlow<List<JobEntity>>(emptyList())
    val localJobs: StateFlow<List<JobEntity>> = _localJobs

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success

    /**
     * Start observing local jobs from Room database
     * This will automatically update UI when data changes
     */
    fun observeLocalJobs(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("ActivityViewModel", "Starting to observe local jobs for user: $userId")
                jobRepository.getJobsByUserLocal(userId).collect { jobs ->
                    Log.d("ActivityViewModel", "Local jobs updated: ${jobs.size} jobs")
                    _localJobs.value = jobs
                }
            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Error observing local jobs: ${e.message}")
            }
        }
    }

    /**
     * Fetch jobs from API and save to Room database
     * This will trigger UI update automatically via Flow
     */
    fun refreshJobs(uid: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                Log.d("ActivityViewModel", "Fetching jobs from API for user: $uid")
                val result = jobRepository.fetchAndSaveJobs(uid)
                
                when (result) {
                    is NetworkResult.Success -> {
                        Log.d("ActivityViewModel", "Jobs refreshed successfully")
                        _success.value = true
                    }
                    is NetworkResult.Error -> {
                        Log.e("ActivityViewModel", "Error refreshing jobs: ${result.message}")
                        _error.value = result.message
                        _success.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Exception refreshing jobs: ${e.message}")
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    /**
     * Cancel a job (both API and local database)
     * Local database will auto-update via Flow
     */
    fun cancelJob(serviceType: String, jobID: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null

            var retryCount = 0
            val maxRetries = 3
            var delayMs = 1000L // Start with 1 second

            while (retryCount < maxRetries) {
                try {
                    Log.d(
                        "ActivityViewModel",
                        "Cancel job attempt ${retryCount + 1}/$maxRetries for job $jobID"
                    )

                    // Cancel job via repository (handles both remote and local)
                    val response = jobRepository.cancelJob(serviceType = serviceType, jobId = jobID)
                    Log.d("ActivityViewModel", "Cancel job response: $response")

                    when (response) {
                        is NetworkResult.Success -> {
                            Log.d("ActivityViewModel", "Job cancelled successfully: $jobID")
                            _success.value = true
                            _loading.value = false
                            return@launch // Success, exit retry loop
                        }

                        is NetworkResult.Error -> {
                            // Nếu là maintenance job và timeout, thử alternative endpoint
                            if (serviceType == "maintenance" && response.message.contains("timeout")) {
                                Log.d(
                                    "ActivityViewModel",
                                    "Maintenance cancel timeout, trying alternative endpoint..."
                                )
                                return@launch
                            }

                            _error.value = response.message
                            _success.value = false
                            _loading.value = false
                            return@launch // Error, exit retry loop
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        "ActivityViewModel",
                        "Cancel job error attempt ${retryCount + 1}: ${e.message}"
                    )
                    retryCount++

                    if (retryCount < maxRetries) {
                        Log.d("ActivityViewModel", "Retrying cancel job in ${delayMs}ms...")
                        kotlinx.coroutines.delay(delayMs)
                        delayMs *= 2 // Exponential backoff: 1s, 2s, 4s
                    } else {
                        Log.e("ActivityViewModel", "Max retries exceeded for cancel job")
                        _error.value =
                            "Không thể hủy bài đăng sau ${maxRetries} lần thử. Vui lòng thử lại sau."
                        _success.value = false
                    }
                } finally {
                    if (retryCount >= maxRetries) {
                        _loading.value = false
                    }
                }
            }
        }
    }

    /**
     * Clear all local jobs (useful for logout)
     */
    fun clearLocalJobs() {
        viewModelScope.launch {
            try {
                Log.d("ActivityViewModel", "Clearing local jobs")
                jobRepository.clearLocalJobs()
            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Error clearing local jobs: ${e.message}")
            }
        }
    }


}