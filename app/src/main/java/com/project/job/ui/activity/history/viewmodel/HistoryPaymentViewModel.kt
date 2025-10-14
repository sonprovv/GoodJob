package com.project.job.ui.activity.history.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.ServiceRepository
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.PaymentRemote
import com.project.job.data.source.remote.api.response.DataJobs
import com.project.job.data.source.remote.api.response.PaymentData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryPaymentViewModel : ViewModel() {
    private val historyPaymentRepository = PaymentRemote.getInstance()
    private val serviceRepository = ServiceRepository()
    
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _history_payment = MutableStateFlow<List<PaymentData>?>(null)
    val jobs: StateFlow<List<PaymentData>?> = _history_payment

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success
    
    // StateFlow for job locations map (jobID -> location)
    private val _job_locations = MutableStateFlow<Map<String, String>>(emptyMap())
    val jobLocations: StateFlow<Map<String, String>> = _job_locations

    fun getHistoryPayment() {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = historyPaymentRepository.getHistoryPayment()
                Log.d("HistoryPaymentViewModel", "HistoryPayment response: $response")
                when(response) {
                    is NetworkResult.Success -> {
                        _history_payment.value = response.data.payments
                        _success.value = true
                        
                        // Fetch job locations for the payment jobIDs
                        fetchJobLocations(response.data.payments)
                    }
                    is NetworkResult.Error -> {
                        _error.value = response.message
                        _success.value = false
                        // Set loading to false if payment fetch fails
                        _loading.value = false
                    }

                }

            } catch (e: Exception) {
                Log.e("HistoryPaymentViewModel", "HistoryPayment error: ${e.message}")
                _error.value = e.message
                // Set loading to false if exception occurs
                _loading.value = false
            } finally {
                Log.e("HistoryPaymentViewModel", "HistoryPayment finally")
                // Don't set loading to false here if we're about to fetch job locations
                // fetchJobLocations will handle the loading state
            }
        }
    }
    
    private fun fetchJobLocations(payments: List<PaymentData>) {
        viewModelScope.launch {
            try {
                // Keep loading state active while fetching job locations
                _loading.value = true
                Log.d("HistoryPaymentViewModel", "Starting fetchJobLocations - Loading: true")
                
                // Get user ID from the first payment (assuming all payments are from same user)
                val userID = payments.firstOrNull()?.userID
                if (userID != null) {
                    Log.d("HistoryPaymentViewModel", "Fetching job locations for user: $userID")
                    
                    val jobsResponse = serviceRepository.getUserPostJobs(userID)
                    if (jobsResponse.isSuccess) {
                        val userJobs = jobsResponse.getOrNull()?.jobs ?: emptyList()
                        Log.d("HistoryPaymentViewModel", "Found ${userJobs.size} user jobs")
                        
                        // Create map: jobID (DataJobs.uid) -> location
                        val locationMap = userJobs.associate { job ->
                            job.uid to job.location // job.uid is the jobID
                        }
                        
                        Log.d("HistoryPaymentViewModel", "Job locations map: $locationMap")
                        _job_locations.value = locationMap
                    } else {
                        Log.e("HistoryPaymentViewModel", "Failed to fetch user jobs: ${jobsResponse.exceptionOrNull()?.message}")
                    }
                } else {
                    Log.w("HistoryPaymentViewModel", "No userID found in payments to fetch job locations")
                }
            } catch (e: Exception) {
                Log.e("HistoryPaymentViewModel", "Error fetching job locations: ${e.message}")
            } finally {
                // Hide loading after job locations are fetched
                _loading.value = false
                Log.d("HistoryPaymentViewModel", "Finished fetchJobLocations - Loading: false")
            }
        }
    }
}