package com.project.job.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.project.job.OnMainCallBack
import com.project.job.OnMainCallBack2
import java.lang.reflect.Constructor
import java.util.Objects
import androidx.navigation.fragment.findNavController
import com.project.job.R

open class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    // Activity navigation methods
    protected fun startActivityWithAnimation(intent: Intent) {
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    // Fragment navigation methods với animation
    protected fun replaceFragmentWithAnimation(
        fragment: Fragment,
        tag: String,
        containerId: Int = R.id.fragment_container // Thay bằng id container của bạn
    ) {
        // Get container view
        val container = requireActivity().findViewById<View>(containerId)
        
        // Post để đảm bảo container đã layout xong trước khi animation
        container?.post {
            parentFragmentManager.beginTransaction().apply {
                setCustomAnimations(
                    R.anim.fragment_slide_in_right,
                    R.anim.fragment_slide_out_left,
                    R.anim.fragment_slide_in_left,
                    R.anim.fragment_slide_out_right
                )
                replace(containerId, fragment, tag)
                addToBackStack(tag)
                commit()
            }
        } ?: run {
            // Fallback nếu không tìm thấy container
            parentFragmentManager.beginTransaction().apply {
                setCustomAnimations(
                    R.anim.fragment_slide_in_right,
                    R.anim.fragment_slide_out_left,
                    R.anim.fragment_slide_in_left,
                    R.anim.fragment_slide_out_right
                )
                replace(containerId, fragment, tag)
                addToBackStack(tag)
                commit()
            }
        }
    }

    // Navigation component methods
    protected fun navigateTo(destinationId: Int, args: Bundle? = null) {
        findNavController().navigate(destinationId, args)
    }

    protected fun navigateBack() {
        if (!findNavController().popBackStack()) {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    protected fun showLoading() {
        // Common loading implementation
    }

    protected fun hideLoading() {
        // Common loading hide implementation
    }

    protected fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}