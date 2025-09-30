package com.project.job.ui.intro

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.job.R
import com.project.job.databinding.ActivityCleaningIntroBinding
import com.project.job.utils.addFadeClickEffect

class CleaningIntroActivity : AppCompatActivity() {
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