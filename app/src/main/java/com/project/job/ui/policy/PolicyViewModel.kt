package com.project.job.ui.policy

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.PolicyRemote
import com.project.job.data.source.remote.api.response.PolicyData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PolicyViewModel : ViewModel() {
    private val policyRepository = PolicyRemote.getInstance()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success

    private val _markdown = MutableStateFlow<String?>(null)
    val markdown: StateFlow<String?> = _markdown

    private val _html = MutableStateFlow<String?>(null)
    val html: StateFlow<String?> = _html

    private val _policyData = MutableStateFlow<PolicyData?>(null)
    val policyData: StateFlow<PolicyData?> = _policyData


    fun getPrivacyPolicy() {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            try {
                val response = policyRepository.getPrivacyPolicy()
                Log.d("PolicyViewModel", "PolicyView response: $response")

                when(response) {
                    is NetworkResult.Success -> {
                        val data = response.data
                        _policyData.value = data.data
                        _markdown.value = data.data.markdownContent
                        _html.value = data.data.htmlContent
                        _success.value = true
                    }

                    is NetworkResult.Error -> {
                        _error.value = response.message
                        _success.value = false
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An unexpected error occurred"
            } finally {
                _loading.value = false
            }
        }
    }
}