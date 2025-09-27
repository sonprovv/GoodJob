package com.project.job.ui.reviewworker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivityReviewerBinding
import com.project.job.ui.activity.jobdetail.viewmodel.JobDetailViewModel
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.reviewworker.adapter.CommentAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReviewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewerBinding
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var viewModel: JobDetailViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var loadingDialog: LoadingDialog
    
    companion object {
        private const val EXTRA_WORKER_ID = "worker_id"
        private const val EXTRA_WORKER_NAME = "worker_name"
        private const val EXTRA_SERVICE_TYPE = "serviceType"
        private const val EXTRA_WORKER_AVATAR = "worker_avatar"
        private const val EXTRA_WORKER_PHONE = "worker_phone"
        private const val EXTRA_WORKER_LOCATION = "worker_location"
        private const val EXTRA_WORKER_DESCRIPTION = "worker_description"
        private const val EXTRA_WORKER_GENDER = "worker_gender"
        private const val EXTRA_WORKER_BIRTHDATE = "worker_birthdate"
        private const val EXTRA_WORKER_EMAIL = "worker_email"
        
        // Service type constants
        const val SERVICE_TYPE_HEALTHCARE = "HEALTHCARE"
        const val SERVICE_TYPE_CLEANING = "CLEANING"
        const val SERVICE_TYPE_MAINTENANCE = "MAINTENANCE"
        
        @JvmStatic
        fun newIntent(
            context: android.content.Context,
            workerId: String,
            workerName: String,
            serviceType: String = SERVICE_TYPE_HEALTHCARE,
            workerAvatar: String? = null,
            workerPhone: String? = null,
            workerLocation: String? = null,
            workerDescription: String? = null,
            workerGender: String? = null,
            workerBirthdate: String? = null,
            workerEmail: String? = null
        ): Intent {
            // Validate service type
            val validServiceType = when (serviceType.uppercase()) {
                SERVICE_TYPE_HEALTHCARE -> SERVICE_TYPE_HEALTHCARE
                SERVICE_TYPE_CLEANING -> SERVICE_TYPE_CLEANING
                SERVICE_TYPE_MAINTENANCE -> SERVICE_TYPE_MAINTENANCE
                else -> SERVICE_TYPE_HEALTHCARE // Default to HEALTHCARE if invalid
            }
            return Intent(context, ReviewerActivity::class.java).apply {
                putExtra(EXTRA_WORKER_ID, workerId)
                putExtra(EXTRA_WORKER_NAME, workerName)
                putExtra(EXTRA_SERVICE_TYPE, validServiceType)
                putExtra(EXTRA_WORKER_AVATAR, workerAvatar)
                putExtra(EXTRA_WORKER_PHONE, workerPhone)
                putExtra(EXTRA_WORKER_LOCATION, workerLocation)
                putExtra(EXTRA_WORKER_DESCRIPTION, workerDescription)
                putExtra(EXTRA_WORKER_GENDER, workerGender)
                putExtra(EXTRA_WORKER_BIRTHDATE, workerBirthdate)
                putExtra(EXTRA_WORKER_EMAIL, workerEmail)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReviewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = JobDetailViewModel()
        preferencesManager = PreferencesManager(this)
        loadingDialog = LoadingDialog(this)
        // Lấy thông tin cơ bản
        val workerId = intent.getStringExtra(EXTRA_WORKER_ID) ?: ""
        val workerName = intent.getStringExtra(EXTRA_WORKER_NAME) ?: ""
        val serviceType = intent.getStringExtra(EXTRA_SERVICE_TYPE) ?: SERVICE_TYPE_HEALTHCARE
        
        // Lấy thông tin bổ sung
        val workerAvatar = intent.getStringExtra(EXTRA_WORKER_AVATAR)
        val workerPhone = intent.getStringExtra(EXTRA_WORKER_PHONE)
        val workerLocation = intent.getStringExtra(EXTRA_WORKER_LOCATION)
        val workerDescription = intent.getStringExtra(EXTRA_WORKER_DESCRIPTION)
        val workerGender = intent.getStringExtra(EXTRA_WORKER_GENDER)
        val workerBirthdate = intent.getStringExtra(EXTRA_WORKER_BIRTHDATE)
        val workerEmail = intent.getStringExtra(EXTRA_WORKER_EMAIL)
        
        // Set the title in the toolbar
        binding.tvTitle.text = getString(R.string.reviews_for, workerName)

        binding.ivBack.setOnClickListener {
            finish()
        }

        // Initialize CommentAdapter with the service type
        commentAdapter = CommentAdapter()
        binding.rcvReview.adapter = commentAdapter
        
        // Tải dữ liệu đánh giá
        loadReviewData(workerId, serviceType)
    }

    
    private fun showLoading() {
        loadingDialog.show()
    }
    
    private fun hideLoading() {
        loadingDialog.hide()
    }
    
    private fun loadReviewData(workerId: String, serviceType: String) {
        val token = preferencesManager.getAuthToken() ?: ""
        
        // Hiển thị loading khi bắt đầu tải dữ liệu
        showLoading()
        
        // Lấy đánh giá dựa trên loại dịch vụ, nhưng sử dụng "ALL" để lấy tất cả đánh giá
        viewModel.getReviewWorker(workerId, "ALL")
        
        // Theo dõi dữ liệu đánh giá
        lifecycleScope.launch {
            viewModel.userReview.collectLatest { reviews ->
                // Ẩn loading khi đã nhận được dữ liệu
                hideLoading()
                
                // Cập nhật adapter với dữ liệu đánh giá
                reviews?.let {
                    // Hiển thị thông báo nếu không có đánh giá
                    if (it.isEmpty()) {
                        binding.tvNoReviews.visibility = View.VISIBLE
                        binding.rcvReview.visibility = View.GONE
                    } else {
                        binding.tvNoReviews.visibility = View.GONE
                        binding.rcvReview.visibility = View.VISIBLE
                        commentAdapter.submitList(it)
                    }
                }
            }
        }
    }
}