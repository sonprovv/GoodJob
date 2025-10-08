package com.project.job.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.utils.Utils
import com.project.job.R
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.FragmentHomeBinding
import com.project.job.ui.home.adapter.BannerAdapter
import com.project.job.ui.login.LoginFragment
import com.project.job.ui.login.LoginResultListener
import com.project.job.ui.map.MapActivity
import com.project.job.ui.notification.NotificationActivity
import com.project.job.ui.service.cleaningservice.SelectServiceActivity
import com.project.job.ui.service.healthcareservice.SelectServiceHealthCareActivity
import com.project.job.ui.service.maintenanceservice.SelectServiceMaintenanceActivity
import com.project.job.utils.UserDataBroadcastManager

class HomeFragment : Fragment(), LoginResultListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferencesManager: PreferencesManager

    private val userDataUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == UserDataBroadcastManager.ACTION_USER_DATA_UPDATED) {
                val name = intent.getStringExtra(UserDataBroadcastManager.EXTRA_USER_NAME) ?: ""
                val phone = intent.getStringExtra(UserDataBroadcastManager.EXTRA_USER_PHONE) ?: ""
                updateUserDataInUI(name, phone)
            }
        }
    }

    private val photoList = listOf(
        R.drawable.img_banner_1,
        R.drawable.img_banner_2,
        R.drawable.img_banner_3,
        R.drawable.img_banner_4,
        R.drawable.img_banner_5
    )
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        if (photoList.isNotEmpty()) {
            val currentItem = binding.viewPager.currentItem
            binding.viewPager.setCurrentItem(
                if (currentItem == photoList.size - 1) 0 else currentItem + 1,
                true
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())

        binding.ivNotification.setOnClickListener {
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            startActivity(intent)
        }

        // Register broadcast receiver
        registerUserDataReceiver()
        
        // Handle login button click
        binding.cardViewButtonLogin.setOnClickListener {
            val loginFragment = LoginFragment()
            loginFragment.setLoginResultListener(this)
            loginFragment.show(parentFragmentManager, "LoginFragment")
        }
        
        // Check login status
        checkLoginStatus()

        setupViewPager()

        // Xử lý click Google Map
        binding.llItemService1.setOnClickListener {
            val location = preferencesManager.getUserData()["user_location"]
            if(location == "" || location == null || location == "Chưa cập nhật"){
                val intent = Intent(requireContext(), MapActivity::class.java).apply {
                    putExtra("source", "cleaning_service")
                }
                startActivity(intent)
                return@setOnClickListener
            }
            else {
                val intent = Intent(requireContext(), SelectServiceActivity::class.java)
                startActivity(intent)
            }
        }


        binding.llItemService2.setOnClickListener {
            val location = preferencesManager.getUserData()["user_location"]
            if(location == "" || location == null || location == "Chưa cập nhật"){
                val intent = Intent(requireContext(), MapActivity::class.java).apply {
                    putExtra("source", "healthcare_service")
                }
                startActivity(intent)
                return@setOnClickListener
            }
            else {
                val intent = Intent(requireContext(), SelectServiceHealthCareActivity::class.java)
                startActivity(intent)
            }
        }
        binding.llItemService3.setOnClickListener {
            val location = preferencesManager.getUserData()["user_location"]
            if(location == "" || location == null || location == "Chưa cập nhật"){
                val intent = Intent(requireContext(), MapActivity::class.java).apply {
                    putExtra("source", "maintenance_service")
                }
                startActivity(intent)
                return@setOnClickListener
            }
            else {
                val intent = Intent(requireContext(), SelectServiceMaintenanceActivity::class.java)
                startActivity(intent)
            }
        }
        checkLoginStatus()
    }

    private fun setupViewPager() {
        // Cấu hình ViewPager2 để hiển thị 2 items: ảnh chính + ảnh con bên phải
        binding.viewPager.apply {
            offscreenPageLimit = 1
            clipToPadding = false
            clipChildren = false
        }

        // Đảm bảo parent containers không clip children
        val parentView = binding.viewPager.parent as? ViewGroup
        parentView?.clipChildren = false

        val grandParentView = parentView?.parent as? ViewGroup
        grandParentView?.clipChildren = false

        // Tính toán kích thước và spacing
        val screenWidth = resources.displayMetrics.widthPixels
        val margin = (screenWidth * 0.05f).toInt() // 5% margin từ lề màn hình
        val nextItemWidth = (screenWidth * 0.15f).toInt() // Ảnh con chiếm 20% màn hình
        val spacing = -(screenWidth * 0.05f).toInt() // Khoảng cách giữa 2 ảnh

        // Padding: margin trái + khoảng cách cho ảnh con + spacing
        binding.viewPager.setPadding(
            margin, // Margin từ lề trái
            0,
            margin + nextItemWidth + spacing, // Margin phải + width ảnh con + khoảng cách
            0
        )

        // PageTransformer để hiển thị 2 ảnh cạnh nhau không có hiệu ứng zoom
        binding.viewPager.setPageTransformer { page, position ->
            when {
                // Ảnh chính (current item)
                position == 0f -> {
                    page.alpha = 1f
                    page.scaleX = 1f
                    page.scaleY = 1f
                    page.translationX = 0f
                    page.elevation = 8f
                    page.visibility = View.VISIBLE
                }
                // Ảnh con bên phải (next item)
                position > 0f && position <= 1f -> {
                    page.alpha = 1f
                    page.scaleX = 0.85f  // Giảm kích thước ảnh con
                    page.scaleY = 0.85f  // Giảm kích thước ảnh con

                    // Tính toán vị trí cố định cho ảnh con bên phải
                    val mainImageWidth = page.width
                    val childImageWidth = mainImageWidth * 0.95f
                    // Tăng khoảng cách giữa ảnh chính và ảnh con
                    val totalSpacing = (spacing * 1f) + (mainImageWidth - childImageWidth) / 2

                    // Di chuyển ảnh con ra xa hơn về bên phải
                    page.translationX = -totalSpacing * 0.5f

                    page.elevation = 4f
                    page.visibility = View.VISIBLE
                }
                // Ẩn các ảnh khác
                else -> {
                    page.alpha = 0f
                    page.visibility = View.INVISIBLE
                }
            }
        }

        // Thiết lập adapter và indicator
        binding.viewPager.adapter = BannerAdapter(photoList)
        binding.indicator.setViewPager(binding.viewPager)

        // Tự động chuyển slide
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                if (photoList.isNotEmpty()) {
                    handler.postDelayed(runnable, 3000)
                }
            }
        })

        // Bắt đầu tự động chuyển slide
        if (photoList.isNotEmpty()) {
            handler.postDelayed(runnable, 3000)


            // Xu ly click gg map
            binding.llItemService1.setOnClickListener {
                val intent = Intent(requireContext(), MapActivity::class.java).apply {
                    putExtra("source", "cleaning_service")
                }
                startActivity(intent)
            }
        }

    }
    private fun checkLoginStatus() {
        val isLoggedIn = preferencesManager.getAuthToken() != null
        
        // Update UI based on login status
        if (isLoggedIn) {
            binding.tvHeaderText.text = "Xin chào \n" + preferencesManager.getUserData()["user_name"] ?: "Người dùng"
            binding.cardViewButtonLogin.visibility = View.GONE
            binding.tvContentHeader.setTextColor(resources.getColor(R.color.cam))
            // TODO: Show user info if needed
        } else {
            binding.cardViewButtonLogin.visibility = View.VISIBLE
            binding.tvHeaderText.text = "Hôm nay bạn thế nào?"
            binding.tvContentHeader.setTextColor(resources.getColor(R.color.black))
        }
    }
    
    override fun onLoginSuccess() {
        // Update UI after successful login
        // Cập nhật UI khi đăng nhập thành công
        checkLoginStatus()

        // Gửi broadcast thông báo cập nhật dữ liệu người dùng
        val userData = preferencesManager.getUserData()
        val userName = userData["user_name"] ?: "Người dùng"
        val userPhone = userData["user_phone"] ?: ""
        UserDataBroadcastManager.sendUserDataUpdatedBroadcast(
            requireContext(),
            userName,
            userPhone
        )
    }



    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        if (photoList.isNotEmpty()) {
            handler.postDelayed(runnable, 3000)
        }
    }

    private fun registerUserDataReceiver() {
        val filter = IntentFilter(UserDataBroadcastManager.ACTION_USER_DATA_UPDATED)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(userDataUpdateReceiver, filter)
    }
    
    private fun updateUserDataInUI(name: String, phone: String) {
        // Update the header text with new user name (giữ định dạng xuống dòng như checkLoginStatus)
        binding.tvHeaderText.text = "Xin chào \n$name"

        // Cập nhật các UI elements giống như checkLoginStatus khi đã đăng nhập
        binding.cardViewButtonLogin.visibility = View.GONE
        binding.tvContentHeader.setTextColor(resources.getColor(R.color.cam))

        Log.d("HomeFragment", "User data updated via broadcast: Name=$name, Phone=$phone")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
        
        // Unregister broadcast receiver
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(userDataUpdateReceiver)
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error unregistering receiver", e)
        }
        
        _binding = null
    }
}