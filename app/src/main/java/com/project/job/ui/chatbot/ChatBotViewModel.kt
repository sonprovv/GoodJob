package com.project.job.ui.chatbot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.project.job.data.source.remote.ChatBotRemote
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.LocationData
import com.project.job.data.source.remote.api.request.ReferenceData
import com.project.job.data.source.remote.api.response.QueryJobs
import com.project.job.utils.ErrorHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatBotViewModel : ViewModel() {
    private val chatBotRepository = ChatBotRemote.getInstance()

    private val gson = Gson()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success

    private val _response_text = MutableStateFlow<String?>(null)
    val response_text: StateFlow<String?> = _response_text

    private val _response_answer = MutableStateFlow<String?>(null)
    val response_answer: StateFlow<String?> = _response_answer

    private val _response_jobs = MutableStateFlow<List<QueryJobs>?>(null)
    val response_jobs: StateFlow<List<QueryJobs>?> = _response_jobs

    private val _response_type = MutableStateFlow<String?>(null)
    val response_type: StateFlow<String?> = _response_type

    fun chatBot(request: String, locationData: LocationData, session_id: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            _response_text.value = null
            _response_answer.value = null
            _response_jobs.value = null
            _response_type.value = null
            
            try {
                val preferenceData = ReferenceData(location = locationData)

                val response = chatBotRepository.chatBot(request = request, reference = preferenceData)
                Log.d("ChatBotViewModel", "ChatBot response: $response")
                
                when(response) {
                    is NetworkResult.Success -> {
                        val responseData = response.data
                        
                        // New API format (v2.0) - uses intent instead of type
                        val responseType = responseData.intent ?: responseData.type
                        val dataElement: JsonElement? = responseData.data
                        
                        // Check if this is legacy format with success field
                        if (responseData.success == false) {
                            val rawErrorMessage = if (dataElement?.isJsonPrimitive == true) {
                                dataElement.asString
                            } else {
                                "Có lỗi xảy ra"
                            }
                            _error.value = ErrorHandler.handleChatBotError(rawErrorMessage)
                            _success.value = false
                            Log.d("ChatBotViewModel", "Error response: $rawErrorMessage")
                            return@launch
                        }
                        
                        // Check if this is access denied error (new format)
                        if (responseData.metadata?.error == "access_denied") {
                            val errorMessage = responseData.context ?: "Bạn không có quyền truy cập"
                            _response_text.value = errorMessage  // Show as info message instead of error
                            _success.value = true  // Mark as success to show the message
                            Log.d("ChatBotViewModel", "Access denied: $errorMessage")
                            return@launch
                        }
                        
                        _response_type.value = responseType
                        Log.d("ChatBotViewModel", "Response type: $responseType")
                        
                        when (responseType) {
                            "job", "Job" -> {
                                // New API format: jobs are directly in response
                                if (responseData.jobs != null) {
                                    _response_answer.value = responseData.context
                                    _response_jobs.value = responseData.jobs
                                    Log.d("ChatBotViewModel", "New format - Answer: ${responseData.context}")
                                    Log.d("ChatBotViewModel", "New format - Job list size: ${responseData.jobs.size}")
                                }
                                // Legacy format: jobs in data object
                                else if (dataElement?.isJsonObject == true) {
                                    val dataObject = dataElement.asJsonObject

                                    // Lấy answer text
                                    if (dataObject.has("answer")) {
                                        val answerText = dataObject.get("answer").asString
                                        _response_answer.value = answerText
                                        Log.d("ChatBotViewModel", "Legacy format - Answer: $answerText")
                                    }

                                    // Lấy jobs array
                                    if (dataObject.has("jobs")) {
                                        val jobsElement = dataObject.get("jobs")

                                        if (jobsElement.isJsonArray) {
                                            val jobListType = object : TypeToken<List<QueryJobs>>() {}.type
                                            val jobList = gson.fromJson<List<QueryJobs>>(jobsElement, jobListType)
                                            _response_jobs.value = jobList
                                            Log.d("ChatBotViewModel", "Legacy format - Job list size: ${jobList.size}")
                                        }
                                    }
                                } else if (dataElement?.isJsonPrimitive == true) {
                                    // Data is string (error message)
                                    _error.value = dataElement.asString
                                    _success.value = false
                                    return@launch
                                }
                            }
                            "info", "Info" -> {
                                // New API format: context directly in response
                                if (responseData.context != null) {
                                    _response_text.value = responseData.context
                                    Log.d("ChatBotViewModel", "New format - Info text: ${responseData.context}")
                                }
                                // Legacy format: data is string
                                else if (dataElement?.isJsonPrimitive == true) {
                                    _response_text.value = dataElement.asString
                                    Log.d("ChatBotViewModel", "Legacy format - Info text: ${dataElement.asString}")
                                }
                            }
                            "policy", "Policy" -> {
                                // New API format: context directly in response
                                if (responseData.context != null) {
                                    _response_text.value = responseData.context
                                    Log.d("ChatBotViewModel", "New format - Policy text: ${responseData.context}")
                                }
                                // Legacy format: data is string
                                else if (dataElement?.isJsonPrimitive == true) {
                                    _response_text.value = dataElement.asString
                                    Log.d("ChatBotViewModel", "Legacy format - Policy text: ${dataElement.asString}")
                                }
                            }
                            "general", "General" -> {
                                // New API format: context directly in response
                                if (responseData.context != null) {
                                    _response_text.value = responseData.context
                                    Log.d("ChatBotViewModel", "New format - General text: ${responseData.context}")
                                }
                                // Legacy format: data is string
                                else if (dataElement?.isJsonPrimitive == true) {
                                    _response_text.value = dataElement.asString
                                    Log.d("ChatBotViewModel", "Legacy format - General text: ${dataElement.asString}")
                                }
                            }
                            else -> {
                                _error.value = "Không xác định loại phản hồi: $responseType"
                                _success.value = false
                                return@launch
                            }
                        }
                        
                        _success.value = true
                    }
                    is NetworkResult.Error -> {
                        _error.value = ErrorHandler.handleHttpError(response.message)
                        _success.value = false
                    }
                }

            } catch (e: Exception) {
                Log.e("ChatBotViewModel", "ChatBot error: ${e.message}")
                _error.value = ErrorHandler.handleException(e)
            } finally {
                Log.d("ChatBotViewModel", "ChatBot finally")
                _loading.value = false
            }
        }
    }
}