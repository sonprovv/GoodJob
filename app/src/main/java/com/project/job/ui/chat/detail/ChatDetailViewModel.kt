package com.project.job.ui.chat.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.project.job.data.source.remote.ChatRemote
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.chat.SendMessageRequest
import com.project.job.data.source.remote.api.response.chat.ConversationData
import com.project.job.data.source.remote.api.response.chat.MessageData
import com.project.job.data.source.remote.api.response.chat.SenderData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatDetailViewModel : ViewModel() {
    private val chatRepository: ChatRemote = ChatRemote.getInstance()

    private val gson = Gson()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success

    private val _message_text = MutableStateFlow<String?>(null)
    val message_text: StateFlow<String?> = _message_text

    private val _user_info = MutableStateFlow<SenderData?>(null)
    val user_info: StateFlow<SenderData?> = _user_info

    private val _messages = MutableStateFlow<List<MessageData>?>(emptyList())
    val messages: StateFlow<List<MessageData>?> = _messages

    fun sendMessage(receiverId: String, message: String) {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null

            try {
                val response = chatRepository.sendMessage(
                    SendMessageRequest(receiverId = receiverId, message = message)
                )
                Log.d("ChatViewModel", "Chat response: $response")

                when (response) {
                    is NetworkResult.Success -> {
                        _message_text.value = response.data.message.message
                        _success.value = true
                    }

                    is NetworkResult.Error -> {
                        _error.value = response.message
                        _success.value = false
                    }
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Chat error: ${e.message}")
                _error.value = e.message
            } finally {
                Log.d("ChatViewModel", "Chat finally")
                _loading.value = false
            }
        }
    }

    fun getMessages(receiverId: String) {
        viewModelScope.launch {
            // Don't reset _success here to avoid triggering infinite loop
            _loading.value = true
            _error.value = null

            try {
                val response = chatRepository.getMessages(userId = receiverId)
                Log.d("ChatDetailViewModel", "GetMessages response: $response")

                when (response) {
                    is NetworkResult.Success -> {
                        Log.d("ChatDetailViewModel", "Messages loaded: ${response.data.messages.size} items")
                        _messages.value = response.data.messages
                        // DON'T set _success here - only update messages state
                    }

                    is NetworkResult.Error -> {
                        Log.e("ChatDetailViewModel", "GetMessages error: ${response.message}")
                        _error.value = response.message
                    }
                }

            } catch (e: Exception) {
                Log.e("ChatDetailViewModel", "GetMessages exception: ${e.message}")
                _error.value = e.message
            } finally {
                Log.d("ChatDetailViewModel", "GetMessages finally")
                _loading.value = false
            }
        }
    }

}