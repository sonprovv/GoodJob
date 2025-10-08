package com.project.job.ui.activity.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.ServiceRemote
import com.project.job.data.source.remote.api.response.DataJobs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActivityViewModel : ViewModel() {
    private val serviceRepository = ServiceRemote.getInstance()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _jobs = MutableStateFlow<List<DataJobs>?>(null)
    val jobs: StateFlow<List<DataJobs>?> = _jobs

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success

    fun getListJob(uid: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = serviceRepository.getUserPostJobs(uid=uid)
                Log.d("ActivityViewModel", "Activity response: $response")
                Log.d("ActivityViewModel", "Activity response: $uid")
                when(response) {
                    is NetworkResult.Success -> {
                        _jobs.value = response.data.jobs
                        _success.value = true
                    }
                    is NetworkResult.Error -> {
                        _error.value = response.message
                        _success.value = false
                    }

                }

            } catch (e: Exception) {
                Log.e("ActivityViewModel", "Activity error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("ActivityViewModel", "Activity finally")
                _loading.value = false
            }
        }
    }

    fun cancelJob(serviceType: String, jobID: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = serviceRepository.cancelJob(serviceType = serviceType, jobID = jobID)
                Log.d("ActivityViewModel", "Activity response: $response")
                Log.d("ActivityViewModel", "Activity response: $jobID")

                when(response) {
                    is NetworkResult.Success -> {
                        _success.value = true
                    }
                    is NetworkResult.Error -> {
                        _error.value = response.message
                        _success.value = false
                    }
                }
            }
            catch (e: Exception) {
                Log.e("ActivityViewModel", "Activity error: ${e.message}")
                _error.value = e.message
            }
            finally {
                Log.e("ActivityViewModel", "Activity finally")
                _loading.value = false
            }
        }
    }

}