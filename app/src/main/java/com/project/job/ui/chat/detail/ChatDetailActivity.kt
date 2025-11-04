package com.project.job.ui.chat.detail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.project.job.base.BaseActivity
import com.project.job.data.model.ChatMessage
import com.project.job.data.source.remote.api.response.chat.MessageData
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivityChatDetailBinding
import com.project.job.utils.Constant
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Timer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityChatDetailBinding
    private val viewModel: ChatDetailViewModel by viewModels()
    private lateinit var adapter: ChatMessagesAdapter
    private lateinit var preferencesManager: PreferencesManager
    private var currentUserId: String = ""

    private val receiverId: String by lazy { intent.getStringExtra(EXTRA_RECEIVER_ID) ?: "" }
    private val partnerName: String? by lazy { intent.getStringExtra(EXTRA_PARTNER_NAME) }
    private val partnerAvatar: String? by lazy { intent.getStringExtra(EXTRA_PARTNER_AVATAR) }

    // Firebase Realtime Database
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseDb: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private var messagesListener: ValueEventListener? = null
    private var conversationId: String = ""
    private var partnerUserListener: ValueEventListener? = null

    // Update message receiver to handle Socket.IO events
    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Constant.ACTION_NEW_MESSAGE) {
                val senderId = intent.getStringExtra("sender_id") ?: ""
                val message = intent.getStringExtra("message") ?: ""
                val notificationType = intent.getStringExtra("notificationType") ?: ""

                Log.d("ChatDetailActivity", "Received broadcast - SenderId: $senderId, Message: $message, Type: $notificationType")

                // Only refresh if message is from current conversation
                if (notificationType == "Chat" && senderId == receiverId) {
                    Log.d("ChatDetailActivity", "Message is for this conversation, refreshing...")
                    attachRealtimeMessagesListener()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Log all intent extras for debugging
        Log.d("ChatDetailActivity", "====== Activity Started ======")
        Log.d("ChatDetailActivity", "Intent action: ${intent.action}")
        Log.d("ChatDetailActivity", "Intent flags: ${intent.flags}")
        Log.d("ChatDetailActivity", "All intent extras:")
        intent.extras?.keySet()?.forEach { key ->
            Log.d("ChatDetailActivity", "  - $key: ${intent.extras?.get(key)}")
        }

        // Initialize PreferencesManager and get current user ID
        preferencesManager = PreferencesManager(this)
        val userData = preferencesManager.getUserData()
        Log.d("ChatDetailActivity", "User data: $userData")
        currentUserId = userData["user_id"] ?: ""
        Log.d("ChatDetailActivity", "Current User ID: '$currentUserId'")
        Log.d("ChatDetailActivity", "Receiver ID: '$receiverId' (isEmpty: ${receiverId.isEmpty()})")
        Log.d("ChatDetailActivity", "Partner Name: '$partnerName'")
        Log.d("ChatDetailActivity", "Partner Avatar: '$partnerAvatar'")
        
        // Validate receiverId
        if (receiverId.isEmpty()) {
            Log.e("ChatDetailActivity", "⚠️⚠️⚠️ Receiver ID is EMPTY! ⚠️⚠️⚠️")
            Log.e("ChatDetailActivity", "Cannot load conversation without receiver ID")
            Log.e("ChatDetailActivity", "Check notification payload - 'sender_id' field may be missing")
            
            // Show error and finish activity
            showError("Không tìm thấy thông tin người nhận. Vui lòng thử lại.")
            finish()
            return
        }
        
        if (currentUserId.isEmpty()) {
            Log.e("ChatDetailActivity", "⚠️ Current User ID is EMPTY! Messages will all appear on LEFT")
        }

        // Register broadcast receiver for in-app notifications
//        registerMessageReceiver()

        setupUI()
        // Build conversation id (sorted order)
        conversationId = if (currentUserId < receiverId) {
            "${currentUserId}_${receiverId}"
        } else {
            "${receiverId}_${currentUserId}"
        }
        // Ensure user profiles (me and partner) exist in Realtime DB
        syncUserProfilesToRealtime()
        // Fetch partner profile (name/avatar) from Realtime to ensure latest
        attachPartnerProfileListener()
        attachRealtimeMessagesListener()
    }

    private fun attachRealtimeMessagesListener() {
        if (conversationId.isEmpty()) return
        val convRef = firebaseDb.getReference("conversations").child(conversationId)
        // Remove previous listener if any
        messagesListener?.let { convRef.removeEventListener(it) }

        _setLoading(true)
        messagesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val list = mutableListOf<MessageData>()
                    for (msgSnap in snapshot.children) {
                        val senderId = msgSnap.child("senderId").getValue(String::class.java) ?: ""
                        val receiverIdV = msgSnap.child("receiverId").getValue(String::class.java) ?: ""
                        val messageText = msgSnap.child("message").getValue(String::class.java) ?: ""
                        val timestamp = msgSnap.child("timestamp").getValue(Long::class.java) ?: 0L
                        val id = msgSnap.key ?: timestamp.toString()
                        val createdAt = toIso8601(timestamp)
                        val messageData = MessageData(
                            id = id,
                            senderId = senderId,
                            receiverId = receiverIdV,
                            message = messageText,
                            type = "text",
                            createdAt = createdAt,
                            isRead = false
                        )
                        list.add(messageData)
                    }
                    val sorted = list.sortedBy { it.createdAt }
                    adapter.submitList(sorted)
                    if (sorted.isNotEmpty()) scrollToBottom()
                } catch (e: Exception) {
                    Log.e("ChatDetailActivity", "Error parsing messages: ${e.message}")
                    showError(e.message ?: "Lỗi không xác định")
                } finally {
                    _setLoading(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _setLoading(false)
                showError(error.message)
            }
        }
        convRef.addValueEventListener(messagesListener as ValueEventListener)
    }
    private fun setupUI() {
        // Setup toolbar
        binding.ivBack.setOnClickListener { finish() }
        binding.tvName.text = partnerName ?: "Tin nhắn"
        Glide.with(this)
            .load(partnerAvatar)
            .circleCrop()
            .into(binding.ivAvatar)

        // Setup RecyclerView with adapter
        Log.d("ChatDetailActivity", "Creating adapter with currentUserId: '$currentUserId'")
        adapter = ChatMessagesAdapter(currentUserId)
        binding.recyclerMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatDetailActivity).apply { 
                stackFromEnd = true 
            }
            adapter = this@ChatDetailActivity.adapter
        }
        
        // Log input field state
        Log.d("ChatDetailActivity", "Input message enabled: ${binding.inputMessage.isEnabled}")
        Log.d("ChatDetailActivity", "Button send enabled: ${binding.buttonSend.isEnabled}")

        // Setup send button and input listeners
        binding.buttonSend.setOnClickListener { sendCurrentInput() }
        binding.inputMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendCurrentInput()
                true
            } else false
        }
    }


    private fun attachPartnerProfileListener() {
        try {
            if (receiverId.isEmpty()) return
            val userRef = firebaseDb.getReference("users").child(receiverId)
            // remove old
            partnerUserListener?.let { userRef.removeEventListener(it) }
            partnerUserListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("username").getValue(String::class.java) ?: ""
                    val avatar = snapshot.child("avatar").getValue(String::class.java) ?: ""
                    if (name.isNotEmpty()) {
                        binding.tvName.text = name
                    }
                    if (avatar.isNotEmpty()) {
                        Glide.with(this@ChatDetailActivity)
                            .load(avatar)
                            .circleCrop()
                            .into(binding.ivAvatar)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatDetailActivity", "attachPartnerProfileListener cancelled: ${error.message}")
                }
            }
            userRef.addValueEventListener(partnerUserListener as ValueEventListener)
        } catch (e: Exception) {
            Log.e("ChatDetailActivity", "attachPartnerProfileListener error: ${e.message}")
        }
    }

    private fun sendCurrentInput() {
        val text = binding.inputMessage.text?.toString()?.trim().orEmpty()
        if (text.isEmpty() || receiverId.isEmpty()) return

        val timestamp = System.currentTimeMillis()
        val msgId = "msg_${timestamp}"
        val convRef = firebaseDb.getReference("conversations").child(conversationId)

        val data = hashMapOf(
            "senderId" to currentUserId,
            "receiverId" to receiverId,
            "message" to text,
            "timestamp" to timestamp
        )

        _setLoading(true)
        convRef.child(msgId).setValue(data)
            .addOnSuccessListener {
                binding.inputMessage.text?.clear()
                _setLoading(false)
            }
            .addOnFailureListener { e ->
                _setLoading(false)
                showError(e.message ?: "Gửi tin nhắn thất bại")
            }
    }

    private fun scrollToBottom() {
        val count = adapter.itemCount
        if (count > 0) binding.recyclerMessages.scrollToPosition(count - 1)
    }

    private fun toIso8601(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    private fun syncUserProfilesToRealtime() {
        try {
            val usersRef = firebaseDb.getReference("users")

            // Current user info from Preferences
            val userData = preferencesManager.getUserData()
            val myName = userData["user_name"] ?: ""
            val myAvatar = userData["user_avatar"] ?: ""
            val myFcm = preferencesManager.getFCMToken() ?: ""

            if (currentUserId.isNotEmpty()) {
                val meMap = hashMapOf<String, Any>()
                if (myName.isNotEmpty()) meMap["username"] = myName
                if (myAvatar.isNotEmpty()) meMap["avatar"] = myAvatar
                if (myFcm.isNotEmpty()) meMap["fcmToken"] = myFcm
                if (meMap.isNotEmpty()) usersRef.child(currentUserId).updateChildren(meMap)
            }

            // Partner info from intent extras
            val partnerMap = hashMapOf<String, Any>()
            (partnerName ?: "").takeIf { it.isNotEmpty() }?.let { partnerMap["username"] = it }
            (partnerAvatar ?: "").takeIf { it.isNotEmpty() }?.let { partnerMap["avatar"] = it }
            if (receiverId.isNotEmpty() && partnerMap.isNotEmpty()) {
                usersRef.child(receiverId).updateChildren(partnerMap)
            }
        } catch (e: Exception) {
            Log.e("ChatDetailActivity", "syncUserProfilesToRealtime error: ${e.message}")
        }
    }

    private fun setLoading(loading: Boolean) {
        Log.d("ChatDetailActivity", "setLoading: $loading")
        binding.progressBar.isVisible = loading
        binding.buttonSend.isEnabled = !loading
        binding.inputMessage.isEnabled = !loading
        Log.d("ChatDetailActivity", "After setLoading - Input enabled: ${binding.inputMessage.isEnabled}, Button enabled: ${binding.buttonSend.isEnabled}")
    }
    private fun _setLoading(loading: Boolean) { setLoading(loading) }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Thử lại") {
                attachRealtimeMessagesListener()
            }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up listeners
        try {
            if (conversationId.isNotEmpty()) {
                val convRef = firebaseDb.getReference("conversations").child(conversationId)
                messagesListener?.let { convRef.removeEventListener(it) }
            }
            if (receiverId.isNotEmpty()) {
                val userRef = firebaseDb.getReference("users").child(receiverId)
                partnerUserListener?.let { userRef.removeEventListener(it) }
            }
        } catch (e: Exception) {
            Log.e("ChatDetailActivity", "Error removing firebase listener", e)
        }
        try {
            unregisterReceiver(messageReceiver)
        } catch (e: Exception) {
            Log.e("ChatDetailActivity", "Error unregistering receiver", e)
        }
    }

    companion object {
        const val EXTRA_RECEIVER_ID = "receiverId"
        const val EXTRA_PARTNER_NAME = "partnerName"
        const val EXTRA_PARTNER_AVATAR = "partnerAvatar"

        fun newIntent(
            context: Context,
            receiverId: String,
            receiverName: String,
            receiverAvatar: String?
        ): Intent {
            return Intent(context, ChatDetailActivity::class.java).apply {
                putExtra(EXTRA_RECEIVER_ID, receiverId)
                putExtra(EXTRA_PARTNER_NAME, receiverName)
                putExtra(EXTRA_PARTNER_AVATAR, receiverAvatar)
            }
        }
    }
}
