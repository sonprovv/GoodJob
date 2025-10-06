package com.project.job.ui.service.healthcareservice.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.ServiceRemote
import com.project.job.data.source.remote.api.request.ServiceInfoHealthcare
import com.project.job.data.source.remote.api.request.ShiftInfo
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.data.source.remote.api.response.HealthcareShift
import com.project.job.data.source.remote.api.response.NewJobHealthcare
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HealthCareViewModel: ViewModel() {
    private val serviceRepository = ServiceRemote.getInstance()
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

    private val _new_job_healthcare = MutableStateFlow<NewJobHealthcare?>(null)
    val new_job_healthcare: StateFlow<NewJobHealthcare?> = _new_job_healthcare

    fun getServiceHealthcare() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                Log.d("HealthCareViewModel", "Fetching cleaning services...")
                val response = serviceRepository.getServiceHealthcare()
                Log.d("HealthCareViewModel", "Raw response: $response")

                when(response) {
                    is NetworkResult.Success -> {
                        val serviceResponse = response.data
                        Log.d("HealthCareViewModel", "Parsed response: $serviceResponse")
                        _healthcareService.value = serviceResponse.data.services
                        _shift.value = serviceResponse.data.shifts
                    }
                    is NetworkResult.Error -> {
                        val errorMsg = response.message
                        Log.e("HealthCareViewModel", errorMsg)
                    }
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

                when(response) {
                    is NetworkResult.Success -> {
                        val jobResponse = response.data
                        Log.d("HealthCareViewModel", "Parsed response: $jobResponse")
                        _success_post.value = true
                        _new_job_healthcare.value = jobResponse.newJob
                    }
                    is NetworkResult.Error -> {
                        val errorMsg = response.message
                        Log.e("HealthCareViewModel", errorMsg)
                    }
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