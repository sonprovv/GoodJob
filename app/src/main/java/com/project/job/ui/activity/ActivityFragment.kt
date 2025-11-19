package com.project.job.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.job.R
import com.project.job.base.BaseFragment
import com.project.job.data.mapper.JobEntityToDataJobsMapper
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

class ActivityFragment : BaseFragment(), LoginResultListener {
    private lateinit var loadingDialog: LoadingDialog
    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!
    // Use viewModels delegate for AndroidViewModel
    private val viewModel: ActivityViewModel by viewModels()
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
        // viewModel is already initialized by viewModels delegate
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
        
        // Setup status filter spinner
        setupStatusFilter()
        
        // Setup sort button
        setupSortButton()

        // Set listener để xử lý cancel job khi swipe
        jobAdapter.setOnJobCancelListener(object : OnJobCancelListener {
            override fun onJobCancel(jobId: String, serviceType: String) {
                // Đánh dấu đang cancel job và lưu jobId
                isCancellingJob = true
                cancellingJobId = jobId

                // Debug logging
                Log.d("ActivityFragment", "Original serviceType: '$serviceType'")
                val formattedServiceType = serviceType.lowercase()
                Log.d("ActivityFragment", "Formatted serviceType: '$formattedServiceType'")

                viewModel.cancelJob(formattedServiceType, jobId)
            }
        })

        val token = preferencesManager.getAuthToken() ?: ""
        val uid = preferencesManager.getUserData()["user_id"] ?: ""

        // Debug: Log token info để kiểm tra refresh token
        Log.d("ActivityFragment", "Token info on startup: ${preferencesManager.getTokensInfo()}")

        // Kiểm tra xem token có tồn tại hay không
        if (token != "") {
            // Start observing local jobs (auto-update when data changes)
            viewModel.observeLocalJobs(uid)
            // Refresh jobs from API (will update local database and trigger UI update)
            viewModel.refreshJobs(uid)
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
            startActivityWithAnimation(intent)
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
        viewLifecycleOwner.lifecycleScope.launch {
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

            // Collect local jobs from Room database (auto-update when data changes)
            launch {
                viewModel.localJobs.collectLatest { jobEntities ->
                    Log.d("ActivityFragment", "Local jobs updated: ${jobEntities.size} jobs")
                    
                    if (jobEntities.isEmpty()) {
                        binding.llLoginSuccessNoData.visibility = View.VISIBLE
                        binding.llListJob.visibility = View.GONE
                    } else {
                        binding.llLoginSuccessNoData.visibility = View.GONE
                        binding.llListJob.visibility = View.VISIBLE
                        
                        // Convert JobEntity to DataJobs for adapter
                        val dataJobsList = JobEntityToDataJobsMapper.toDataJobsList(jobEntities)
                        jobAdapter.updateList(
                            dataJobsList,
                            healthcareServiceList,
                            maintenanceServiceList
                        )
                    }
                }
            }

            // Observe success state for cancel job
            launch {
                viewModel.success_change.collectLatest { isSuccess ->
                    if (isSuccess == true && isCancellingJob && cancellingJobId != null) {
                        // Hiển thị Toast khi cancel job thành công
                        Toast.makeText(
                            requireContext(),
                            "Huỷ bài đăng thành công!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Local database sẽ tự động update qua Flow
                        // Không cần gọi updateJobStatus nữa

                        // Reset flags sau khi cập nhật
                        isCancellingJob = false
                        cancellingJobId = null
                    }
                }
            }

            // Observe error state for cancel job
            launch {
                viewModel.error.collectLatest { errorMessage ->
                    if (errorMessage != null && isCancellingJob && cancellingJobId != null) {
                        // Hiển thị error message khi cancel job thất bại
                        Toast.makeText(
                            requireContext(),
                            "Lỗi: $errorMessage",
                            Toast.LENGTH_LONG
                        ).show()

                        // Reset item về vị trí ban đầu nếu có lỗi
                        jobAdapter.notifyItemChanged(
                            jobAdapter.jobList.indexOfFirst { it.uid == cancellingJobId }
                        )

                        // Reset flags
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

            // Start observing local jobs
            viewModel.observeLocalJobs(uid)
            // Refresh jobs from API
            viewModel.refreshJobs(uid)
            viewModelHealthcare.getServiceHealthcare()
            viewModelMaintenance.getMaintenanceService()
            binding.llLoginSuccessNoData.visibility = View.VISIBLE
            binding.llNoLogin.visibility = View.GONE
        }
    }

    private fun setupStatusFilter() {
        val statusList = listOf(
            "Tất cả",
            "Not Payment",
            "Hiring",
            "Processing",
            "Active",
            "Completed",
            "Closed",
            "Cancel"
        )
        
        val statusDisplayList = listOf(
            "Tất cả",
            "Chưa thanh toán",
            "Đang tuyển",
            "Đang xử lý",
            "Đang hoạt động",
            "Đã hoàn tất",
            "Đã đóng",
            "Đã hủy"
        )
        
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            R.layout.spinner_item_status,
            statusDisplayList
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_status)
        binding.spinnerStatusFilter.adapter = adapter
        binding.spinnerStatusFilter.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = if (position == 0) null else statusList[position]
                jobAdapter.filterByStatus(selectedStatus)
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                jobAdapter.filterByStatus(null)
            }
        }
    }
    
    private fun setupSortButton() {
        var isNewest = true
        
        binding.btnSort.addFadeClickEffect {
            isNewest = !isNewest
            
            // Animate icon rotation
            binding.ivSortIcon.animate()
                .rotation(if (isNewest) 0f else 180f)
                .setDuration(200)
                .start()
            
            if (isNewest) {
                binding.tvSortLabel.text = "Mới nhất"
                jobAdapter.sortByDate(JobAdapter.SortOrder.NEWEST)
            } else {
                binding.tvSortLabel.text = "Cũ nhất"
                jobAdapter.sortByDate(JobAdapter.SortOrder.OLDEST)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}