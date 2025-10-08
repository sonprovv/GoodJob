package com.project.job

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.project.job.databinding.ActivityMainBinding
import com.project.job.ui.activity.ActivityFragment
import com.project.job.ui.chat.ChatFragment
import com.project.job.ui.dialog.SessionExpiredBottomSheet
import com.project.job.ui.home.HomeFragment
import com.project.job.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionExpiredReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Gán binding trước
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // Thiết lập BottomNavigationView
        setupBottomNavigation()

        // Hiển thị HomeFragment mặc định khi Activity được tạo
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // Xử lý sự kiện khi chọn item trên BottomNavigationView
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.activity -> {
                    replaceFragment(ActivityFragment())
                    true
                }

                R.id.chat -> {
                    replaceFragment(ChatFragment())
                    true
                }

                R.id.profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }

                else -> {
                    replaceFragment(HomeFragment())
                    true
                }
            }
        }

        // Thiết lập BroadcastReceiver để lắng nghe session expired
        setupSessionExpiredReceiver()
    }

    @SuppressLint("RestrictedApi")
    private fun setupBottomNavigation() {
        // Tắt icon tint
        binding.bottomNav.itemIconTintList = null

        // Tắt tooltip và ripple effect cho tất cả các item
        binding.bottomNav.post {
            val bottomNavMenuView = binding.bottomNav.getChildAt(0) as BottomNavigationMenuView
            for (i in 0 until bottomNavMenuView.childCount) {
                val item = bottomNavMenuView.getChildAt(i) as BottomNavigationItemView
                // Tắt tooltip
                item.setOnLongClickListener { true }

                // Tắt ripple effect bằng cách set background drawable
                item.background = null
                item.isClickable = true
                item.isFocusable = true

                // Hoặc thử cách này nếu cách trên không hiệu quả
                try {
                    val field = item.javaClass.getDeclaredField("rippleDrawable")
                    field.isAccessible = true
                    field.set(item, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Hàm để thay thế Fragment trong FrameLayout
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setupSessionExpiredReceiver() {
        sessionExpiredReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.project.job.SESSION_EXPIRED") {
                    Log.d("MainActivity", "Session expired broadcast received, showing dialog")
                    showSessionExpiredDialog()
                }
            }
        }

        // Đăng ký receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            sessionExpiredReceiver,
            IntentFilter("com.project.job.SESSION_EXPIRED")
        )
    }

    private fun showSessionExpiredDialog() {
        try {
            if (!isFinishing && !isDestroyed) {
                val dialog = SessionExpiredBottomSheet()
                dialog.show(supportFragmentManager, "SessionExpiredBottomSheet")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to show session expired dialog", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Hủy đăng ký receiver
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(sessionExpiredReceiver)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to unregister session expired receiver", e)
        }
    }
}