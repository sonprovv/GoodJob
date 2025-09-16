package com.project.job.ui.service.healthcareservice

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.ServiceRepository
import com.project.job.data.source.remote.api.request.ServiceInfoHealthcare
import com.project.job.data.source.remote.api.request.ShiftInfo
import com.project.job.data.source.remote.api.response.CleaningDuration
import com.project.job.data.source.remote.api.response.CleaningService
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.data.source.remote.api.response.HealthcareShift
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HealthCareViewModel: ViewModel() {
    private val serviceRepository = ServiceRepository()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _healthcareService = MutableStateFlow<List<HealthcareService?>>(emptyList())
    val healthcareService: StateFlow<List<HealthcareService?>> = _healthcareService

    private val _shift = MutableStateFlow<List<HealthcareShift?>>(emptyList())
    val shift: StateFlow<List<HealthcareShift?>> = _shift


    private val _success_post = MutableStateFlow<Boolean>(false)
    val success_post: StateFlow<Boolean> = _success_post

    fun getServiceHealthcare() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                Log.d("HealthCareViewModel", "Fetching cleaning services...")
                val response = serviceRepository.getServiceHealthcare()
                Log.d("HealthCareViewModel", "Raw response: $response")

                // Log the raw response body as string
                val responseBody = response.errorBody()?.string()
                Log.d("HealthCareViewModel", "Response body: $responseBody")

                if (response.isSuccessful) {
                    val serviceResponse = response.body()
                    Log.d("HealthCareViewModel", "Parsed response: $serviceResponse")

                    if (serviceResponse?.success == true) {
                        serviceResponse.data?.let { data ->
                            if (data.services.isNotEmpty() && data.services.isNotEmpty()) {
                                _error.value = null
                                _healthcareService.value = data.services
                                _shift.value = data.shifts
                                Log.d("HealthCareViewModel", "Data loaded successfully")
                                Log.d("HealthCareViewModel", "Shifts loaded: ${data.shifts.size} items")
                            } else {
                                val errorMsg = "No service data available"
                                Log.e("HealthCareViewModel", errorMsg)
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
                        Log.e("HealthCareViewModel", errorMsg)
                        _error.value = errorMsg
                    }
                } else {
                    val errorMsg = response.message().takeIf { it.isNotBlank() }
                        ?: "Failed to load cleaning services. Code: ${response.code()}"
                    Log.e("HealthCareViewModel", "API Error: $errorMsg")
                    _error.value = errorMsg
                }

            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                Log.e("HealthCareViewModel", errorMsg, e)
                _error.value = errorMsg
            } finally {
                _loading.value = false
                Log.d("HealthCareViewModel", "Loading completed")
            }
        }

    }

    fun postServiceHealthcare(
        token : String,
        userID: String,
        startTime: String,
        price: Int,
        workerQuantity: Int,
        listDays: List<String>,
        location: String,
        shift: ShiftInfo,
        services: List<ServiceInfoHealthcare>
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _success_post.value = false
            try {
                Log.d("HealthCareViewModel", "Posting healthcare service...")
                val response = serviceRepository.postJobHealthcare(
                    token = token,
                    userID = userID,
                    serviceType = "HEALTHCARE",
                    startTime = startTime,
                    price = price,
                    workerQuantity = workerQuantity,
                    listDays = listDays,
                    location = location,
                    shift = shift,
                    services = services
                )
                Log.d("HealthCareViewModel", "Raw response: $response")

                // Log the raw response body as string
                val responseBody = response.errorBody()?.string()
                Log.d("HealthCareViewModel", "Response body: $responseBody")

                if (response.isSuccessful) {
                    val postResponse = response.body()
                    Log.d("HealthCareViewModel", "Parsed response: $postResponse")

                    if (postResponse?.success == true) {
                        _success_post.value = true
                        Log.d("HealthCareViewModel", "Post successful")
                    } else {
                        val errorMsg =
                            postResponse?.message ?: "Failed to post healthcare service"
                        Log.e("HealthCareViewModel", errorMsg)
                        _error.value = errorMsg
                    }
                } else {
                    val errorMsg = response.message().takeIf { it.isNotBlank() }
                        ?: "Failed to post healthcare service. Code: ${response.code()}"
                    Log.e("HealthCareViewModel", "API Error: $errorMsg")
                    _error.value = errorMsg
                }

            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                Log.e("HealthCareViewModel", errorMsg, e)
                _error.value = errorMsg
            } finally {
                _loading.value = false
                Log.d("HealthCareViewModel", "Loading completed")
            }
        }

    }
}