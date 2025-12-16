package com.project.job.ui.intro

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.project.job.base.BaseActivity
import com.project.job.databinding.ActivityMaintenanceIntroBinding
import com.project.job.utils.addFadeClickEffect

class MaintenanceIntroActivity : BaseActivity() {
    private lateinit var binding: ActivityMaintenanceIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMaintenanceIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cardViewButtonBack.addFadeClickEffect {
            finish()
        }
    }
}