package com.project.job.ui.activity.jobdetail.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChoiceWorkerViewModel: ViewModel() {
    private val serviceRepository = ServiceRepository()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success

    fun choiceWorker(token: String, uid: String, status: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = serviceRepository.updateChoiceWorker(uid=uid, status=status)
                Log.d("ChoideWorkerViewModel", "ChoideWorker response: $response")
                Log.d("ChoideWorkerViewModel", "ChoideWorker response: $token, $uid, $status")

                if (response.isSuccess) {
                    val userResponse = response.getOrNull()
                    if (userResponse != null && userResponse.success) {
                        _error.value = null
                        Log.d("ChoideWorkerViewModel", "ChoideWorker successful: $userResponse")
                        _success.value = true
                    } else {
                        _error.value = userResponse?.message ?: "ChoideWorker failed"
                    }
                } else {
                    _error.value = response.getOrNull()?.message ?: "ChoideWorker failed"
                }

            } catch (e: Exception) {
                Log.e("ChoideWorkerViewModel", "JobDetail error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("ChoideWorkerViewModel", "Activity finally")
                _loading.value = false
            }
        }
    }

    fun postReviewWorker(token: String, userID: String, rating: Int, comment: String, workerID: String, orderID: String, serviceType: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = serviceRepository.postReviewWorker(userID=userID, rating=rating, workerID = workerID, orderID = orderID, comment=comment, serviceType = serviceType)
                Log.d("ChoideWorkerViewModel", "postReviewWorker response: $response")
                Log.d("ChoideWorkerViewModel", "postReviewWorker response: $token, $userID, $rating, $comment")

                if (response.isSuccess) {
                    val userResponse = response.getOrNull()
                    if (userResponse != null && userResponse.success) {
                        _error.value = null
                        Log.d("ChoideWorkerViewModel", "postReviewWorker successful: $userResponse")
                        _success.value = true
                    } else {
                        _error.value = userResponse?.message ?: "postReviewWorker failed"
                    }
                } else {
                    _error.value = response.getOrNull()?.message ?: "postReviewWorker failed"
                }

            } catch (e: Exception) {
                Log.e("ChoideWorkerViewModel", "postReviewWorker error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("ChoideWorkerViewModel", "Activity finally")
                _loading.value = false
            }
        }
    }
}