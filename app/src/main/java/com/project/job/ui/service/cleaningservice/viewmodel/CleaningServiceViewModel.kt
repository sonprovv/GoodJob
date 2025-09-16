package com.project.job.ui.service.cleaningservice.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.ServiceRepository
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CleaningService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CleaningServiceViewModel : ViewModel() {
    private val serviceRepository = ServiceRepository()
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

    fun getServiceCleaning() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                Log.d("CleaningServiceViewModel", "Fetching cleaning services...")
                val response = serviceRepository.getServiceCleaning()
                Log.d("CleaningServiceViewModel", "Raw response: $response")

                // Log the raw response body as string
                val responseBody = response.errorBody()?.string()
                Log.d("CleaningServiceViewModel", "Response body: $responseBody")

                if (response.isSuccessful) {
                    val serviceResponse = response.body()
                    Log.d("CleaningServiceViewModel", "Parsed response: $serviceResponse")

                    if (serviceResponse?.success == true) {
                        serviceResponse.data?.let { data ->
                            if (data.services.isNotEmpty() && data.durations.isNotEmpty()) {
                                _error.value = null
                                _cleaningdata.value = data.services
                                _durations.value = data.durations
                                Log.d("CleaningServiceViewModel", "Data loaded successfully")
                            } else {
                                val errorMsg = "No service data available"
                                Log.e("CleaningServiceViewModel", errorMsg)
                                _error.value = errorMsg
                            }
                        } ?: run {
                            val errorMsg = "No data received from server"
                            Log.e("CleaningServiceViewModel", errorMsg)
                            _error.value = errorMsg
                        }
                    } else {
                        val errorMsg =
                            serviceResponse?.message ?: "Failed to load cleaning services"
                        Log.e("CleaningServiceViewModel", errorMsg)
                        _error.value = errorMsg
                    }
                } else {
                    val errorMsg = response.message().takeIf { it.isNotBlank() }
                        ?: "Failed to load cleaning services. Code: ${response.code()}"
                    Log.e("CleaningServiceViewModel", "API Error: $errorMsg")
                    _error.value = errorMsg
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
        token: String,
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
                    token = token,
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

                if (response.isSuccessful) {
                    _success_post.value = true
                } else {
                    val errorMsg = response.message().takeIf { it.isNotBlank() }
                        ?: "Failed to load cleaning services. Code: ${response.code()}"
                    Log.e("CleaningServiceViewModel", "API Error: $errorMsg")
                    _error.value = errorMsg
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