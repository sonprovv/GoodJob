package com.project.job.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.project.job.MainActivity
import com.project.job.R
import com.project.job.base.BaseActivity
import com.project.job.data.repository.TokenRepository
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivityRegisterBinding
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.login.viewmodel.LoginViewModel
import com.project.job.ui.login.viewmodel.RegisterViewModel
import com.project.job.utils.addFadeClickEffect
import com.project.job.utils.ErrorHandler
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterActivity : BaseActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private lateinit var viewModelLogin : LoginViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferencesManager = PreferencesManager(this)
        loadingDialog = LoadingDialog(this)

        // Thiết lập màu sắc cho status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.parseColor("#FFFFFF") // Màu nền status bar
        }

        // Đặt icon sáng/tối cho status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Icon sáng cho nền tối
            // Nếu muốn icon tối cho nền sáng, bỏ dòng trên hoặc dùng:
            // window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }

        viewModel = RegisterViewModel(tokenRepository = TokenRepository(preferencesManager))
        viewModelLogin = LoginViewModel(tokenRepository = TokenRepository(preferencesManager))


        setupPasswordToggle(binding.edtPassword)
        setupPasswordToggle(binding.edtConfirmPassword)

        binding.ivBack.addFadeClickEffect {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.cardViewBtnRegister.setOnClickListener {
            // Xử lý sự kiện đăng ký
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPassword = binding.edtConfirmPassword.text.toString()
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                if(password != confirmPassword){
                    binding.edtConfirmPassword.error = getString(R.string.password_not_match)
                }
//                binding.edtName.error = getString(R.string.empty_field)
                binding.edtEmail.error = getString(R.string.empty_field)
                binding.edtPassword.error = getString(R.string.empty_field)
            }
            else {
                val fcmToken = preferencesManager.getFCMToken() ?: ""
                viewModel.register(email=email, password=password, confirmPassword=confirmPassword, fcmToken=fcmToken)
            }
        }

        binding.tvLogin.addFadeClickEffect {
            // Xử lý sự kiện chuyển sang màn hình đăng nhập
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.tvForgotPassword.addFadeClickEffect {
            // Xử lý sự kiện quên mật khẩu
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
            finish()
        }
        observeViewModel()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPasswordToggle(editText: EditText) {
        editText.apply {
            // Sử dụng AppCompatResources để tự động áp dụng theme tint
            val drawable = androidx.appcompat.content.res.AppCompatResources.getDrawable(
                context, 
                R.drawable.ic_visibility_off
            )
            
            setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                drawable,
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

        // Toggle drawable using AppCompatResources for automatic theme tinting
        val drawableRes = if (isPasswordVisible) {
            R.drawable.ic_visibility_off
        } else {
            R.drawable.ic_visibility
        }

        val drawable = androidx.appcompat.content.res.AppCompatResources.getDrawable(
            this, 
            drawableRes
        )

        editText.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            drawable,
            null
        )
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Collect loading
            launch {
                viewModel.loading.collectLatest { isLoading ->
                    // Xử lý trạng thái loading tại đây
                    if (isLoading) {
                        // Hiển thị ProgressBar hoặc trạng thái loading
                        loadingDialog.show()
                        binding.cardViewBtnRegister.isEnabled = false // Vô hiệu hóa nút đăng nhập
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        loadingDialog.hide()
                        binding.cardViewBtnRegister.isEnabled = true // Kích hoạt lại nút đăng nhập
                    }
                }
            }
            // Collect user data
            launch {
                viewModel.user.collectLatest { user ->
                    Log.e("RegisterActivity", "User: $user")

                    if (user != null) {
                        // Lưu user vào SharedPreferences
                        preferencesManager.saveUser(user)
                    }
                }
            }

            // Collect token in parallel
            launch {
                viewModel.token.collectLatest { token ->
                    if (token != null) {
                        // Lưu token vào SharedPreferences
                        preferencesManager.saveAuthToken(token)

                        viewModelLogin.postFCMToken(fcmToken = preferencesManager.getFCMToken() ?: "")

                        // Chuyển đến MainActivity
                        val intent =
                            Intent(this@RegisterActivity, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }

            // Collect error messages
            launch {
                viewModel.error.collectLatest { errorMessage ->
                    if (!errorMessage.isNullOrEmpty()) {
                        // Xử lý error message qua ErrorHandler
                        val processedError = ErrorHandler.handleRegisterError(errorMessage)
                        showErrorMessage(processedError, errorMessage)
                        Log.e("RegisterActivity", "Register error: $errorMessage")
                    }
                }
            }
        }
    }

    private fun showErrorMessage(processedError: String, originalError: String = processedError) {
        // Hiển thị error với Snackbar có retry action
        val snackbar = Snackbar.make(binding.root, processedError, Snackbar.LENGTH_LONG)
        
        // Thêm action button dựa trên loại lỗi
        when {
            originalError.contains("The email address is already in use by another account") ||
            originalError.contains("Email đã được đăng ký") -> {
                snackbar.setAction("Đăng nhập") {
                    // Navigate to login screen
                    val intent = Intent(this, LoginActivity::class.java)
                    // Pre-fill email if available
                    val email = binding.edtEmail.text.toString().trim()
                    if (email.isNotEmpty()) {
                        intent.putExtra("prefill_email", email)
                    }
                    startActivity(intent)
                    finish()
                }
            }
            originalError.contains("kết nối") || originalError.contains("mạng") -> {
                snackbar.setAction("Thử lại") {
                    retryRegister()
                }
            }
            originalError.contains("máy chủ") -> {
                snackbar.setAction("Thử lại") {
                    retryRegister()
                }
            }
            originalError.contains("Mật khẩu không trùng khớp") || 
            processedError.contains("Mật khẩu xác nhận không khớp") -> {
                snackbar.setAction("OK") {
                    // Focus on confirm password field
                    binding.edtConfirmPassword.requestFocus()
                }
            }
            ErrorHandler.isAuthError(originalError) -> {
                snackbar.setAction("OK") {
                    // Just dismiss
                }
            }
            else -> {
                snackbar.setAction("Thử lại") {
                    retryRegister()
                }
            }
        }
        
        snackbar.show()
    }
    
    private fun retryRegister() {
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()
        val confirmPassword = binding.edtConfirmPassword.text.toString().trim()
        
        if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
            val fcmToken = preferencesManager.getFCMToken() ?: ""
            viewModel.register(email = email, password = password, confirmPassword = confirmPassword, fcmToken = fcmToken)
        } else {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
        }
    }
}