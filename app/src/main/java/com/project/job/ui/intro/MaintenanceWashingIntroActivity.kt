package com.project.job.ui.intro

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.job.R
import com.project.job.databinding.ActivityMaintenanceWashingIntroBinding
import com.project.job.utils.addFadeClickEffect

class MaintenanceWashingIntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMaintenanceWashingIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMaintenanceWashingIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cardViewButtonBack.addFadeClickEffect {
            finish()
        }
    }
}