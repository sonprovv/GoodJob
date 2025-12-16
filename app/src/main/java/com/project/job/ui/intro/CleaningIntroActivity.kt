package com.project.job.ui.intro

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.project.job.base.BaseActivity
import com.project.job.databinding.ActivityCleaningIntroBinding
import com.project.job.utils.addFadeClickEffect

class CleaningIntroActivity : BaseActivity() {
    private lateinit var binding: ActivityCleaningIntroBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCleaningIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardViewButtonBack.addFadeClickEffect {
            finish()
        }
    }
}