package com.project.job.ui.service.cleaningservice

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.project.job.R
import com.project.job.databinding.ActivitySelectServiceBinding
import com.project.job.ui.intro.CleaningIntroActivity
import com.project.job.ui.map.MapActivity
import com.project.job.utils.addFadeClickEffect

class SelectServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectServiceBinding
    private var selectedOptionId: Int = R.id.ll_h_1 // Default selected option
    private var selectedExtraServiceId: Int = -1 // No default selection for extra services

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        // Set up radio button click listeners
        setupRadioGroup()
        setupExtraServicesRadioGroup()

        // Handle back button click
        binding.ivBack.addFadeClickEffect {
            finish()
        }

        // Handle job detail card click
        binding.cardViewJobDetail.setOnClickListener {
            val cleaningServiceDetailFragment = CleaningServiceDetailFragment()
            cleaningServiceDetailFragment.show(supportFragmentManager, "CleaningServiceDetailFragment")
        }

        // Handle info button click
        binding.ivInfo.addFadeClickEffect {
            val intent = Intent(this, CleaningIntroActivity::class.java)
            startActivity(intent)
        }

        // Handle location header click
        binding.llContentHeader.addFadeClickEffect {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRadioGroup() {
        // Set initial selection
        updateRadioButtonAppearance(selectedOptionId, true)

        // Set click listeners for each option
        binding.llH1.setOnClickListener { handleOptionClick(R.id.ll_h_1) }
        binding.llH2.setOnClickListener { handleOptionClick(R.id.ll_h_2) }
        binding.llH3.setOnClickListener { handleOptionClick(R.id.ll_h_3) }
    }

    private fun handleOptionClick(selectedId: Int) {
        if (selectedOptionId != selectedId) {
            // Reset previous selection
            updateRadioButtonAppearance(selectedOptionId, false)
            // Set new selection
            selectedOptionId = selectedId
            updateRadioButtonAppearance(selectedOptionId, true)
        }
    }

    private fun updateRadioButtonAppearance(viewId: Int, isSelected: Boolean) {
        val backgroundRes = if (isSelected) R.drawable.bg_edt_orange else R.drawable.bg_edt_white
        val textColorRes = if (isSelected) R.color.cam else R.color.black

        when (viewId) {
            R.id.ll_h_1 -> {
                binding.llH1.setBackgroundResource(backgroundRes)
                binding.tvH1.setTextColor(ContextCompat.getColor(this, textColorRes))
            }
            R.id.ll_h_2 -> {
                binding.llH2.setBackgroundResource(backgroundRes)
                binding.tvH2.setTextColor(ContextCompat.getColor(this, textColorRes))
            }
            R.id.ll_h_3 -> {
                binding.llH3.setBackgroundResource(backgroundRes)
                binding.tvH3.setTextColor(ContextCompat.getColor(this, textColorRes))
            }
        }
    }
    
    private fun setupExtraServicesRadioGroup() {
        // Set click listeners for extra services
        binding.rgServiceExtras.findViewById<View>(R.id.ll_extra_cooking).setOnClickListener {
            handleExtraServiceClick(R.id.ll_extra_cooking, R.id.tv_pan)
        }
        binding.rgServiceExtras.findViewById<View>(R.id.ll_extra_ironing).setOnClickListener {
            handleExtraServiceClick(R.id.ll_extra_ironing, R.id.tv_iron)
        }
    }
    
    private fun handleExtraServiceClick(layoutId: Int, textViewId: Int) {
        if (selectedExtraServiceId != layoutId) {
            // Reset previous selection if any
            if (selectedExtraServiceId != -1) {
                updateExtraServiceAppearance(selectedExtraServiceId, false)
            }
            // Set new selection
            selectedExtraServiceId = layoutId
            updateExtraServiceAppearance(selectedExtraServiceId, true)
        } else {
            // Toggle off if clicking the same item
            updateExtraServiceAppearance(selectedExtraServiceId, false)
            selectedExtraServiceId = -1
        }
    }
    
    private fun updateExtraServiceAppearance(layoutId: Int, isSelected: Boolean) {
        val backgroundRes = if (isSelected) R.drawable.bg_edt_orange else R.drawable.bg_edt_white
        val textColorRes = if (isSelected) R.color.cam else R.color.black
        
        when (layoutId) {
            R.id.ll_extra_cooking -> {
                binding.llExtraCooking.setBackgroundResource(backgroundRes)
                binding.tvPan.setTextColor(ContextCompat.getColor(this, textColorRes))
            }
            R.id.ll_extra_ironing -> {
                binding.llExtraIroning.setBackgroundResource(backgroundRes)
                binding.tvIron.setTextColor(ContextCompat.getColor(this, textColorRes))
            }
        }
    }
}