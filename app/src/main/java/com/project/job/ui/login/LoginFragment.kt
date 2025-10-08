package com.project.job.ui.login

import android.app.Application
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
import com.project.job.JobApplication
import com.project.job.MainActivity
import com.project.job.R
import com.project.job.data.repository.TokenRepository
import com.project.job.data.source.local.PreferencesManager
import com.project.job.data.source.remote.api.response.UserResponse
import com.project.job.databinding.FragmentLoginBinding
import com.project.job.ui.loading.LoadingDialog
import com.project.job.ui.login.viewmodel.LoginViewModel
import com.project.job.utils.TokenManager
import com.project.job.utils.UserDataBroadcastManager
import com.project.job.utils.addFadeClickEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class LoginFragment : BottomSheetDialogFragment() {
    private var loginResultListener: LoginResultListener? = null
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var loadingDialog: LoadingDialog
    fun setLoginResultListener(listener: LoginResultListener) {
        this.loginResultListener = listener
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var viewModel: LoginViewModel
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var tokenManager: TokenManager
    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private var fcmToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set style trong onCreate, không cần lặp lại trong onCreateView
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)

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
        preferencesManager = PreferencesManager(requireContext())
        tokenManager = TokenManager.getInstance(requireContext())
        fcmToken = preferencesManager.getFCMToken() ?: ""
        Log.d(TAG, "FCM Token: ${fcmToken}")
        viewModel = LoginViewModel(tokenRepository = TokenRepository(preferencesManager))
        loadingDialog = LoadingDialog(requireActivity())
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
                    Log.d(TAG, "Firebase authentication successful")
                    
                    // Sử dụng TokenManager để lấy Firebase ID token
                    lifecycleScope.launch {
                        try {
                            val firebaseIdToken = tokenManager.getCurrentFirebaseToken(forceRefresh = false)
                            if (firebaseIdToken != null) {
                                Log.d(TAG, "Firebase ID token obtained via TokenManager")
                                // Gửi token này đến backend
                                viewModel.loginWithGoogle(idToken, fcmToken = fcmToken)
                            } else {
                                Log.e(TAG, "Failed to get Firebase ID token via TokenManager")
                                showError("Failed to get authentication token")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error getting Firebase token via TokenManager", e)
                            showError("Authentication failed: ${e.message}")
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
                            loadingDialog.show()
                            binding.ivGoogleLogin.isEnabled = false // Vô hiệu hóa nút đăng nhập
                        } else {
                            // Ẩn ProgressBar khi không còn loading
                            loadingDialog.hide()
                            binding.ivGoogleLogin.isEnabled = true // Kích hoạt lại nút đăng nhập
                        }
                    }
                }
                launch {
                    viewModel.user.collect { user ->
                        if (user != null) {
                            Log.d(TAG, "User data received: $user")
                            preferencesManager.saveUser(user)

                            // Gửi broadcast thông báo cập nhật dữ liệu người dùng
                            val userName = user.username ?: "Người dùng"
                            val userPhone = user.tel ?: ""
                            UserDataBroadcastManager.sendUserDataUpdatedBroadcast(
                                requireContext(),
                                userName,
                                userPhone
                            )

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

                launch {
                    viewModel.refreshToken.collect { refreshToken ->
                        if (refreshToken != null) {
                            Log.d(TAG, "Backend refresh token received: ${refreshToken.take(10)}...")
                            preferencesManager.saveRefreshToken(refreshToken)
                            
                            // Log trạng thái tokens để debug
                            Log.d(TAG, "All tokens saved. Current status:\n${preferencesManager.getTokensInfo()}")
                        }
                    }
                }

                // Handle error messages
                launch {
                    viewModel.error.collect { errorMessage ->
                        if (!errorMessage.isNullOrEmpty()) {
                            Log.e(TAG, "Error received: $errorMessage")
                            showError(errorMessage)
                            // Re-enable Google login button after error
                            binding.ivGoogleLogin.isEnabled = true
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
//            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
//            val account = task.getResult(ApiException::class.java)
            val account = GoogleSignIn.getLastSignedInAccount(requireContext())
                ?: throw ApiException(com.google.android.gms.common.api.Status.RESULT_CANCELED)
            // 1. Dùng ID Token login Firebase
            account.idToken?.let { token ->
                Log.d(TAG, "idTokenPP: ${token}")
                firebaseAuthWithGoogle(token)
            } ?: showError("No ID token found")

            // 2. Lưu thông tin Google account (không cần exchange tokens)
            account.serverAuthCode?.let { authCode ->
                Log.d(TAG, "Server auth code received: ${authCode.take(10)}...")
                // Lưu auth code để có thể sử dụng sau này nếu cần
                // Nhưng không exchange ngay vì cần client_secret
                Log.d(TAG, "Server auth code saved for potential backend exchange")
            }

        } catch (e: ApiException) {
            Log.e(TAG, "Google sign in failed", e)
            showError("Google sign in failed: ${e.statusCode}")
        }
    }

    /**
     * ⚠️ KHÔNG SỬ DỤNG PHƯƠNG THỨC NÀY TRONG PRODUCTION
     * 
     * Lý do: Client Secret không nên được hardcode trong Android app vì:
     * 1. Không an toàn - có thể bị reverse engineer
     * 2. Google không khuyến khích sử dụng client secret trong mobile apps
     * 3. Firebase ID Token đã đủ để authenticate với backend
     * 
     * Thay vào đó:
     * - Sử dụng Firebase ID Token để authenticate với backend
     * - Backend sẽ verify Firebase token và tạo session riêng
     * - Hoặc backend exchange auth code với client secret (an toàn hơn)
     */
    private fun exchangeAuthCodeForTokens_DEPRECATED(authCode: String): TokenResponse? {
        // KHÔNG SỬ DỤNG - Chỉ để tham khảo
        return null
    }

    data class TokenResponse(
        val accessToken: String,
        val refreshToken: String?,
        val expiresIn: Long
    )

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