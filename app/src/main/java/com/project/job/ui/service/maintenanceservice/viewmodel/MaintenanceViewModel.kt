package com.project.job.ui.service.maintenanceservice.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.ServiceRemote
import com.project.job.data.source.remote.api.request.ServicePowerInfo
import com.project.job.data.source.remote.api.response.MaintenanceData
import com.project.job.data.source.remote.api.response.NewJobMaintenance
import com.project.job.data.source.remote.api.response.PowersInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MaintenanceViewModel : ViewModel() {
    private val serviceRepository = ServiceRemote.getInstance()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _maintenanceService = MutableStateFlow<List<MaintenanceData?>>(emptyList())
    val maintenanceService: StateFlow<List<MaintenanceData?>> = _maintenanceService

    private val _powerService = MutableStateFlow<List<PowersInfo>>(emptyList())
    val powerService: StateFlow<List<PowersInfo>> = _powerService

    private val _new_job_maintenance = MutableStateFlow<NewJobMaintenance?>(null)
    val new_job_maintenance : StateFlow<NewJobMaintenance?> = _new_job_maintenance


    private val _success_post = MutableStateFlow<Boolean>(false)
    val success_post: StateFlow<Boolean> = _success_post

    fun getMaintenanceService() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                Log.d("MaintenanceViewModel", "Fetching cleaning services...")
                val response = serviceRepository.getServiceMaintenance()
                Log.d("MaintenanceViewModel", "Raw response: $response")

                when(response) {
                    is NetworkResult.Success -> {
                        val serviceResponse = response.data
                        Log.d("MaintenanceViewModel", "Parsed response: $serviceResponse")
                        _maintenanceService.value = serviceResponse.data
                        _powerService.value = serviceResponse.data.flatMap { it.powers }
                    }
                    is NetworkResult.Error -> {
                        val errorMsg = response.message
                        Log.e("MaintenanceViewModel", errorMsg)
                    }
                }

            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                Log.e("MaintenanceViewModel", errorMsg, e)
                _error.value = errorMsg
            } finally {
                _loading.value = false
                Log.d("MaintenanceViewModel", "Loading completed")
            }
        }
    }

    fun postServiceMaintenance(
        userID: String,
        startTime: String,
        price: Int,
        listDays: List<String>,
        location: String,
        services: List<ServicePowerInfo>
    ) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _success_post.value = false
            try {
                Log.d("MaintenanceViewModel", "Posting maintenance service...")
                val response = serviceRepository.postJobMaintenance(
                    userID = userID,
                    serviceType = "MAINTENANCE",
                    startTime = startTime,
                    price = price,
                    listDays = listDays,
                    location = location,
                    services = services
                )
                Log.d("MaintenanceViewModel", "Raw response: $response")

                when (response) {
                    is NetworkResult.Success -> {
                        _success_post.value = true
                        _new_job_maintenance.value = response.data.newJob
                        Log.d("MaintenanceViewModel", "Maintenance service posted successfully")
                    }

                    is NetworkResult.Error -> {
                        val errorMsg = response.message
                        Log.e("MaintenanceViewModel", errorMsg)
                        _error.value = errorMsg
                    }
                }
            }
            finally {
                _loading.value = false
                Log.d("MaintenanceViewModel", "Loading completed")
            }
        }

    }
}