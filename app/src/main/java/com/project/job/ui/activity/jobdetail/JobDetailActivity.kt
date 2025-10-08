package com.project.job.ui.activity.jobdetail

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.project.job.data.source.remote.api.response.DataJobs
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.data.source.remote.api.response.MaintenanceData
import com.project.job.databinding.ActivityJobDetailBinding
import com.project.job.ui.activity.jobdetail.adapter.TabLayoutAdapter

class JobDetailActivity : AppCompatActivity() {
    lateinit var binding: ActivityJobDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityJobDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dataJob = intent.getParcelableExtra<DataJobs>("job")
        val healthcareServiceList = intent.getParcelableArrayListExtra<HealthcareService>("healthcareServiceList")
        Log.d("JobDetailActivity", "dataJob: $dataJob")
        Log.d("JobDetailActivity", "Healthcare services received: ${healthcareServiceList?.size}")
        healthcareServiceList?.forEachIndexed { index, service ->
            Log.d("JobDetailActivity", "Service $index: ${service.uid} - ${service.serviceName}")
        }
        val maintenanceServiceList = intent.getParcelableArrayListExtra<MaintenanceData>("maintenanceServiceList")
        Log.d("JobDetailActivity", "Maintenance services received: ${maintenanceServiceList?.size}")
        maintenanceServiceList?.forEachIndexed{index, service ->
            Log.d("JobDetailActivity", "Service $index: ${service.uid} - ${service.serviceName}")
        }

        // Set up ViewPager2 with adapter
        dataJob?.let { job ->
            val adapter = TabLayoutAdapter(this, job, healthcareServiceList, maintenanceServiceList)
            binding.viewPagerActivity.adapter = adapter
        }

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayoutActivity, binding.viewPagerActivity) { tab, position ->
            tab.text = when (position) {
                0 -> "Thông tin công việc"
                1 -> "Người ứng tuyển"
                else -> "Thông tin công việc"
            }
        }.attach()

        binding.ivBack.setOnClickListener {
            finish()
        }
    }
}