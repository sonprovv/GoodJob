package com.project.job.ui.policy

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.project.job.base.BaseActivity
import com.project.job.databinding.ActivityPolicyBinding
import com.project.job.ui.loading.LoadingDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PolicyActivity : BaseActivity() {
    private lateinit var binding: ActivityPolicyBinding
    private lateinit var viewModel: PolicyViewModel
    private lateinit var loadingDialog: LoadingDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPolicyBinding.inflate(layoutInflater)
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

        loadingDialog = LoadingDialog(this)

        viewModel = PolicyViewModel()

        binding.ivBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Cấu hình WebView
        setupWebView()

         // Adjust for system bars
        viewModel.getPrivacyPolicy()
        observeViewModel()
    }

    private fun setupWebView() {
        with(binding.wvContent.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false

            // Thêm các setting để hiển thị tốt hơn
            setSupportZoom(true)
            defaultFontSize = 16
        }

        binding.wvContent.webViewClient = android.webkit.WebViewClient()
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
                        binding.wvContent.visibility = android.view.View.GONE
                    } else {
                        // Ẩn ProgressBar khi không còn loading
                        loadingDialog.hide()
                        binding.wvContent.visibility = android.view.View.VISIBLE
                    }
                }
            }
            launch {
                // Collect error
                viewModel.policyData.collectLatest { policyResponse ->
                    policyResponse?.let { response ->
                        if (!response.htmlContent.isNullOrEmpty()) {
                            // Sử dụng htmlContent từ API
                            val htmlContent = response.htmlContent

                            // Tạo HTML hoàn chỉnh với CSS để hiển thị đẹp hơn
                            val styledHtml = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                    <style>
                                        body {
                                            font-family: 'Roboto', sans-serif;
                                            line-height: 1.6;
                                            color: #333;
                                            padding: 8px;
                                            font-size: 16px;
                                        }
                                        h1 {
                                            color: #2c3e50;
                                            font-size: 24px;
                                            margin-bottom: 16px;
                                        }
                                        h2 {
                                            color: #34495e;
                                            font-size: 20px;
                                            margin-top: 20px;
                                            margin-bottom: 12px;
                                        }
                                        h3 {
                                            color: #16a085;
                                            font-size: 18px;
                                            margin-top: 16px;
                                            margin-bottom: 8px;
                                        }
                                        blockquote {
                                            background: #f9f9f9;
                                            border-left: 4px solid #ccc;
                                            margin: 1.5em 10px;
                                            padding: 0.5em 10px;
                                            font-style: italic;
                                        }
                                        ul {
                                            padding-left: 20px;
                                        }
                                        li {
                                            margin-bottom: 8px;
                                        }
                                        hr {
                                            border: 0;
                                            height: 1px;
                                            background: #ddd;
                                            margin: 20px 0;
                                        }
                                        strong {
                                            color: #e74c3c;
                                        }
                                    </style>
                                </head>
                                <body>
                                    $htmlContent
                                </body>
                                </html>
                            """.trimIndent()

                            binding.wvContent.loadDataWithBaseURL(
                                null,
                                styledHtml,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        } else {
                            // Xử lý khi API trả về lỗi
                            val errorHtml = "<p style='text-align:center; color:red;'>Lỗi</p>"
                            binding.wvContent.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null)
                        }
                    }
                }
            }
        }
    }
}