package com.project.job.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.HealthcareService
import com.project.job.databinding.FragmentActivityBinding
import com.project.job.ui.activity.adapter.JobAdapter
import com.project.job.ui.activity.history.HistoryActivity
import com.project.job.ui.service.healthcareservice.HealthCareViewModel
import com.project.job.utils.addFadeClickEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ActivityFragment : Fragment() {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel : ActivityViewModel
    private lateinit var viewModelHealthcare : HealthCareViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var jobAdapter: JobAdapter
    private var healthcareServiceList : List<HealthcareService> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using view binding
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ActivityViewModel()
        viewModelHealthcare = HealthCareViewModel()
        preferencesManager = PreferencesManager(requireContext())

        // Initialize adapter
        jobAdapter = JobAdapter()

        // Setup RecyclerView
        binding.rcvListJob.adapter = jobAdapter
        binding.rcvListJob.layoutManager = LinearLayoutManager(requireContext())
        
        // Initialize with empty lists
        jobAdapter.updateList(emptyList(), emptyList())

        val token = preferencesManager.getAuthToken() ?: ""
        val uid = preferencesManager.getUserData()["user_id"] ?: ""

        // Kiểm tra xem token có tồn tại hay không
        if (token != "") {
            viewModel.getListJob(token = token, uid = uid)
            viewModelHealthcare.getServiceHealthcare()
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

        observeViewModel()
    }

    private fun observeViewModel(){
        lifecycleScope.launch {
            // Collect loading
            launch {
                viewModel.loading.collectLatest { isLoading ->
                    // Xử lý trạng thái loading tại đây
                    if (isLoading) {
                        // Hiển thị ProgressBar hoặc trạng thái loading
                        binding.flLottieLoader.visibility = View.VISIBLE
                        binding.llListJob.visibility = View.GONE
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        binding.flLottieLoader.visibility = View.GONE
                        binding.llListJob.visibility = View.VISIBLE
                    }
                }
            }

            launch {
                viewModelHealthcare.loading.collectLatest { isLoading ->
                    // Xử lý trạng thái loading tại đây
                    if (isLoading) {
                        // Hiển thị ProgressBar hoặc trạng thái loading
                        binding.flLottieLoader.visibility = View.VISIBLE
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        binding.flLottieLoader.visibility = View.GONE
                    }
                }
            }
            // Collect jobs and update adapter
            launch {
                viewModel.jobs.collectLatest { listJob ->
                    if(listJob == null || listJob.isEmpty()){
                        binding.llLoginSuccessNoData.visibility = View.VISIBLE
                        binding.llListJob.visibility = View.GONE
                    }
                    else {
                        binding.llLoginSuccessNoData.visibility = View.GONE
                        binding.llListJob.visibility = View.VISIBLE
                        jobAdapter.updateList(listJob, healthcareServiceList)
                    }
                }
            }

            launch {
                viewModelHealthcare.healthcareService.collectLatest { listService ->
                    healthcareServiceList = listService.filterNotNull()
                    // Update the adapter with the latest services
                    jobAdapter.updateList(jobAdapter.jobList, healthcareServiceList)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }
}