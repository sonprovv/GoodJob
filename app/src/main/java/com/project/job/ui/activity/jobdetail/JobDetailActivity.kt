package com.project.job.ui.activity.jobdetail

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.project.job.base.BaseActivity
import com.project.job.data.source.remote.api.response.DataJobs
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.data.source.remote.api.response.MaintenanceData
import com.project.job.databinding.ActivityJobDetailBinding
import com.project.job.ui.activity.jobdetail.adapter.TabLayoutAdapter

class JobDetailActivity : BaseActivity() {
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

        // Thêm listener để refresh khi click vào tab đang active
        binding.tabLayoutActivity.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Khi tab được chọn, kiểm tra nếu là tab WorkerOrderFragment (position 1)
                if (tab?.position == 1) {
                    // Lấy fragment hiện tại từ adapter
                    val fragment = supportFragmentManager.findFragmentByTag("f${tab.position}")
                    if (fragment is WorkerOrderFragment) {
                        fragment.refreshData()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Không cần xử lý
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Khi click vào tab đang active, refresh data
                if (tab?.position == 1) {
                    val fragment = supportFragmentManager.findFragmentByTag("f${tab.position}")
                    if (fragment is WorkerOrderFragment) {
                        fragment.refreshData()
                    }
                }
            }
        })

        binding.ivBack.setOnClickListener {
            finish()
        }
    }
}