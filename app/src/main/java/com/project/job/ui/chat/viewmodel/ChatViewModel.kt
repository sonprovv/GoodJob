package com.project.job.ui.chat.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.job.data.repository.ConversationRepository
import com.project.job.data.repository.implement.ConversationRepositoryImpl
import com.project.job.data.source.local.room.entity.ChatEntity
import com.project.job.data.source.remote.api.response.chat.ConversationData
import com.project.job.data.source.remote.api.response.chat.SenderData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val conversationRepository: ConversationRepository = ConversationRepositoryImpl.getInstance(application)

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
     * Legacy method for backward compatibility
     * Internally calls observeLocalConversations() and refreshConversations()
     */
    fun getConversations() {
        // Switched to Firebase Realtime Database for conversations
        observeRealtimeConversations()
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
    
    // Store listener reference to remove it later
    private var conversationsListener: ValueEventListener? = null

    private var authListener: FirebaseAuth.AuthStateListener? = null

    fun observeRealtimeConversations() {
        _loading.value = true
        
        // Remove old listeners if exists
        removeConversationsListener()
        authListener?.let { firebaseAuth.removeAuthStateListener(it) }
        
        authListener = FirebaseAuth.AuthStateListener { auth ->
            val currentUid = auth.currentUser?.uid
            Log.d("ChatViewModel", "AuthStateChanged: currentUid=$currentUid")
            
            if (!currentUid.isNullOrEmpty()) {
                // User is logged in, attach database listener
                attachDatabaseListener(currentUid)
            } else {
                Log.d("ChatViewModel", "User is null in AuthStateListener")
                _loading.value = false
                _error.value = "Bạn chưa đăng nhập"
                _conversations.value = emptyList()
            }
        }
        
        firebaseAuth.addAuthStateListener(authListener!!)
    }

    private fun attachDatabaseListener(currentUid: String) {
        val roomsRef = firebaseDb.getReference("rooms")
        
        // Remove old listener if exists to avoid duplicates
        conversationsListener?.let { roomsRef.removeEventListener(it) }
        
        conversationsListener = object : ValueEventListener {
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
        }
        
        roomsRef.addValueEventListener(conversationsListener!!)
    }
    
    /**
     * Remove Firebase listener and clear conversations
     * Call this when user logs out
     */
    fun removeConversationsListener() {
        conversationsListener?.let { listener ->
            try {
                val roomsRef = firebaseDb.getReference("rooms")
                roomsRef.removeEventListener(listener)
                Log.d("ChatViewModel", "Removed conversations listener")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error removing listener: ${e.message}")
            }
        }
        conversationsListener = null
        lastKnownMessages.clear()
    }
    
    /**
     * Clear all chat data (for logout)
     * Removes Firebase listener and clears local data
     */
    fun clearAllChatData() {
        removeConversationsListener()
        _conversations.value = emptyList()
        _localConversations.value = emptyList()
        clearLocalConversations()
        Log.d("ChatViewModel", "Cleared all chat data")
    }
    
    override fun onCleared() {
        super.onCleared()
        removeConversationsListener()
        authListener?.let { firebaseAuth.removeAuthStateListener(it) }
    }
}