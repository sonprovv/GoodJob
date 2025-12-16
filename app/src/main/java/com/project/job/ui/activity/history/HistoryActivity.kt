package com.project.job.ui.activity.history

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.job.base.BaseActivity
import com.project.job.data.source.remote.NetworkResult
import com.project.job.data.source.remote.ServiceRemote
import com.project.job.data.source.remote.api.response.DataJobs
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivityHistoryBinding
import com.project.job.ui.activity.history.adapter.HistoryPaymentAdapter
import com.project.job.ui.activity.history.viewmodel.HistoryPaymentViewModel
import com.project.job.ui.activity.jobdetail.JobDetailActivity
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.login.LoginFragment
import com.project.job.utils.addFadeClickEffect
import kotlinx.coroutines.launch


class HistoryActivity : BaseActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var historyPaymentAdapter: HistoryPaymentAdapter
    private val viewModel: HistoryPaymentViewModel by viewModels()
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)
        preferencesManager = PreferencesManager(this)
        
        setupUI()
        setupRecyclerView()
        setupObservers()
        checkLoginStatus()
    }
    
    private fun setupUI() {
        binding.ivBack.addFadeClickEffect {
            onBackPressedDispatcher.onBackPressed()
        }
        
        binding.cardViewButtonLogin.setOnClickListener {
            val loginFragment = LoginFragment.newInstance()
            loginFragment.show(supportFragmentManager, "LoginFragment")
        }
    }
    
    private fun setupRecyclerView() {
        historyPaymentAdapter = HistoryPaymentAdapter()
        binding.rcvHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyPaymentAdapter
        }
        
        // Set item click listener
        historyPaymentAdapter.setOnItemClickListener { payment ->
            Log.d("HistoryActivity", "Payment clicked: ${payment.jobID}")
            lifecycleScope.launch {
                try {
                    loadingDialog.show()
                    val serviceRemote = ServiceRemote.getInstance()
                    val jobsResponse = serviceRemote.getUserPostJobs(payment.userID)
                    when (jobsResponse) {
                        is NetworkResult.Success -> {
                            val jobs = jobsResponse.data.jobs
                            val job: DataJobs? = jobs.firstOrNull { it.uid == payment.jobID }
                            if (job != null) {
                                val intent = Intent(this@HistoryActivity, JobDetailActivity::class.java).apply {
                                    putExtra("job", job)
                                }
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@HistoryActivity, "Không tìm thấy công việc", Toast.LENGTH_LONG).show()
                            }
                        }
                        is NetworkResult.Error -> {
                            Toast.makeText(this@HistoryActivity, "Lỗi tải công việc: ${jobsResponse.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HistoryActivity", "Error navigating to JobDetail: ${e.message}")
                    Toast.makeText(this@HistoryActivity, "Có lỗi xảy ra", Toast.LENGTH_LONG).show()
                } finally {
                    loadingDialog.hide()
                }
            }
        }
    }
    
    private fun setupObservers() {
        lifecycleScope.launch {
            // Observe payment history data
            viewModel.jobs.collect { paymentList ->
                paymentList?.let { payments ->
                    Log.d("HistoryActivity", "Received ${payments.size} payments")
                    if (payments.isNotEmpty()) {
                        historyPaymentAdapter.updateData(payments)
                        showHistoryList()
                    } else {
                        showNoDataState()
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            // Observe loading state
            viewModel.loading.collect { isLoading ->
                if (isLoading) {
                    loadingDialog.show()
                } else {
                    loadingDialog.hide()
                }
            }
        }
        
        lifecycleScope.launch {
            // Observe error state
            viewModel.error.collect { errorMessage ->
                errorMessage?.let { error ->
                    Log.e("HistoryActivity", "Error: $error")
                    Toast.makeText(this@HistoryActivity, "Lỗi: $error", Toast.LENGTH_LONG).show()
                    showNoDataState()
                }
            }
        }
        
        lifecycleScope.launch {
            // Observe success state
            viewModel.success_change.collect { success ->
                success?.let { isSuccess ->
                    Log.d("HistoryActivity", "Success state: $isSuccess")
                    if (!isSuccess) {
                        showNoDataState()
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            // Observe job locations
            viewModel.jobLocations.collect { locationMap ->
                Log.d("HistoryActivity", "Job locations updated: ${locationMap.size} locations")
                historyPaymentAdapter.updateJobLocations(locationMap)
            }
        }
    }
    
    private fun checkLoginStatus() {
        val token = preferencesManager.getAuthToken() ?: ""
        Log.d("HistoryActivity", "Token check: ${if (token.isNotEmpty()) "Valid" else "Empty"}")
        
        if (token.isNotEmpty()) {
            // User is logged in, fetch payment history
            binding.llNoLogin.visibility = View.GONE
            viewModel.getHistoryPayment()
        } else {
            // User is not logged in
            showLoginRequired()
        }
    }
    
    private fun showLoginRequired() {
        binding.llNoLogin.visibility = View.VISIBLE
        binding.llLoginSuccessNoData.visibility = View.GONE
        binding.llListHistory.visibility = View.GONE
    }
    
    private fun showNoDataState() {
        binding.llNoLogin.visibility = View.GONE
        binding.llLoginSuccessNoData.visibility = View.VISIBLE
        binding.llListHistory.visibility = View.GONE
    }
    
    private fun showHistoryList() {
        binding.llNoLogin.visibility = View.GONE
        binding.llLoginSuccessNoData.visibility = View.GONE
        binding.llListHistory.visibility = View.VISIBLE
    }
}