package com.project.job.ui.notification

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.DataJobs
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.data.source.remote.api.response.NotificationInfo
import com.project.job.databinding.ActivityNotificationBinding
import com.project.job.ui.activity.viewmodel.ActivityViewModel
import com.project.job.ui.activity.jobdetail.JobDetailActivity
import com.project.job.ui.notification.adapter.NotificationAdapter
import com.project.job.ui.notification.viewmodel.NotificationViewModel
import com.project.job.ui.service.healthcareservice.viewmodel.HealthCareViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationActivity : AppCompatActivity() {
    private val TAG = "NotificationActivity"
    private lateinit var binding: ActivityNotificationBinding
    private lateinit var viewModel: NotificationViewModel
    private lateinit var activityViewModel: ActivityViewModel
    private lateinit var healthcareViewModel: HealthCareViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var adapter: NotificationAdapter
    
    private var jobList: List<DataJobs> = emptyList()
    private var healthcareServiceList: List<HealthcareService> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = NotificationViewModel()
        activityViewModel = ActivityViewModel()
        healthcareViewModel = HealthCareViewModel()
        preferencesManager = PreferencesManager(this)
        
        // Initialize adapter with menu callback and detail click callback
        adapter = NotificationAdapter(
            onMenuItemClick = { notification, action ->
                handleMenuAction(notification, action)
            },
            onViewDetailClicked = { notification ->
//                navigateToJobDetail(jobID)
                navigateToNotificationDetail(notification)
            }
        )

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }

        // Setup SwipeRefreshLayout
        setupSwipeRefresh()

        // Load initial data
        loadData()

        observeViewModel()
    }

    private fun loadData() {
        val uid = preferencesManager.getUserData()["user_id"] ?: ""
        
        // Load notifications
        viewModel.getNotifications()
        
        // Load job list and healthcare services for navigation
        activityViewModel.getListJob(uid)
        healthcareViewModel.getServiceHealthcare()
    }

//    private fun navigateToJobDetail(jobID: String) {
//        // Find the job by ID
//        val job = jobList.find { it.uid == jobID }
//        if (job != null) {
//            val intent = Intent(this, JobDetailActivity::class.java)
//            intent.putExtra("job", job)
//            intent.putParcelableArrayListExtra("healthcareServiceList", ArrayList(healthcareServiceList))
//            startActivity(intent)
//        } else {
//            Toast.makeText(this, "Không tìm thấy thông tin công việc", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun navigateToNotificationDetail(notification: NotificationInfo) {
        val intent = Intent(this, NotificationDetailActivity::class.java)
        intent.putExtra("title_notification", notification.title)
        intent.putExtra("content_notification", notification.content)
        intent.putExtra("time_notification", notification.createdAt)
        startActivity(intent)
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Refresh all data when user swipes down
            loadData()
        }
        
        // Set refresh colors
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.xanh,
            R.color.chudao,
            R.color.cam
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            launch {
                viewModel.notifications.collectLatest { notifications ->
                    adapter.submitList(notifications)
                    binding.rcvNotification.adapter = adapter
                }
            }
            launch {
                viewModel.loading.collectLatest { isLoading ->
                    // Update SwipeRefreshLayout state based on loading
                    binding.swipeRefreshLayout.isRefreshing = isLoading
                }
            }
            launch {
                viewModel.markReadSuccess.collectLatest { success ->
                    if (success == true) {
                        Toast.makeText(
                            this@NotificationActivity,
                            "Đánh dấu đã đọc thành công",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Refresh notifications after successful mark as read
                        viewModel.getNotifications()
                    }
                }
            }
            launch {
                viewModel.error.collectLatest { errorMessage ->
                    if (!errorMessage.isNullOrEmpty()) {
                        Toast.makeText(
                            this@NotificationActivity,
                            "Lỗi: $errorMessage",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            launch {
                activityViewModel.jobs.collectLatest { jobs ->
                    jobList = jobs ?: emptyList()
                }
            }
            launch {
                healthcareViewModel.healthcareService.collectLatest { services ->
                    healthcareServiceList = services.filterNotNull()
                }
            }
        }
    }

    private fun handleMenuAction(notification: NotificationInfo, action: String) {
        val token = preferencesManager.getAuthToken() ?: ""
        when (action) {
            "mark_read" -> {
                // Call API to mark notification as read
                viewModel.markNotificationAsRead(
                    notificationID = notification.uid
                )
            }

            "delete" -> {
                // TODO: Call API to delete notification
                // For now, just show a toast
                Toast.makeText(this, "Xóa thông báo: ${notification.title}", Toast.LENGTH_SHORT)
                    .show()

                // You can add API call here to delete the notification
                // viewModel.deleteNotification(notification.uid)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}