package com.project.job.ui.notification

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.job.R
import com.project.job.databinding.ActivityNotificationDetailBinding
import com.project.job.utils.addFadeClickEffect

class NotificationDetailActivity : AppCompatActivity() {
    private lateinit var binding : ActivityNotificationDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        val title = intent.getStringExtra("title_notification")
        val content = intent.getStringExtra("content_notification")
        val time_create = intent.getStringExtra("time_notification")

        binding.tvTitleNotification.text = title
        binding.tvContentNotification.text = content
        binding.tvTimeNotification.text = time_create

        binding.ivBack.addFadeClickEffect {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }
    }
}