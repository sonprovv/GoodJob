package com.project.job.ui.activity.jobdetail.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.ServiceRemote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChoiceWorkerViewModel : ViewModel() {
    private val serviceRepository = ServiceRemote.getInstance()
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
                val response = serviceRepository.updateChoiceWorker(uid = uid, status = status)
                Log.d("ChoiceWorkerViewModel", "ChoiceWorker response: $response")
                Log.d("ChoiceWorkerViewModel", "ChoiceWorker response: $token, $uid, $status")

                when (response) {
                    is NetworkResult.Success -> {
                        val userResponse = response.data
                        if (userResponse.success) {
                            _error.value = null
                            Log.d("ChoiceWorkerViewModel", "ChoiceWorker successful: $userResponse")
                            _success.value = true
                        } else {
                            _error.value = userResponse.message
                        }
                    }

                    is NetworkResult.Error -> {
                        _error.value = response.message
                    }
                }
            } catch (e: Exception) {
                Log.e("ChoiceWorkerViewModel", "ChoiceWorker error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("ChoiceWorkerViewModel", "Activity finally")
                _loading.value = false
            }
        }
    }

    fun postReviewWorker(
        token: String,
        userID: String,
        rating: Int,
        comment: String,
        workerID: String,
        orderID: String,
        serviceType: String
    ) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = serviceRepository.postReviewWorker(
                    userID = userID,
                    rating = rating,
                    workerID = workerID,
                    orderID = orderID,
                    comment = comment,
                    serviceType = serviceType
                )
                Log.d("ChoiceWorkerViewModel", "postReviewWorker response: $response")
                Log.d(
                    "ChoiceWorkerViewModel",
                    "postReviewWorker response: $token, $userID, $rating, $comment"
                )

                when (response) {
                    is NetworkResult.Success -> {
                        val userResponse = response.data
                        if (userResponse.success) {
                            _error.value = null
                            Log.d(
                                "ChoiceWorkerViewModel",
                                "postReviewWorker successful: $userResponse"
                            )
                            _success.value = true
                        } else {
                            _error.value = userResponse.message
                        }
                    }

                    is NetworkResult.Error -> {
                        _error.value = response.message
                    }
                }
            } catch (e: Exception) {
                Log.e("ChoiceWorkerViewModel", "postReviewWorker error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("ChoiceWorkerViewModel", "postReviewWorker finally")
                _loading.value = false
            }
        }
    }
}