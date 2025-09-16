package com.project.job.ui.login

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.UserResponse
import com.project.job.databinding.FragmentLoginBinding
import com.project.job.ui.login.viewmodel.LoginViewModel
import com.project.job.utils.addFadeClickEffect
import com.project.job.utils.getFCMToken
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : BottomSheetDialogFragment() {
    private var loginResultListener: LoginResultListener? = null
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    fun setLoginResultListener(listener: LoginResultListener) {
        this.loginResultListener = listener
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var viewModel: LoginViewModel
    private lateinit var preferencesManager: PreferencesManager
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set style trong onCreate, không cần lặp lại trong onCreateView
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
        preferencesManager = PreferencesManager(requireContext())

        // Cấu hình Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set background bo tròn cho view chính
        view.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_login)

        viewModel = LoginViewModel()

        // Xử lý sự kiện đăng nhập thường
        binding.btnLogin.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            dismiss()
        }

        // Xử lý nút "Đăng ký"
        binding.btnRegister.setOnClickListener {
            val intent = Intent(requireContext(), RegisterActivity::class.java)
            startActivity(intent)
            dismiss()
        }

        // Xử lý nút đóng
        binding.ivClose.setOnClickListener {
            dismiss()
        }

        // Xử lý đăng nhập bằng Google
        binding.ivGoogleLogin.addFadeClickEffect {
            signInWithGoogle()
        }

        observeViewModel()
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        Log.d(TAG, "Starting Firebase auth with Google token")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Get Firebase ID token
                    task.result?.user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val firebaseIdToken = tokenTask.result?.token
                            if (firebaseIdToken != null) {
                                Log.d(TAG, "Firebase ID token obtained successfully")
                                // Send this token to your backend
                                viewModel.loginWithGoogle(firebaseIdToken, "user")
                            } else {
                                Log.e(TAG, "Firebase ID token is null")
                                showError("Failed to get authentication token")
                            }
                        } else {
                            Log.e(TAG, "Failed to get ID token", tokenTask.exception)
                            showError("Authentication failed: ${tokenTask.exception?.message}")
                        }
                    }
                } else {
                    Log.e(TAG, "Firebase auth failed", task.exception)
                    showError("Authentication failed: ${task.exception?.message}")
                }
            }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.loading.collectLatest { isLoading ->
                        // Xử lý trạng thái loading tại đây
                        if (isLoading) {
                            // Hiển thị ProgressBar hoặc trạng thái loading
                            binding.lottieLoader.visibility = View.VISIBLE
                            binding.ivGoogleLogin.isEnabled = false // Vô hiệu hóa nút đăng nhập
                        } else {
                            // Ẩn ProgressBar khi không còn loading
                            binding.lottieLoader.visibility = View.GONE
                            binding.ivGoogleLogin.isEnabled = true // Kích hoạt lại nút đăng nhập
                        }
                    }
                }
                launch {
                    viewModel.user.collect { user ->
                        if (user != null) {
                            Log.d(TAG, "User data received: $user")
                            preferencesManager.saveUser(user)
                            val fcmtoken = getFCMToken().toString()
                            viewModel.postFCMToken(clientID = user.uid, fcmToken = fcmtoken)

                            // Dismiss the fragment when user data is received
                            if (isAdded && !isRemoving) {
                                loginResultListener?.onLoginSuccess()
                                dismiss()
                            }
                        }
                    }
                }

                launch {
                    viewModel.token.collect { token ->
                        if (token != null) {
                            Log.d(TAG, "Token received: $token")
                            preferencesManager.saveAuthToken(token)
                        }
                    }
                }

                // Handle login result
                launch {
                    viewModel.loginResult.collect { authResponse ->
                        authResponse?.let {
                            if (it.success && isAdded && !isRemoving) {
                                // Dismiss the fragment on successful login
                                dismiss()
                            }
                        }
                    }
                }
            }
        }
    }

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { token ->
                firebaseAuthWithGoogle(token)
            } ?: showError("No ID token found")
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign in failed", e)
            showError("Google sign in failed: ${e.statusCode}")
        }
    }

    private fun saveAuthData(authResponse: UserResponse) {
        // Lưu token và thông tin người dùng vào SharedPreferences
        preferencesManager.saveAuthToken(authResponse.data.token)
        authResponse.data?.user?.let { user ->
            // Lưu thông tin người dùng
            preferencesManager.saveUser(user)

            // Kiểm tra xem người dùng đã hoàn thiện hồ sơ chưa
            // Chúng ta coi là hoàn thiện nếu có tên và email
            val isProfileComplete = !user.username.isNullOrEmpty() && !user.email.isNullOrEmpty()
            if (!isProfileComplete) {
                // TODO: Chuyển đến màn hình cập nhật thông tin
                showError("Vui lòng cập nhật thông tin cá nhân")
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        // TODO: Hiển thị/hủy hiển thị loading
        // Ví dụ: progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        // Kiểm tra xem user đã đăng nhập chưa
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User đã đăng nhập, có thể chuyển đến MainActivity
            // hoặc không làm gì để giữ dialog mở
            Log.d(TAG, "User already signed in: ${currentUser.email}")
        }
    }

    companion object {
        private const val TAG = "LoginFragment"

        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}