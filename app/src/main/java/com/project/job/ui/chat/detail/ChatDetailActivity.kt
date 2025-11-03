package com.project.job.ui.chat.detail

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.project.job.base.BaseActivity
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivityChatDetailBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatDetailActivity : BaseActivity() {

    private lateinit var binding: ActivityChatDetailBinding
    private val viewModel: ChatDetailViewModel by viewModels()
    private lateinit var adapter: ChatMessagesAdapter
    private lateinit var preferencesManager: PreferencesManager
    private var currentUserId: String = ""

    private val receiverId: String by lazy { intent.getStringExtra(EXTRA_RECEIVER_ID) ?: "" }
    private val partnerName: String? by lazy { intent.getStringExtra(EXTRA_PARTNER_NAME) }
    private val partnerAvatar: String? by lazy { intent.getStringExtra(EXTRA_PARTNER_AVATAR) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize PreferencesManager and get current user ID
        preferencesManager = PreferencesManager(this)
        val userData = preferencesManager.getUserData()
        Log.d("ChatDetailActivity", "User data: $userData")
        currentUserId = userData["user_id"] ?: ""
        Log.d("ChatDetailActivity", "Current User ID: '$currentUserId'")
        Log.d("ChatDetailActivity", "Receiver ID: '$receiverId'")
        
        if (currentUserId.isEmpty()) {
            Log.e("ChatDetailActivity", "⚠️ Current User ID is EMPTY! Messages will all appear on LEFT")
        }

        setupUI()
        observeViewModel()
        loadMessages()
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

    private fun sendCurrentInput() {
        val text = binding.inputMessage.text?.toString()?.trim().orEmpty()
        Log.d("ChatDetailActivity", "sendCurrentInput called. Text: '$text', isEmpty: ${text.isEmpty()}")
        Log.d("ChatDetailActivity", "receiverId: '$receiverId', isEmpty: ${receiverId.isEmpty()}")
        
        if (text.isEmpty()) {
            Log.w("ChatDetailActivity", "Text is empty, not sending")
            return
        }
        if (receiverId.isEmpty()) {
            Log.e("ChatDetailActivity", "Receiver ID is empty, cannot send!")
            return
        }
        
        Log.d("ChatDetailActivity", "Sending message: '$text' to receiver: '$receiverId'")
        
        // Clear input immediately for better UX
        binding.inputMessage.setText("")
        
        // Send message via ViewModel
        viewModel.sendMessage(receiverId, text)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Observe loading state
            launch {
                viewModel.loading.collectLatest { isLoading ->
                    setLoading(isLoading)
                }
            }
            
            // Observe messages list
            launch {
                viewModel.messages.collectLatest { messagesList ->
                    Log.d("ChatDetailActivity", "Messages received: ${messagesList?.size} items")
                    messagesList?.let {
                        // Log each message with senderId for debugging
                        it.forEachIndexed { index, msg ->
                            val position = if (msg.senderId == currentUserId) "RIGHT" else "LEFT"
                            Log.d("ChatDetailActivity", "Message $index: senderId='${msg.senderId}', currentUserId='$currentUserId', position=$position")
                        }
                        adapter.submitList(it)
                        if (it.isNotEmpty()) {
                            scrollToBottom()
                        }
                    }
                }
            }
            
            // Observe send message success
            launch {
                viewModel.success_change.collectLatest { success ->
                    if (success == true) {
                        Log.d("ChatDetailActivity", "Send message success! Reloading messages...")
                        // Reload messages after sending to get the latest
                        viewModel.getMessages(receiverId)
                        // Note: getMessages no longer sets _success, so no infinite loop
                    } else if (success == false) {
                        Log.w("ChatDetailActivity", "Send message failed")
                    }
                }
            }
            
            // Observe error state
            launch {
                viewModel.error.collectLatest { errorMessage ->
                    errorMessage?.let {
                        showError(it)
                    }
                }
            }
        }
    }
    
    private fun loadMessages() {
        if (receiverId.isEmpty()) {
            showError("Không tìm thấy thông tin người nhận")
            return
        }
        viewModel.getMessages(receiverId)
    }

    private fun scrollToBottom() {
        val count = adapter.itemCount
        if (count > 0) binding.recyclerMessages.scrollToPosition(count - 1)
    }

    private fun setLoading(loading: Boolean) {
        Log.d("ChatDetailActivity", "setLoading: $loading")
        binding.progressBar.isVisible = loading
        binding.buttonSend.isEnabled = !loading
        binding.inputMessage.isEnabled = !loading
        Log.d("ChatDetailActivity", "After setLoading - Input enabled: ${binding.inputMessage.isEnabled}, Button enabled: ${binding.buttonSend.isEnabled}")
    }
    
    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Thử lại") {
                loadMessages()
            }
            .show()
    }

    companion object {
        const val EXTRA_RECEIVER_ID = "receiverId"
        const val EXTRA_PARTNER_NAME = "partnerName"
        const val EXTRA_PARTNER_AVATAR = "partnerAvatar"
    }
}
