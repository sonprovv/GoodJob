package com.project.job.ui.login

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.project.job.R
import com.project.job.databinding.ActivityChangPasswordBinding

class ChangPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChangPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}