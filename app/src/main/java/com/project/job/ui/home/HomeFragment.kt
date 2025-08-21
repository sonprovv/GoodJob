package com.project.job.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.project.job.R
import com.project.job.databinding.FragmentHomeBinding
import com.project.job.ui.login.LoginFragment

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Xử lý sự kiện khi người dùng nhấn nút đăng nhập
        binding.cardViewButtonLogin.setOnClickListener {
            // Mở LoginFragment
            val loginFragment = LoginFragment.newInstance()
            loginFragment.show(parentFragmentManager, "LoginFragment")
        }


    }


}