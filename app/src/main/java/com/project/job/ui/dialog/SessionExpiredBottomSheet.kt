package com.project.job.ui.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.project.job.MainActivity
import com.project.job.databinding.BottomSheetSessionExpiredBinding
import com.project.job.data.source.local.PreferencesManager

class SessionExpiredBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSessionExpiredBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSessionExpiredBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOk.setOnClickListener {
            logoutAndRedirectToLogin()
        }

        // Không thể tắt dialog bằng cách nhấn ngoài
        isCancelable = false
    }

    private fun logoutAndRedirectToLogin() {
        val context = requireContext()
        val preferencesManager = PreferencesManager(context)

        // Clear tất cả auth data
        preferencesManager.clearAuthData()

        // Chuyển về MainActivity với HomeFragment (ActivityFragment sẽ tự động hiển thị)
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // Đóng dialog
        dismiss()

        // Finish current activity if it's not already finishing
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
