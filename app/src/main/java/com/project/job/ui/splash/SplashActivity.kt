package com.project.job.ui.splash

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.project.job.MainActivity
import com.project.job.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private val SPLASH_DELAY: Long = 2000 // 2 giây
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        // Configure status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN


        lifecycleScope.launch {
            delay(SPLASH_DELAY)

            // Tạo Intent để chuyển sang MainActivity
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)

            // Kết thúc SplashActivity để người dùng không thể quay lại nó
            finish()
        }
    }
}