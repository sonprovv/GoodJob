package com.project.job.ui.splash

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.project.job.MainActivity
import com.project.job.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    private val SPLASH_DELAY: Long = 4000 // 2 giây
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        // Configure status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        val container = findViewById<LinearLayout>(R.id.containerText)
        val logo = findViewById<ImageView>(R.id.iv_logo)
        val llLogoText = findViewById<LinearLayout>(R.id.ll_logo_text)

        val brandText = "GOODJOB"

        // B1: Animate từng chữ cái
        for ((index, char) in brandText.withIndex()) {
            val tv = TextView(this).apply {
                text = char.toString()
                textSize = 32f
                setTextColor(resources.getColor(R.color.white, theme))
                typeface = resources.getFont(R.font.inknutantiquasemibold)
                gravity = Gravity.CENTER
                scaleX = 0f
                scaleY = 0f
                alpha = 0f
            }
            container.addView(tv)

            val delay = index * 150L
            tv.postDelayed({
                val scaleXAnim = ObjectAnimator.ofFloat(tv, "scaleX", 0f, 1f)
                val scaleYAnim = ObjectAnimator.ofFloat(tv, "scaleY", 0f, 1f)
                val alphaAnim = ObjectAnimator.ofFloat(tv, "alpha", 0f, 1f)

                listOf(scaleXAnim, scaleYAnim, alphaAnim).forEach { anim ->
                    anim.duration = 400
                    anim.interpolator = AccelerateDecelerateInterpolator()
                    anim.start()
                }
            }, delay)
        }

        // B2: Animate logo sau khi text xong
        val totalDelay = brandText.length * 150L + 400L
        logo.apply {
            scaleX = 0f
            scaleY = 0f
            alpha = 0f
            visibility = View.GONE
        }
        logo.postDelayed({
            logo.visibility = View.VISIBLE
            val scaleXAnim = ObjectAnimator.ofFloat(logo, "scaleX", 0f, 1f)
            val scaleYAnim = ObjectAnimator.ofFloat(logo, "scaleY", 0f, 1f)
            val alphaAnim = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f)

            listOf(scaleXAnim, scaleYAnim, alphaAnim).forEach { anim ->
                anim.duration = 600
                anim.interpolator = AccelerateDecelerateInterpolator()
                anim.start()
            }
        }, totalDelay)

        // B3: Thu nhỏ cả cụm (logo + text) sau khi đã hiển thị xong
        val shrinkDelay = totalDelay + 1000L // đợi thêm 1s sau khi logo xong
        llLogoText.postDelayed({
            val scaleXAnim = ObjectAnimator.ofFloat(llLogoText, "scaleX", 1f, 0.7f)
            val scaleYAnim = ObjectAnimator.ofFloat(llLogoText, "scaleY", 1f, 0.7f)

            listOf(scaleXAnim, scaleYAnim).forEach { anim ->
                anim.duration = 600
                anim.interpolator = AccelerateDecelerateInterpolator()
                anim.start()
            }
        }, shrinkDelay)



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