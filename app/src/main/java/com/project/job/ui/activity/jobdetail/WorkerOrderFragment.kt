package com.project.job.ui.activity.jobdetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.FragmentWorkerOrderBinding
import com.project.job.ui.activity.jobdetail.adapter.WorkerAdapter
import com.project.job.ui.activity.jobdetail.viewmodel.JobDetailViewModel
import com.project.job.ui.activity.jobdetail.viewmodel.ChoideWorkerViewModel
import com.project.job.ui.reviewworker.ReviewWorkerFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WorkerOrderFragment : Fragment() {
    private var _binding: FragmentWorkerOrderBinding? = null
    private val binding get() = _binding!!
    private lateinit var workerAdapter: WorkerAdapter
    private var jobId: String? = null
    private lateinit var viewModel: JobDetailViewModel
    private lateinit var choideWorkerViewModel: ChoideWorkerViewModel
    private lateinit var preferencesManager: PreferencesManager

    companion object {
        private const val ARG_JOB_ID = "job_id"
        
        fun newInstance(jobId: String): WorkerOrderFragment {
            val fragment = WorkerOrderFragment()
            val args = Bundle()
            args.putString(ARG_JOB_ID, jobId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            jobId = it.getString(ARG_JOB_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkerOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        val token = preferencesManager.getAuthToken()?:""
        viewModel = JobDetailViewModel()
        choideWorkerViewModel = ChoideWorkerViewModel()

        workerAdapter = WorkerAdapter(
            viewModel = choideWorkerViewModel,
            token = token,
            lifecycleOwner = this,
            onWorkerStatusChanged = {
                // Refresh data sau khi thay đổi status worker (accept/reject)
                jobId?.let { id ->
                    viewModel.getListWorker(token, id)
                }
            },
            onViewDetailClicked = { worker ->
                // Navigate to ReviewWorkerFragment
                navigateToReviewWorkerFragment(worker)
            },
            preferencesManager = preferencesManager
        )
        binding.rcvListWorker.adapter = workerAdapter
        
        // Use jobId here to load worker data
        jobId?.let { id ->
           viewModel.getListWorker(token, id)
        }

        observeViewModel()

    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Collect loading từ JobDetailViewModel
            launch {
                viewModel.loading.collectLatest { isLoading ->
                    // Xử lý trạng thái loading tại đây
                    if (isLoading) {
                        // Hiển thị ProgressBar hoặc trạng thái loading
                        binding.flLottieLoader.visibility = View.VISIBLE
                        binding.llNoData.visibility = View.GONE
                        binding.llListWorker.visibility = View.GONE
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        binding.flLottieLoader.visibility = View.GONE
                    }
                }
            }
            
            // Collect loading từ ChoideWorkerViewModel
            launch {
                choideWorkerViewModel.loading.collectLatest { isLoading ->
                    if (isLoading) {
                        // Hiển thị loading khi đang xử lý choice worker
                        binding.flLottieLoader.visibility = View.VISIBLE
                        binding.llListWorker.isEnabled = false // Disable interaction
                    } else {
                        // Ẩn loading khi hoàn thành
                        binding.flLottieLoader.visibility = View.GONE
                        binding.llListWorker.isEnabled = true // Enable interaction
                    }
                }
            }
            
            // Collect success state từ ChoideWorkerViewModel
            launch {
                choideWorkerViewModel.success_change.collectLatest { success ->
                    success?.let {
                        if (it) {
                            // Refresh data sau khi thành công
                            val token = preferencesManager.getAuthToken() ?: ""
                            jobId?.let { id ->
                                viewModel.getListWorker(token, id)
                            }
                        }
                    }
                }
            }
            
            launch {
                viewModel.workers.collectLatest { worker ->
                    if (worker != null && worker.isNotEmpty()) {
                        // Có data: hiện list, ẩn no data
                        binding.llNoData.visibility = View.GONE
                        binding.llListWorker.visibility = View.VISIBLE
                        workerAdapter.submitList(worker)
                    } else {
                        // Không có data: ẩn list, hiện no data
                        binding.llListWorker.visibility = View.GONE
                        binding.llNoData.visibility = View.VISIBLE
                        workerAdapter.submitList(emptyList())
                    }
                }
            }
        }
    }

    private fun navigateToReviewWorkerFragment(worker: com.project.job.data.source.remote.api.response.WorkerOrder) {
        val reviewWorkerFragment = ReviewWorkerFragment()
        
        // Có thể truyền data worker qua Bundle nếu cần
        val bundle = Bundle().apply {
            putString("worker_id", worker.uid)
            putString("worker_name", worker.worker.username)
            putString("worker_birthdate", worker.worker.dob)
            putString("worker_phone", worker.worker.tel)
            putString("worker_location", worker.worker.location)
            putString("worker_email", worker.worker.email)
            putString("worker_gender", worker.worker.gender)
            putString("worker_status", worker.status)
            putString("worker_avatar", worker.worker.avatar)
            putString("worker_description", worker.worker.description)
        }
        reviewWorkerFragment.arguments = bundle
        
        // Navigate using Activity's supportFragmentManager and replace the entire activity content
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, reviewWorkerFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}