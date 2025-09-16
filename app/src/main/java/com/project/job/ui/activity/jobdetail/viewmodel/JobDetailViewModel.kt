package com.project.job.ui.activity.jobdetail.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.ServiceRepository
import com.project.job.data.source.remote.api.response.WorkerOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JobDetailViewModel: ViewModel() {
    private val serviceRepository = ServiceRepository()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _workers = MutableStateFlow<List<WorkerOrder>?>(null)
    val workers: StateFlow<List<WorkerOrder>?> = _workers

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success

    fun getListWorker(token: String, jobID: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = serviceRepository.getWorkerOrderJob(token=token, jobID = jobID)
                Log.d("JobDetailViewModel", "JobDetail response: $response")
                Log.d("JobDetailViewModel", "JobDetail response: $token, $jobID")

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null && userResponse.success) {
                        _workers.value = userResponse.orders
                        _error.value = null
                        Log.d("JobDetailViewModel", "JobDetail successful: $userResponse")
                        _success.value = true
                    } else {
                        _error.value = userResponse?.message ?: "JobDetail failed"
                    }
                } else {
                    _error.value = response.message() ?: "JobDetail failed"
                }

            } catch (e: Exception) {
                Log.e("JobDetailViewModel", "JobDetail error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("ActivityViewModel", "Activity finally")
                _loading.value = false
            }
        }
    }
}