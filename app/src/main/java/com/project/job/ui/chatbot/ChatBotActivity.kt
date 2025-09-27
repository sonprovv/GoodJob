package com.project.job.ui.chatbot

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.EditorInfo
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.job.data.model.ChatMessage
import com.project.job.databinding.ActivityChatBotBinding

class ChatBotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBotBinding
    private lateinit var chatBotAdapter: ChatBotAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatBotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupRecyclerView()
        setupClickListeners()
        showWelcomeMessage()
        
        // Debug: Check if input layout is visible
        binding.inputLayout.post {
            android.util.Log.d("ChatBot", "Input layout height: ${binding.inputLayout.height}")
            android.util.Log.d("ChatBot", "Input layout visibility: ${binding.inputLayout.visibility}")
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            
            // Adjust padding for system bars
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            
            // Handle keyboard visibility
            if (ime.bottom > 0) {
                // Keyboard is visible - scroll to bottom to show latest message
                binding.root.post {
                    scrollToBottom()
                }
            }
            
            insets
        }
    }

    private fun setupRecyclerView() {
        chatBotAdapter = ChatBotAdapter(chatMessages)
        binding.rvChatMessages.apply {
            adapter = chatBotAdapter
            layoutManager = LinearLayoutManager(this@ChatBotActivity)
            setHasFixedSize(false)
        }
    }

    private fun setupClickListeners() {
        // Send button click
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        // Send on Enter key
        binding.etMessageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
        
        // Handle focus changes to ensure input is visible
        binding.etMessageInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.root.post {
                    scrollToBottom()
                }
            }
        }

        // Back button
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        // Clear chat
        binding.tvClear.setOnClickListener {
            clearChat()
        }
    }

    private fun sendMessage() {
        val messageText = binding.etMessageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            // Add user message
            val userMessage = ChatMessage(messageText, true, System.currentTimeMillis())
            chatMessages.add(userMessage)
            chatBotAdapter.notifyItemInserted(chatMessages.size - 1)

            // Clear input
            binding.etMessageInput.text.clear()

            // Scroll to bottom
            scrollToBottom()

            // Show typing indicator
            showTypingIndicator()

            // Simulate AI response (replace with actual API call)
            simulateAIResponse(messageText)
        }
    }

    private fun showTypingIndicator() {
        val typingMessage = ChatMessage("Typing...", false, System.currentTimeMillis(), true)
        chatMessages.add(typingMessage)
        chatBotAdapter.notifyItemInserted(chatMessages.size - 1)
        scrollToBottom()
    }

    private fun hideTypingIndicator() {
        val typingIndex = chatMessages.indexOfFirst { it.isTyping }
        if (typingIndex != -1) {
            chatMessages.removeAt(typingIndex)
            chatBotAdapter.notifyItemRemoved(typingIndex)
        }
    }

    private fun simulateAIResponse(userMessage: String) {
        // Simulate API call delay
        Handler(Looper.getMainLooper()).postDelayed({
            // Remove typing indicator
            hideTypingIndicator()

            // Generate AI response
            val aiResponse = generateAIResponse(userMessage)
            val aiMessage = ChatMessage(aiResponse, false, System.currentTimeMillis())

            chatMessages.add(aiMessage)
            chatBotAdapter.notifyItemInserted(chatMessages.size - 1)
            scrollToBottom()
        }, 1500) // 1.5 seconds delay
    }

    private fun generateAIResponse(userMessage: String): String {
        // Simple response logic - replace with actual AI API integration
        return when {
            userMessage.contains("hello", ignoreCase = true) ->
                "Hello! I'm your AI assistant. How can I help you with your job search today?"

            userMessage.contains("job", ignoreCase = true) ->
                "I can help you find job opportunities. What type of job are you looking for?"

            userMessage.contains("salary", ignoreCase = true) ->
                "Salary expectations vary by industry and experience. Could you tell me more about your field?"

            userMessage.contains("thank", ignoreCase = true) ->
                "You're welcome! Is there anything else I can help you with?"

            userMessage.contains("help", ignoreCase = true) ->
                "I can assist you with:\n• Job search tips\n• Resume advice\n• Interview preparation\n• Career guidance\n\nWhat would you like to know?"

            else ->
                "I understand you're asking about: \"$userMessage\". This is a great topic! " +
                        "Could you provide more details so I can give you the best assistance?"
        }
    }

    private fun scrollToBottom() {
        if (chatMessages.isNotEmpty()) {
            binding.rvChatMessages.post {
                binding.rvChatMessages.smoothScrollToPosition(chatMessages.size - 1)
            }
        }
    }

    private fun showWelcomeMessage() {
        Handler(Looper.getMainLooper()).postDelayed({
            val welcomeMessage = ChatMessage(
                "👋 Hello! I'm your Job Assistant AI. I can help you with:\n\n" +
                        "• Job search strategies\n• Resume optimization\n• Interview tips\n• Career advice\n\n" +
                        "What would you like to know today?",
                false,
                System.currentTimeMillis()
            )
            chatMessages.add(welcomeMessage)
            chatBotAdapter.notifyItemInserted(chatMessages.size - 1)
            scrollToBottom()
        }, 500)
    }

    private fun clearChat() {
        chatMessages.clear()
        chatBotAdapter.notifyDataSetChanged()
        showWelcomeMessage()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}