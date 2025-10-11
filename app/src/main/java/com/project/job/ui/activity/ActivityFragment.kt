package com.project.job.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.data.source.remote.api.response.MaintenanceData
import com.project.job.databinding.FragmentActivityBinding
import com.project.job.ui.activity.adapter.JobAdapter
import com.project.job.ui.activity.adapter.OnJobCancelListener
import com.project.job.ui.activity.history.HistoryActivity
import com.project.job.ui.activity.viewmodel.ActivityViewModel
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.login.LoginFragment
import com.project.job.ui.service.maintenanceservice.viewmodel.MaintenanceViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.project.job.ui.login.LoginResultListener
import com.project.job.ui.service.healthcareservice.viewmodel.HealthCareViewModel
import com.project.job.utils.addFadeClickEffect

class ActivityFragment : Fragment(), LoginResultListener {
    private lateinit var loadingDialog: LoadingDialog
    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel : ActivityViewModel
    private lateinit var viewModelHealthcare : HealthCareViewModel
    private lateinit var viewModelMaintenance : MaintenanceViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var jobAdapter: JobAdapter
    private var healthcareServiceList : List<HealthcareService> = emptyList()
    private var maintenanceServiceList : List<MaintenanceData> = emptyList()
    private var isCancellingJob = false // Flag để track khi đang cancel job
    private var cancellingJobId: String? = null // Lưu jobId đang được cancel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = LoadingDialog(requireActivity())
        viewModel = ActivityViewModel()
        viewModelHealthcare = HealthCareViewModel()
        viewModelMaintenance = MaintenanceViewModel()
        preferencesManager = PreferencesManager(requireContext())

        binding.cardViewButtonLogin.setOnClickListener {
            val loginFragment = LoginFragment()
            loginFragment.setLoginResultListener(this)
            loginFragment.show(parentFragmentManager, "LoginFragment")
        }

        // Initialize adapter
        jobAdapter = JobAdapter()

        // ⚠️ QUAN TRỌNG: Attach ItemTouchHelper để enable swipe gestures
        jobAdapter.getItemTouchHelper().attachToRecyclerView(binding.rcvListJob)

        // Setup RecyclerView
        binding.rcvListJob.adapter = jobAdapter
        binding.rcvListJob.layoutManager = LinearLayoutManager(requireContext())

        jobAdapter.updateList(emptyList(), emptyList(), emptyList())

        // Set listener để xử lý cancel job khi swipe
        jobAdapter.setOnJobCancelListener(object : OnJobCancelListener {
            override fun onJobCancel(jobId: String, serviceType: String) {
                // Đánh dấu đang cancel job và lưu jobId
                isCancellingJob = true
                cancellingJobId = jobId
                viewModel.cancelJob(serviceType.lowercase(), jobId)
            }
        })

        val token = preferencesManager.getAuthToken() ?: ""
        val uid = preferencesManager.getUserData()["user_id"] ?: ""

        // Debug: Log token info để kiểm tra refresh token
        Log.d("ActivityFragment", "Token info on startup: ${preferencesManager.getTokensInfo()}")

        // Kiểm tra xem token có tồn tại hay không
        if (token != "") {
            viewModel.getListJob(uid = uid)
            viewModelHealthcare.getServiceHealthcare()
            viewModelMaintenance.getMaintenanceService()
            binding.llLoginSuccessNoData.visibility = View.VISIBLE
            binding.llNoLogin.visibility = View.GONE
        } else {
            binding.llLoginSuccessNoData.visibility = View.GONE
            binding.llNoLogin.visibility = View.VISIBLE
        }

        binding.tvHistory.addFadeClickEffect {
            val intent = Intent(requireContext(), HistoryActivity::class.java)
            startActivity(intent)
        }

        // Test button để trigger session expired dialog (ẩn trong production)
        binding.tvHistory.setOnLongClickListener {
            // Gửi broadcast để test dialog
            val intent = android.content.Intent("com.project.job.SESSION_EXPIRED")
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)
            true
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Collect loading
            launch {
                viewModel.loading.collectLatest { isLoading ->
                    if (isLoading) {
                        loadingDialog.show()
                        binding.llListJob.visibility = View.GONE
                    } else {
                        loadingDialog.hide()
                        binding.llListJob.visibility = View.VISIBLE
                    }
                }
            }

            launch {
                viewModelHealthcare.loading.collectLatest { isLoading ->
                    if (isLoading) {
                        loadingDialog.show()
                    } else {
                        loadingDialog.hide()
                    }
                }
            }

            // Collect jobs and update adapter
            launch {
                viewModel.jobs.collectLatest { listJob ->
                    if (listJob == null || listJob.isEmpty()) {
                        binding.llLoginSuccessNoData.visibility = View.VISIBLE
                        binding.llListJob.visibility = View.GONE
                    } else {
                        binding.llLoginSuccessNoData.visibility = View.GONE
                        binding.llListJob.visibility = View.VISIBLE
                        jobAdapter.updateList(
                            listJob,
                            healthcareServiceList,
                            maintenanceServiceList
                        )
                    }
                }
            }

            // Observe success state for cancel job (chỉ hiển thị khi thực sự cancel job thành công)
            launch {
                viewModel.success_change.collectLatest { isSuccess ->
                    if (isSuccess == true && isCancellingJob && cancellingJobId != null) {
                        // Chỉ hiển thị Toast khi thực sự cancel job thành công
                        Toast.makeText(
                            requireContext(),
                            "Huỷ bài đăng thành công!",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Cập nhật status của job đó thành "Cancel" thay vì reload toàn bộ list
                        jobAdapter.updateJobStatus(cancellingJobId!!, "Cancel")
                        
                        // Reset flags sau khi cập nhật
                        isCancellingJob = false
                        cancellingJobId = null
                    }
                }
            }

            // Observe healthcare services
            launch {
                viewModelHealthcare.healthcareService.collectLatest { listService ->
                    healthcareServiceList = listService.filterNotNull()
                    // Update the adapter with the latest services
                    jobAdapter.updateList(
                        jobAdapter.jobList,
                        healthcareServiceList,
                        maintenanceServiceList
                    )
                }
            }

            // Observe maintenance services
            launch {
                viewModelMaintenance.maintenanceService.collectLatest { listService ->
                    maintenanceServiceList = listService.filterNotNull()
                    // Update the adapter with the latest services
                    jobAdapter.updateList(
                        jobAdapter.jobList,
                        healthcareServiceList,
                        maintenanceServiceList
                    )
                }
            }
        }
    }

    override fun onLoginSuccess() {
        val token = preferencesManager.getAuthToken() ?: ""
        val uid = preferencesManager.getUserData()["user_id"] ?: ""

        if (token.isNotEmpty()) {
            // ⚠️ QUAN TRỌNG: Attach ItemTouchHelper khi đăng nhập thành công
            jobAdapter.getItemTouchHelper().attachToRecyclerView(binding.rcvListJob)

            viewModel.getListJob(uid = uid)
            viewModelHealthcare.getServiceHealthcare()
            viewModelMaintenance.getMaintenanceService()
            binding.llLoginSuccessNoData.visibility = View.VISIBLE
            binding.llNoLogin.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}