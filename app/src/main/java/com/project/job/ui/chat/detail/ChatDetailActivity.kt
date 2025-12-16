package com.project.job.ui.chat.detail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.project.job.base.BaseActivity
import com.project.job.data.source.remote.api.response.chat.MessageData
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivityChatDetailBinding
import com.project.job.utils.Constant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ChatDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityChatDetailBinding
    private lateinit var adapter: ChatMessagesAdapter
    private lateinit var preferencesManager: PreferencesManager
    private var currentUserId: String = ""

    private val receiverId: String by lazy {
        intent.getStringExtra(EXTRA_RECEIVER_ID)
            ?: intent.getStringExtra("senderId")  // ✅ Fallback từ notification
            ?: ""
    }

    private val partnerName: String? by lazy {
        intent.getStringExtra(EXTRA_PARTNER_NAME)
            ?: intent.getStringExtra("senderName")  // ✅ Fallback từ notification
    }

    private val partnerAvatar: String? by lazy {
        intent.getStringExtra(EXTRA_PARTNER_AVATAR)
            ?: intent.getStringExtra("senderAvatar")  // ✅ Fallback từ notification
    }

    private val roomId: String? by lazy {
        intent.getStringExtra(EXTRA_ROOM_ID)
            ?: intent.getStringExtra("chat_room_id")  // ✅ Fallback từ notification
            ?: intent.getStringExtra("roomId")  // ✅ Fallback từ notification
    }
    // Firebase Realtime Database
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firebaseDb: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private var messagesListener: ValueEventListener? = null
    private var conversationId: String = ""
    private var partnerUserListener: ValueEventListener? = null

    // Update message receiver to handle Socket.IO events
    private var isReceiverRegistered = false
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

        // Thiết lập màu sắc cho status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.parseColor("#FFFFFF") // Màu nền status bar
        }

        // Đặt icon sáng/tối cho status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Icon sáng cho nền tối
            // Nếu muốn icon tối cho nền sáng, bỏ dòng trên hoặc dùng:
            // window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }


        // Log all intent extras for debugging
        Log.d("ChatDetailActivity", "====== Activity Started onCreate ======")
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
//        conversationId = if (currentUserId < receiverId) {
//            "${currentUserId}_${receiverId}"
//        } else {
//            "${receiverId}_${currentUserId}"
//        }
        conversationId = roomId ?: ""
        Log.d("ChatDetailActivity", "Using conversationId: '$conversationId'")
        // Initialize room structure in Realtime DB
        initRoomIfNeeded()
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

    private fun initRoomIfNeeded() {
        try {
            if (conversationId.isEmpty()) return
            val roomsRef = firebaseDb.getReference("rooms").child(conversationId)
            // Nếu có conversationId rồi thì bỏ qua khởi tạo
            if (roomsRef.key != null) return

            Log.d("ChatDetailActivity", "Initializing room at rooms/$conversationId")
            val usersMap = hashMapOf<String, Any>()

            // Current user profile from Preferences
            val myData = preferencesManager.getUserData()
            val myName = myData["user_name"] ?: ""
            val myAvatar = myData["user_avatar"] ?: ""
            val me = hashMapOf<String, Any>()
            if (myName.isNotEmpty()) me["username"] = myName
            if (myAvatar.isNotEmpty()) me["avatar"] = myAvatar

            // Partner profile from intent extras
            val partner = hashMapOf<String, Any>()
            (partnerName ?: "").takeIf { it.isNotEmpty() }?.let { partner["username"] = it }
            (partnerAvatar ?: "").takeIf { it.isNotEmpty() }?.let { partner["avatar"] = it }

            if (me.isNotEmpty()) usersMap[currentUserId] = me
            if (partner.isNotEmpty()) usersMap[receiverId] = partner

            val roomData = hashMapOf<String, Any>()
            // Don't overwrite existing lastMessage/lastTimestamp if they exist; just ensure fields present on first create
            roomData["lastMessage"] = ""
            roomData["lastTimestamp"] = 0L
            if (usersMap.isNotEmpty()) roomData["users"] = usersMap

            roomsRef.updateChildren(roomData)
            Log.d("ChatDetailActivity", "Room initialized/updated successfully")
        } catch (e: Exception) {
            Log.e("ChatDetailActivity", "initRoomIfNeeded error: ${e.message}")
        }
    }

    private fun sendCurrentInput() {
        val text = binding.inputMessage.text?.toString()?.trim().orEmpty()
        if (text.isEmpty() || receiverId.isEmpty()) return

        val timestamp = System.currentTimeMillis()
        val msgId = "msg_${timestamp}"
        val convRef = firebaseDb.getReference("conversations").child(conversationId)
        val roomsRef = firebaseDb.getReference("rooms").child(conversationId)

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

                // Update rooms/{conversationId}
                try {
                    val usersMap = hashMapOf<String, Any>()

                    // Current user profile
                    val myName = preferencesManager.getUserData()["user_name"] ?: ""
                    val myAvatar = preferencesManager.getUserData()["user_avatar"] ?: ""
                    val me = hashMapOf<String, Any>()
                    if (myName.isNotEmpty()) me["username"] = myName
                    if (myAvatar.isNotEmpty()) me["avatar"] = myAvatar

                    // Partner profile from extras and realtime cache
                    val partner = hashMapOf<String, Any>()
                    (partnerName ?: "").takeIf { it.isNotEmpty() }?.let { partner["username"] = it }
                    (partnerAvatar ?: "").takeIf { it.isNotEmpty() }?.let { partner["avatar"] = it }

                    if (me.isNotEmpty()) usersMap[currentUserId] = me
                    if (partner.isNotEmpty()) usersMap[receiverId] = partner

                    val roomData = hashMapOf<String, Any>(
                        "lastMessage" to text,
                        "lastTimestamp" to timestamp
                    )
                    if (usersMap.isNotEmpty()) roomData["users"] = usersMap

                    roomsRef.updateChildren(roomData)
                } catch (e: Exception) {
                    Log.e("ChatDetailActivity", "Update room failed: ${e.message}")
                }
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

    // Removed syncing to top-level users node per new requirement

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

    override fun onResume() {
        super.onResume()
        if (!isReceiverRegistered) {
            try {
                val filter = IntentFilter(Constant.ACTION_NEW_MESSAGE)
                registerReceiver(messageReceiver, filter)
                isReceiverRegistered = true
                Log.d("ChatDetailActivity", "Registered message receiver")
            } catch (e: Exception) {
                Log.e("ChatDetailActivity", "Error registering receiver", e)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(messageReceiver)
                isReceiverRegistered = false
                Log.d("ChatDetailActivity", "Unregistered message receiver in onPause")
            } catch (e: IllegalArgumentException) {
                Log.e("ChatDetailActivity", "Receiver not registered in onPause", e)
            }
        }
    }

    override fun onDestroy() {
        // Clean up Firebase listeners
        try {
            if (conversationId.isNotEmpty()) {
                val convRef = firebaseDb.getReference("conversations").child(conversationId)
                messagesListener?.let { convRef.removeEventListener(it) }
            }
        } catch (e: Exception) {
            Log.e("ChatDetailActivity", "Error removing Firebase listener", e)
        }
        
        // Safely unregister receiver
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(messageReceiver)
                isReceiverRegistered = false
                Log.d("ChatDetailActivity", "Unregistered message receiver in onDestroy")
            } catch (e: IllegalArgumentException) {
                Log.e("ChatDetailActivity", "Error unregistering receiver in onDestroy", e)
            }
        }
        
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // ✅ QUAN TRỌNG: Update intent hiện tại
        Log.d("ChatDetailActivity", "onNewIntent called")
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent) {
        Log.d("ChatDetailActivity", "=== handleNotificationIntent ===")
        Log.d("ChatDetailActivity", "Intent action: ${intent.action}")
        Log.d("ChatDetailActivity", "Intent extras:")
        intent.extras?.keySet()?.forEach { key ->
            Log.d("ChatDetailActivity", "  - $key: ${intent.extras?.get(key)}")
        }

        // Nếu đến từ notification, có thể có data trong intent extras
        val notifRoomId = intent.getStringExtra("roomId")
            ?: intent.getStringExtra("chat_room_id")
        val notifSenderId = intent.getStringExtra("senderId")

        if (!notifRoomId.isNullOrEmpty() && !notifSenderId.isNullOrEmpty()) {
            Log.d("ChatDetailActivity", "📱 Opened from notification!")
            Log.d("ChatDetailActivity", "  RoomId: $notifRoomId")
            Log.d("ChatDetailActivity", "  SenderId: $notifSenderId")

            // Nếu activity đã được khởi tạo và đang hiển thị conversation khác
            // thì refresh lại với conversation mới
            if (this::adapter.isInitialized && conversationId.isNotEmpty() && conversationId != notifRoomId) {
                // Update conversation và refresh
                conversationId = notifRoomId
                attachRealtimeMessagesListener()
            }
        }
    }

    companion object {
        const val EXTRA_RECEIVER_ID = "receiverId"
        const val EXTRA_PARTNER_NAME = "partnerName"
        const val EXTRA_PARTNER_AVATAR = "partnerAvatar"
        const val EXTRA_ROOM_ID = "roomId"

        fun newIntent(
            context: Context,
            receiverId: String,
            partnerName: String? = null,
            partnerAvatar: String? = null,
            roomId: String? = null
        ): Intent {
            return Intent(context, ChatDetailActivity::class.java).apply {
                putExtra(EXTRA_RECEIVER_ID, receiverId)
                putExtra(EXTRA_PARTNER_NAME, partnerName)
                putExtra(EXTRA_PARTNER_AVATAR, partnerAvatar)
                putExtra(EXTRA_ROOM_ID, roomId)
            }
        }
    }
}
