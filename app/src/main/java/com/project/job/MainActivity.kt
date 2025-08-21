package com.project.job

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.project.job.databinding.ActivityMainBinding
import com.project.job.ui.activity.ActivityFragment
import com.project.job.ui.chat.ChatFragment
import com.project.job.ui.home.HomeFragment
import com.project.job.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        // Gán binding trước
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Hiển thị BottomNavigationView
        binding.bottomNav.itemIconTintList = null

        // Hiển thị HomeFragment mặc định khi Activity được tạo
        if (savedInstanceState == null) { // Chỉ hiển thị fragment lần đầu, tránh tạo lại khi xoay màn hình
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
    }

    // Hàm để thay thế Fragment trong FrameLayout
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // R.id.fragment_container là ID của FrameLayout
            .commit()
    }

}