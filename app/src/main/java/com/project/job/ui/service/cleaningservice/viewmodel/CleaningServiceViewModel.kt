package com.project.job.ui.service.cleaningservice.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.ServiceRemote
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CleaningService
import com.project.job.data.source.remote.api.response.NewJobCleaning
import com.project.job.data.source.remote.api.response.NewJobHealthcare
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CleaningServiceViewModel : ViewModel() {
    private val serviceRepository = ServiceRemote.getInstance()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _cleaningdata = MutableStateFlow<List<CleaningService?>>(emptyList())
    val cleaningdata: StateFlow<List<CleaningService?>> = _cleaningdata

    private val _durations = MutableStateFlow<List<CleaningDuration?>>(emptyList())
    val durations: StateFlow<List<CleaningDuration?>> = _durations

    private val _success_post = MutableStateFlow<Boolean>(false)
    val success_post: StateFlow<Boolean> = _success_post

    private val _new_job_cleaning = MutableStateFlow<NewJobCleaning?>(null)
    val new_job_cleaning: StateFlow<NewJobCleaning?> = _new_job_cleaning

    fun getServiceCleaning() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                Log.d("CleaningServiceViewModel", "Fetching cleaning services...")
                val response = serviceRepository.getServiceCleaning()
                Log.d("CleaningServiceViewModel", "Raw response: $response")

                // Log the raw response body as string
                val responseBody = response
                Log.d("CleaningServiceViewModel", "Response body: $responseBody")

                when(response) {
                    is NetworkResult.Success -> {
                        _durations.value = response.data.data!!.durations
                        _cleaningdata.value = response.data.data.services
                    }
                    is NetworkResult.Error -> {
                        _error.value = response.message.takeIf { it.isNotBlank() }
                            ?: "Failed to load cleaning services. Code: ${response.message}"
                    }

                }

            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                Log.e("CleaningServiceViewModel", errorMsg, e)
                _error.value = errorMsg
            } finally {
                _loading.value = false
                Log.d("CleaningServiceViewModel", "Loading completed")
            }
        }

    }

    fun postServiceCleaning(
        userID: String,
        startTime: String,
        price: Int,
        listDays: List<String>,
        duration: CleaningDuration,
        isCooking: Boolean,
        isIroning: Boolean,
        location: String
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _success_post.value = false
            try {
                Log.d("CleaningServiceViewModel", "Fetching cleaning services...")
                val response = serviceRepository.postJobCleaning(
                    userID = userID,
                    serviceType = "CLEANING",
                    startTime = startTime,
//                    workerQuantity = workerQuantity,
                    price = price,
                    listDays = listDays,
                    duration = duration,
                    isCooking = isCooking,
                    isIroning = isIroning,
                    location = location
//                    services = services
                )
                Log.d("CleaningServiceViewModel", "Raw response: $response")

                when(response) {
                    is NetworkResult.Success -> {
                        _success_post.value = true
                        _new_job_cleaning.value = response.data?.newJob
                    }
                    is NetworkResult.Error -> {
                        _error.value = response.message.takeIf { it.isNotBlank() }
                            ?: "Failed to load cleaning services. Code: ${response.message}"
                    }
                }

            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                Log.e("CleaningServiceViewModel", errorMsg, e)
                _error.value = errorMsg
            } finally {
                _loading.value = false
                Log.d("CleaningServiceViewModel", "Loading completed")
            }
        }
    }
}

