package com.project.job.ui.chatbot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.project.job.data.source.remote.ChatBotRemote
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.ExperienceData
import com.project.job.data.source.remote.api.request.LocationData
import com.project.job.data.source.remote.api.request.ReferenceData
import com.project.job.data.source.remote.api.response.QueryJobs
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

    fun chatBot(request: String, locationData: LocationData) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null
            _response_text.value = null
            _response_answer.value = null
            _response_jobs.value = null
            _response_type.value = null
            
            try {
                val preferenceData = ReferenceData(location = locationData, experiences = ExperienceData())

                val response = chatBotRepository.chatBot(request = request, refrence = preferenceData)
                Log.d("ChatBotViewModel", "ChatBot response: $response")
                
                when(response) {
                    is NetworkResult.Success -> {
                        val responseData = response.data
                        val dataElement: JsonElement = responseData.data
                        val responseType = responseData.type
                        
                        _response_type.value = responseType
                        Log.d("ChatBotViewModel", "Response type: $responseType")
                        
                        when (responseType) {
                            "Job" -> {
                                // Lấy jobs array và answer text từ trong data object
                                val dataObject = dataElement.asJsonObject

                                // Lấy answer text
                                if (dataObject.has("answer")) {
                                    val answerText = dataObject.get("answer").asString
                                    _response_answer.value = answerText
                                    Log.d("ChatBotViewModel", "Answer: $answerText")
                                }

                                // Lấy jobs array
                                if (dataObject.has("jobs")) {
                                    val jobsElement = dataObject.get("jobs")

                                    if (jobsElement.isJsonArray) {
                                        val jobListType = object : TypeToken<List<QueryJobs>>() {}.type
                                        val jobList = gson.fromJson<List<QueryJobs>>(jobsElement, jobListType)
                                        _response_jobs.value = jobList
                                        Log.d("ChatBotViewModel", "Job list size: ${jobList.size}")
                                    } else {
                                        _error.value = "Dữ liệu jobs không phải array"
                                    }
                                } else {
                                    _error.value = "Không tìm thấy trường 'jobs' trong response"
                                }
                            }
                            "Info" -> {
                                // Xử lý response dạng Info Text
                                if (dataElement.isJsonPrimitive) {
                                    _response_text.value = dataElement.asString
                                    Log.d("ChatBotViewModel", "Info text: ${dataElement.asString}")
                                } else {
                                    _error.value = "Dữ liệu info không đúng định dạng"
                                }
                            }
                            "Policy" -> {
                                // Xử lý response dạng Policy Text
                                if (dataElement.isJsonPrimitive) {
                                    _response_text.value = dataElement.asString
                                    Log.d("ChatBotViewModel", "Policy text: ${dataElement.asString}")
                                } else {
                                    _error.value = "Dữ liệu policy không đúng định dạng"
                                }
                            }
                            "General" -> {
                                // Xử lý response dạng General Text
                                if (dataElement.isJsonPrimitive) {
                                    _response_text.value = dataElement.asString
                                    Log.d("ChatBotViewModel", "General text: ${dataElement.asString}")
                                } else {
                                    _error.value = "Dữ liệu general không đúng định dạng"
                                }
                            }
                            else -> {
                                _error.value = "Không xác định loại phản hồi: $responseType"
                            }
                        }
                        
                        _success.value = true
                    }
                    is NetworkResult.Error -> {
                        _error.value = response.message
                        _success.value = false
                    }
                }

            } catch (e: Exception) {
                Log.e("ChatBotViewModel", "ChatBot error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.d("ChatBotViewModel", "ChatBot finally")
                _loading.value = false
            }
        }
    }
}