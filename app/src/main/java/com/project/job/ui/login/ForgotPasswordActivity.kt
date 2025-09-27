package com.project.job.ui.login

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.project.job.R
import com.project.job.databinding.ActivityForgotPasswordBinding
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.login.viewmodel.ForgotPasswordViewModel
import com.project.job.utils.hideKeyboard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var viewModel: ForgotPasswordViewModel
    private var currentStep = 1 // 1: Email, 2: Code & Password
    private var code: String = ""
    private lateinit var loadingDialog: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)
        viewModel = ViewModelProvider(this)[ForgotPasswordViewModel::class.java]

        // Configure status bar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setupViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViews() {
        // Show only email field initially
        updateUIForStep(1)

        // Add text change listeners for validation
        binding.edtEmail.addTextChangedListener(createTextWatcher())
        binding.edtCode.addTextChangedListener(createTextWatcher())
        binding.edtPassword.addTextChangedListener(createTextWatcher())
        binding.edtPasswordAgain.addTextChangedListener(createTextWatcher())

        // Set input type for password fields
        binding.edtPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.edtPasswordAgain.inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
    }

    private fun setupClickListeners() {
        // Back button
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        // Continue/Reset Password button
        binding.cardViewBtnLogin.setOnClickListener {
            when (currentStep) {
                1 -> handleEmailStep()
                2 -> handleResetPasswordStep()
            }
        }

        // Set up password toggle for both password fields
        setupPasswordToggle(binding.edtPassword)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPasswordToggle(editText: EditText) {
        editText.apply {
            setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(context, R.drawable.ic_visibility_off),
                null
            )

            setOnTouchListener { _, event ->
                val drawableRight = 2
                if (event.action == android.view.MotionEvent.ACTION_UP) {
                    if (event.rawX >= (right - compoundDrawables[drawableRight].bounds.width())) {
                        togglePasswordVisibility(this)
                        return@setOnTouchListener true
                    }
                }
                false
            }
        }
    }

    private fun togglePasswordVisibility(editText: EditText) {
        val selection = editText.selectionEnd
        
        // Check current input type to determine current state
        val isPasswordVisible = editText.inputType == (android.text.InputType.TYPE_CLASS_TEXT or 
            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)

        // Toggle input type
        editText.inputType = if (isPasswordVisible) {
            android.text.InputType.TYPE_CLASS_TEXT or 
            android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            android.text.InputType.TYPE_CLASS_TEXT or 
            android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }

        // Move cursor to the end
        editText.setSelection(selection)

        // Toggle drawable
        val drawableRes = if (isPasswordVisible) {
            R.drawable.ic_visibility_off
        } else {
            R.drawable.ic_visibility
        }

        editText.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ContextCompat.getDrawable(this, drawableRes),
            null
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.loading.collectLatest { isLoading ->
                if(isLoading) {
                    loadingDialog.show()
                }
                else {
                    loadingDialog.hide()
                }
                binding.cardViewBtnLogin.isEnabled = !isLoading
            }
        }

        lifecycleScope.launch {
            viewModel.success_forgot.collectLatest { success ->
                if (success == true) {
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Đặt lại mật khẩu thành công",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = android.content.Intent(
                        this@ForgotPasswordActivity,
                        LoginActivity::class.java
                    )
                    startActivity(intent)
                    finish()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.code.collectLatest { code ->
                this@ForgotPasswordActivity.code = code ?: "0"
                code?.let {
                    // Move to the next step when code is received
                    currentStep = 2
                    updateUIForStep(currentStep)
                }
            }
        }
    }

    private fun handleEmailStep() {
        val email = binding.edtEmail.text.toString().trim()
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        // Hide keyboard and send email
        binding.edtEmail.hideKeyboard()
        viewModel.sendMailForgotPassword(email)
    }

    private fun handleResetPasswordStep() {
        val codeEnter = binding.edtCode.text.toString().trim()
        val newPassword = binding.edtPassword.text.toString()
        val confirmPassword = binding.edtPasswordAgain.text.toString()

        if (codeEnter.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
            return
        }

        val email = binding.edtEmail.text.toString().trim()
        binding.edtPassword.hideKeyboard()
        viewModel.forgotPassword(email=email, newPassword=newPassword, code=code, confirmPassword=confirmPassword, codeEnter=codeEnter)
    }

    private fun updateUIForStep(step: Int) {
        when (step) {
            1 -> {
                // Show email step
                binding.tvEmail.isVisible = true
                binding.edtEmail.isVisible = true
                binding.tvContentCode.isVisible = true

                // Hide password step
                binding.tvCode.isVisible = false
                binding.edtCode.isVisible = false
                binding.tvPassword.isVisible = false
                binding.edtPassword.isVisible = false
                binding.tvPasswordAgain.isVisible = false
                binding.edtPasswordAgain.isVisible = false

                // Update button text and state
                binding.tvBtnContent.text = "Tiếp tục"
                updateButtonState()

                // Clear fields
                binding.edtCode.text?.clear()
                binding.edtPassword.text?.clear()
                binding.edtPasswordAgain.text?.clear()

                // Request focus on email field
                binding.edtEmail.requestFocus()
            }

            2 -> {
                // Show password step
                binding.tvCode.isVisible = true
                binding.edtCode.isVisible = true
                binding.tvPassword.isVisible = true
                binding.edtPassword.isVisible = true
                binding.tvPasswordAgain.isVisible = true
                binding.edtPasswordAgain.isVisible = true

                // Update button text and state
                binding.tvBtnContent.text = "Đặt lại mật khẩu"
                updateButtonState()

                // Request focus on code field
                binding.edtCode.requestFocus()

                // Show success message for email sent
                Toast.makeText(this, "Mã xác thực đã được gửi đến email của bạn", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun createTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }

            override fun afterTextChanged(s: Editable?) {
                // Enable/disable button based on input
                updateButtonState()
            }
        }
    }

    private fun updateButtonState() {
        val isEnabled = when (currentStep) {
            1 -> binding.edtEmail.text?.isNotBlank() == true &&
                    android.util.Patterns.EMAIL_ADDRESS.matcher(binding.edtEmail.text.toString())
                        .matches()

            2 -> binding.edtCode.text?.isNotBlank() == true &&
                    binding.edtPassword.text?.isNotBlank() == true &&
                    binding.edtPasswordAgain.text?.isNotBlank() == true &&
                    binding.edtPassword.text.toString() == binding.edtPasswordAgain.text.toString() &&
                    binding.edtPassword.text.toString().length >= 6

            else -> false
        }

        binding.cardViewBtnLogin.isEnabled = isEnabled
        binding.cardViewBtnLogin.setCardBackgroundColor(
            ContextCompat.getColor(
                this,
                if (isEnabled) R.color.primary_color else R.color.gray_light
            )
        )
        binding.tvBtnContent.setTextColor(
            ContextCompat.getColor(
                this,
                if (isEnabled) R.color.white else R.color.primary_color
            )
        )
    }

    override fun onBackPressed() {
        if (currentStep == 1) {
            super.onBackPressed()
        } else {
            currentStep = 1
            updateUIForStep(currentStep)
        }
    }
}