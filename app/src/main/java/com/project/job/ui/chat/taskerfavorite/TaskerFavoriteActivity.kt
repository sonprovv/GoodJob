package com.project.job.ui.chat.taskerfavorite

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.job.R
import com.project.job.databinding.ActivityTaskerFavoriteBinding
import com.project.job.ui.login.LoginFragment
import com.project.job.utils.addFadeClickEffect

class TaskerFavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTaskerFavoriteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTaskerFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.addFadeClickEffect {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.ivInfo.addFadeClickEffect {
            val intent = Intent(this, InfoTaskerFavoriteActivity::class.java)
            startActivity(intent)
        }
        binding.cardViewButtonLogin.setOnClickListener {
            val loginFragment = LoginFragment.newInstance()
            loginFragment.show(supportFragmentManager, "LoginFragment")
        }
    }
}