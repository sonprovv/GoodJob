package com.project.job.ui.service.cleaningservice

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.User
import com.project.job.databinding.FragmentUpdateNameAndPhoneBinding
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.profile.viewmodel.UpdateProfileViewModel
import com.project.job.utils.UserDataBroadcastManager
import kotlinx.coroutines.launch

class UpdateNameAndPhoneFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentUpdateNameAndPhoneBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var viewModel: UpdateProfileViewModel
    private lateinit var loadingDialog: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateNameAndPhoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.window?.let { window ->
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                // Set background transparent cho bottom sheet container
                it.background = null

                // Thiết lập behavior
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true

                // Cho phép bottom sheet mở rộng khi bàn phím hiện
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })
            }
        }

        // Set background transparent cho window
        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }

        return dialog
    }

    private fun updateButtonState(phonePre: String?, namePre: String?) {
        val phone = binding.edtPhone.text.toString()
        val name = binding.edtName.text.toString()

        val isPhoneValid = phone.length >= 10
        val isNameValid = name.length >= 3
        val isChanged = phone != phonePre || name != namePre

        val shouldEnable = isPhoneValid && isNameValid && isChanged

        binding.btnSave.isEnabled = shouldEnable
        if (shouldEnable) {
            binding.btnSave.setBackgroundColor(resources.getColor(R.color.xanh))
            binding.btnSave.setTextColor(resources.getColor(R.color.white))
        } else {
            binding.btnSave.setBackgroundColor(resources.getColor(R.color.gray_light))
            binding.btnSave.setTextColor(resources.getColor(R.color.black))
        }
    }

    private fun setupTextWatchers(phonePre: String?, namePre: String?) {
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateButtonState(phonePre, namePre)
            }
        }

        binding.edtPhone.addTextChangedListener(textWatcher)
        binding.edtName.addTextChangedListener(textWatcher)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = UpdateProfileViewModel()
        preferencesManager = PreferencesManager(requireContext())
        loadingDialog = LoadingDialog(requireActivity())
        // Observe ViewModel states
        observeViewModel()
        
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        val phonePre = preferencesManager.getUserData()["user_phone"]
        val namePre = preferencesManager.getUserData()["user_name"]

        binding.edtPhone.setText(phonePre)
        binding.edtName.setText(namePre)

        // Set up text watchers for real-time validation
        setupTextWatchers(phonePre, namePre)

        // Initial button state
        updateButtonState(phonePre, namePre)

        binding.btnSave.setOnClickListener {
            val phone = binding.edtPhone.text.toString()
            val name = binding.edtName.text.toString()
            
            val userpre = preferencesManager.getUserData()
            val user = User(
                uid = userpre["user_id"] ?: "",
                email = userpre["user_email"] ?: "",
                username = name,
                avatar = userpre["user_avatar"] ?: "",
                role = userpre["user_role"] ?: "user",
                location = userpre["user_location"] ?: "",
                gender = userpre["user_gender"] ?: "",
                dob = userpre["user_birthdate"] ?: "",
                tel = phone,
                provider = userpre["user_provider"] ?: ""
            )
            val token = preferencesManager.getAuthToken() ?: ""
            if (token.isNotEmpty()) {
                viewModel.updateProfile(user)
            } else {
                Toast.makeText(requireContext(), "Hết phiên đăng nhập, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.success_change.collect { success ->
                if (success) {
                    // Update local preferences with new data
                    val phone = binding.edtPhone.text.toString()
                    val name = binding.edtName.text.toString()
                    preferencesManager.saveNameAndPhone(name, phone)
                    
                    // Send broadcast to notify other screens
                    UserDataBroadcastManager.sendUserDataUpdatedBroadcast(
                        requireContext(), 
                        name, 
                        phone
                    )
                    
                    Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.loading.collect { loading ->
                binding.btnSave.isEnabled = !loading
                if (loading) {
                    loadingDialog.show()
                    binding.btnSave.text = "Đang cập nhật..."
                } else {
                    loadingDialog.hide()
                    binding.btnSave.text = "Lưu"
                }
            }
        }
    }


}