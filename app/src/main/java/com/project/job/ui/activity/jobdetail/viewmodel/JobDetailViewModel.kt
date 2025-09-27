package com.project.job.ui.activity.jobdetail.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.repository.ServiceRepository
import com.project.job.data.source.remote.api.response.ExtendedReview
import com.project.job.data.source.remote.api.response.WorkerOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class JobDetailViewModel: ViewModel() {
    private val serviceRepository = ServiceRepository()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _workers = MutableStateFlow<List<WorkerOrder>?>(null)
    val workers: StateFlow<List<WorkerOrder>?> = _workers

    private val _userReview = MutableStateFlow<List<ExtendedReview>?>(null)
    val userReview: StateFlow<List<ExtendedReview>?> = _userReview
    
    private val _serviceRatings = MutableStateFlow<Map<String, Double>>(emptyMap())
    val serviceRatings: StateFlow<Map<String, Double>> = _serviceRatings

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success

    fun getListWorker(jobID: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = serviceRepository.getWorkerOrderJob(jobID = jobID)
                Log.d("JobDetailViewModel", "JobDetail response: $response")
                Log.d("JobDetailViewModel", "JobDetail response: $jobID")

                when (response.isSuccess) {
                    true -> {
                        _workers.value = response.getOrNull()?.orders
                        _success.value = true
                    }
                    false -> {
                        _error.value = response.exceptionOrNull()?.message
                        _success.value = false
                    }
                }

            } catch (e: Exception) {
                Log.e("JobDetailViewModel", "JobDetail error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.e("ActivityViewModel", "Activity finally")
                _loading.value = false
            }
        }
    }

    fun getReviewWorker(workerID: String, serviceType: String = "CLEANING") {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val response = serviceRepository.getWorkerReviews(workerID = workerID)
                Log.d("JobDetailViewModel", "getReviewWorker response: $response")
                if (response.isSuccess) {
                    val userResponse = response.getOrNull()
                    if (userResponse != null && userResponse.success) {
                        val allReviews = mutableListOf<ExtendedReview>()
                        val ratingsMap = mutableMapOf<String, Double>()
                        
                        // Xử lý dữ liệu đánh giá theo cấu trúc mới
                        userResponse.experiences.forEach { (serviceTypeName, serviceExperience) ->
                            // Lưu rating từ ServiceExperience
                            ratingsMap[serviceTypeName] = serviceExperience.rating
                            
                            // Xử lý danh sách đánh giá
                            serviceExperience.reviews.forEach { review ->
                                allReviews.add(review.withServiceType(serviceTypeName))
                            }
                        }
                        
                        // Cập nhật ratings
                        _serviceRatings.value = ratingsMap
                        
                        // Lọc theo loại dịch vụ nếu cần
                        val filteredReviews = if (serviceType.uppercase() != "ALL") {
                            allReviews.filter { it.serviceType == serviceType.uppercase() }
                        } else {
                            allReviews
                        }
                        
                        // Log số lượng đánh giá sau khi lọc
                        Log.d("JobDetailViewModel", "Filtered reviews count: ${filteredReviews.size} for service type: $serviceType")
                        
                        // Nếu không có đánh giá cho loại dịch vụ được chọn, hiển thị tất cả đánh giá
                        _userReview.value = if (filteredReviews.isEmpty() && serviceType.uppercase() != "ALL") {
                            Log.d("JobDetailViewModel", "No reviews for $serviceType, showing all reviews")
                            allReviews
                        } else {
                            filteredReviews
                        }
                        _error.value = null
                        Log.d("JobDetailViewModel", "getReviewWorker successful for $serviceType: $userResponse")
                    } else {
                        _error.value = userResponse?.message ?: "getReviewWorker failed"
                    }
                } else {
                    _error.value = response.exceptionOrNull()?.message ?: "getReviewWorker failed"
                }

            } catch (e: Exception) {
                Log.e("JobDetailViewModel", "getReviewWorker error: ${e.message}")
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}