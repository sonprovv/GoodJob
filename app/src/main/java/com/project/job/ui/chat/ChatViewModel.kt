package com.project.job.ui.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.project.job.data.mapper.ChatMapper
import com.project.job.data.repository.ConversationRepository
import com.project.job.data.repository.implement.ConversationRepositoryImpl
import com.project.job.data.source.local.room.entity.ChatEntity
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.api.response.chat.ConversationData
import com.project.job.data.source.remote.api.response.chat.SenderData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val conversationRepository: ConversationRepository = ConversationRepositoryImpl.getInstance(application)

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

    // Local conversations from Room database - auto-update UI when data changes
    private val _localConversations = MutableStateFlow<List<ChatEntity>>(emptyList())
    val localConversations: StateFlow<List<ChatEntity>> = _localConversations

    // For backward compatibility with UI using ConversationData
    private val _conversations = MutableStateFlow<List<ConversationData>?>(emptyList())
    val conversations: StateFlow<List<ConversationData>?> = _conversations

    /**
     * Start observing local conversations from Room database
     * This will automatically update UI when data changes
     */
    fun observeLocalConversations() {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Starting to observe local conversations")
                conversationRepository.getAllConversationsLocal().collect { conversations ->
                    Log.d("ChatViewModel", "Local conversations updated: ${conversations.size} conversations")
                    _localConversations.value = conversations
                    
                    // Also update ConversationData flow for backward compatibility
                    _conversations.value = ChatMapper.toConversationDataList(conversations)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error observing local conversations: ${e.message}")
            }
        }
    }

    /**
     * Fetch conversations from API and save to Room database
     * This will trigger UI update automatically via Flow
     */
    fun refreshConversations() {
        viewModelScope.launch {
            _success.value = false
            _loading.value = true
            _error.value = null

            try {
                Log.d("ChatViewModel", "Fetching conversations from API")
                val result = conversationRepository.fetchAndSaveConversations()

                when(result) {
                    is NetworkResult.Success -> {
                        Log.d("ChatViewModel", "Conversations refreshed successfully")
                        _success.value = true
                    }
                    is NetworkResult.Error -> {
                        Log.e("ChatViewModel", "Error refreshing conversations: ${result.message}")
                        _error.value = result.message
                        _success.value = false
                    }
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Exception refreshing conversations: ${e.message}")
                _error.value = e.message
            } finally {
                Log.d("ChatViewModel", "Refresh conversations finally")
                _loading.value = false
            }
        }
    }

    /**
     * Legacy method for backward compatibility
     * Internally calls observeLocalConversations() and refreshConversations()
     */
    fun getConversations() {
        // Switched to Firebase Realtime Database for conversations
        observeRealtimeConversations()
    }

    /**
     * Mark conversation as read
     */
    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Marking conversation as read: $conversationId")
                conversationRepository.markConversationAsRead(conversationId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error marking conversation as read: ${e.message}")
            }
        }
    }

    /**
     * Clear all local conversations (useful for logout)
     */
    fun clearLocalConversations() {
        viewModelScope.launch {
            try {
                Log.d("ChatViewModel", "Clearing local conversations")
                conversationRepository.clearLocalConversations()
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error clearing local conversations: ${e.message}")
            }
        }
    }

    // ===================== Firebase Realtime Database (new) =====================
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseDb: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    // Store the last known messages to avoid unnecessary updates
    private val lastKnownMessages = mutableMapOf<String, Pair<String, Long>>()

    fun observeRealtimeConversations() {
        val currentUid = firebaseAuth.currentUser?.uid
        if (currentUid.isNullOrEmpty()) {
            _error.value = "Bạn chưa đăng nhập"
            return
        }
        _loading.value = true
        
        val roomsRef = firebaseDb.getReference("rooms")
        roomsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val list = mutableListOf<ConversationData>()
                    val currentTime = System.currentTimeMillis()
                    
                    for (roomSnap in snapshot.children) {
                        val roomId = roomSnap.key ?: continue
                        val ids = roomId.split("_")
                        if (ids.size != 2) continue
                        if (!ids.contains(currentUid)) continue

                        val partnerId = if (ids[0] == currentUid) ids[1] else ids[0]
                        val lastMessageText = roomSnap.child("lastMessage").getValue(String::class.java) ?: continue
                        val lastTimestamp = roomSnap.child("lastTimestamp").getValue(Long::class.java) ?: currentTime
                        
                        // Check if this is a new message or an update
                        val lastKnown = lastKnownMessages[roomId]
                        if (lastKnown != null && 
                            lastKnown.first == lastMessageText && 
                            lastKnown.second == lastTimestamp) {
                            // Skip update if message and timestamp are the same
                            continue
                        }
                        
                        // Update last known message
                        lastKnownMessages[roomId] = Pair(lastMessageText, lastTimestamp)

                        // users/{uid}
                        val usersNode = roomSnap.child("users")
                        val partnerNode = usersNode.child(partnerId)
                        val username = partnerNode.child("username").getValue(String::class.java) ?: "User"
                        val avatar = partnerNode.child("avatar").getValue(String::class.java) ?: ""

                        val senderData = SenderData(
                            id = partnerId,
                            username = username,
                            name = username,
                            avatar = avatar,
                            dob = "",
                            tel = "",
                            email = "",
                            location = "",
                            gender = "",
                            userType = ""
                        )

                        val conversationData = ConversationData(
                            id = roomId,
                            lastMessageTime = lastTimestamp,
                            lastMessage = lastMessageText,
                            unreadCount = 0,
                            updatedAt = lastTimestamp.toString(),
                            senderId = partnerId,
                            sender = senderData
                        )
                        list.add(conversationData)
                    }
                    val sorted = list.sortedByDescending { it.lastMessageTime }
                    _conversations.value = sorted
                    _loading.value = false
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error parsing rooms: ${e.message}")
                    _error.value = e.message
                    _loading.value = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _error.value = error.message
                _loading.value = false
            }
        })
    }
}