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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WorkerOrderFragment : Fragment() {
    private var _binding: FragmentWorkerOrderBinding? = null
    private val binding get() = _binding!!
    private lateinit var workerAdapter: WorkerAdapter
    private var jobId: String? = null
    private lateinit var viewModel: JobDetailViewModel
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

        workerAdapter = WorkerAdapter()
        binding.rcvListWorker.adapter = workerAdapter
        
        // Use jobId here to load worker data
        jobId?.let { id ->
           viewModel.getListWorker(token, id)
        }

        observeViewModel()

    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Collect loading
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}