package com.project.job.ui.activity.history

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.job.R
import com.project.job.databinding.ActivityHistoryBinding
import com.project.job.ui.login.LoginFragment

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.cardViewButtonLogin.setOnClickListener {
            val loginFragment = LoginFragment.newInstance()
            loginFragment.show(supportFragmentManager, "LoginFragment")
        }

    }
}