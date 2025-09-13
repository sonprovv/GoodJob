package com.project.job.ui.activity.history

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivityHistoryBinding
import com.project.job.ui.login.LoginFragment
import com.project.job.utils.addFadeClickEffect


class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferencesManager = PreferencesManager(this)

        binding.ivBack.addFadeClickEffect {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.cardViewButtonLogin.setOnClickListener {
            val loginFragment = LoginFragment.newInstance()
            loginFragment.show(supportFragmentManager, "LoginFragment")
        }

        // Sau khi dăng nhập thành công, cập nhật giao diện người dùng
        val token = preferencesManager.getAuthToken() ?: ""
        if(token.isNotEmpty()){
            binding.llNoLogin.visibility = View.GONE
            binding.llLoginSuccessNoData.visibility = View.VISIBLE
        }
        else {
            binding.llNoLogin.visibility = View.VISIBLE
            binding.llLoginSuccessNoData.visibility = View.GONE
            binding.llListHistory.visibility = View.GONE
        }


    }
}