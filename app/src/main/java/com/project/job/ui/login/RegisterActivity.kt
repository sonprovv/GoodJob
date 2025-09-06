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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.project.job.MainActivity
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivityRegisterBinding
import com.project.job.ui.login.viewmodel.RegisterViewModel
import com.project.job.utils.addFadeClickEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        viewModel = RegisterViewModel()
        preferencesManager = PreferencesManager(this)

        setupPasswordToggle(binding.edtPassword)

        binding.ivBack.addFadeClickEffect {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.cardViewBtnRegister.setOnClickListener {
            // Xử lý sự kiện đăng ký
            val username = binding.edtName.text.toString()
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPassword = binding.edtConfirmPassword.text.toString()
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                if(password != confirmPassword){
                    binding.edtConfirmPassword.error = getString(R.string.password_not_match)
                }
                binding.edtName.error = getString(R.string.empty_field)
                binding.edtEmail.error = getString(R.string.empty_field)
                binding.edtPassword.error = getString(R.string.empty_field)
            }
            else {
                viewModel.register(email, password, username)
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
            // Collect loading
            launch {
                viewModel.loading.collectLatest { isLoading ->
                    // Xử lý trạng thái loading tại đây
                    if (isLoading) {
                        // Hiển thị ProgressBar hoặc trạng thái loading
                        binding.lottieLoader.visibility = View.VISIBLE
                        binding.cardViewBtnRegister.isEnabled = false // Vô hiệu hóa nút đăng nhập
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        binding.lottieLoader.visibility = View.GONE
                        binding.cardViewBtnRegister.isEnabled = true // Kích hoạt lại nút đăng nhập
                    }
                }
            }
            // Collect user data
            launch {
                viewModel.user.collectLatest { user ->
                    Log.e("LoginActivity", "User: $user")

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
                        // Chuyển đến MainActivity
                        val intent =
                            android.content.Intent(this@RegisterActivity, MainActivity::class.java)
                        intent.flags =
                            android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }
}