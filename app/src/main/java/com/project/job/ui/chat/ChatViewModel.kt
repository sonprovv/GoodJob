package com.project.job.ui.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.project.job.data.source.remote.ChatRemote
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.request.ReferenceData
import com.project.job.data.source.remote.api.response.QueryJobs
import com.project.job.data.source.remote.api.response.chat.ConversationData
import com.project.job.data.source.remote.api.response.chat.SenderData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {
    private val chatRepository : ChatRemote = ChatRemote.getInstance()

    private val gson = Gson()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<Boolean?>(null)
    val success_change: StateFlow<Boolean?> = _success

    private val _message_text = MutableStateFlow<String?>(null)
    val response_text: StateFlow<String?> = _message_text

    private val _user_info = MutableStateFlow<SenderData?>(null)
    val user_info: StateFlow<SenderData?> = _user_info

    private val _conversations = MutableStateFlow<List<ConversationData>?>(emptyList())
    val conversations: StateFlow<List<ConversationData>?> = _conversations

    fun getConversations() {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null

            try {
                val response = chatRepository.getConversations()
                Log.d("ChatViewModel", "Chat response: $response")

                when(response) {
                    is NetworkResult.Success -> {
                        Log.d("ChatViewModel", "Success! Conversations count: ${response.data.conversations.size}")
                        Log.d("ChatViewModel", "Conversations data: ${response.data.conversations}")
                        _conversations.value = response.data.conversations
                        _success.value = true
                        Log.d("ChatViewModel", "StateFlow updated with ${_conversations.value?.size} items")
                    }
                    is NetworkResult.Error -> {
                        Log.e("ChatViewModel", "Error: ${response.message}")
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
}