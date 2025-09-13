package com.project.job.ui.activity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.ServiceRepository
import com.project.job.data.source.remote.api.response.DataJobs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ActivityViewModel : ViewModel() {
    private val serviceRepository = ServiceRepository()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _jobs = MutableStateFlow<List<DataJobs>?>(null)
    val jobs: StateFlow<List<DataJobs>?> = _jobs

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success

    fun getListJob(token: String, uid: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = serviceRepository.getUserPostJobs(token=token, uid=uid)
                Log.d("ActivityViewModel", "Activity response: $response")
                Log.d("ActivityViewModel", "Activity response: $token, $uid")

                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null && userResponse.success) {
                        _jobs.value = userResponse.jobs
                        _error.value = null
                        Log.d("ActivityViewModel", "Activity successful: $userResponse")
                        _success.value = true
                    } else {
                        _error.value = userResponse?.message ?: "Activity failed"
                    }
                } else {
                    _error.value = response.message() ?: "Activity failed"
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

}