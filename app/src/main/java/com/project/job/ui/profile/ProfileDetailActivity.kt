package com.project.job.ui.profile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivityProfileDetailBinding
import com.project.job.utils.addFadeClickEffect

class ProfileDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileDetailBinding
    private lateinit var preferencesManager: PreferencesManager

    private val updateProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Profile was updated, refresh UI and pass result back
            refreshUserData()
            setResult(RESULT_OK)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // Handle insets
        preferencesManager = PreferencesManager(this)
        val userName = preferencesManager.getUserData()["user_name"] ?: "User"
        val urlImage = preferencesManager.getUserData()["user_avatar"] ?: ""
        val location = preferencesManager.getUserData()["user_location"] ?: ""
        binding.tvFullName.text = userName
        Glide.with(this)
            .load(urlImage)
            .placeholder(R.drawable.img_profile_picture_defaul)
            .apply(RequestOptions().transform(RoundedCorners(20)))
            .into(binding.ivProfilePicture)

        // Set up back button
        binding.ivBack.addFadeClickEffect {
            onBackPressedDispatcher.onBackPressed()
        }

        // Set up update button
        binding.tvUpdate.addFadeClickEffect {
            // Handle update button click
            val intent = Intent(this, UpdateProfileActivity::class.java)
            updateProfileLauncher.launch(intent)
        }

    }


    private fun refreshUserData() {
        val userName = preferencesManager.getUserData()["user_name"] ?: "User"
        val urlImage = preferencesManager.getUserData()["user_avatar"] ?: ""
        val location = preferencesManager.getUserData()["user_location"] ?: ""
        
        binding.tvFullName.text = userName
        Glide.with(this)
            .load(urlImage)
            .placeholder(R.drawable.img_profile_picture_defaul)
            .apply(RequestOptions().transform(RoundedCorners(20)))
            .into(binding.ivProfilePicture)
    }
}