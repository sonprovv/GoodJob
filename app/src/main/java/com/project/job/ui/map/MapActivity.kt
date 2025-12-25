package com.project.job.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.project.job.R
import com.project.job.data.model.NominatimReverseResult
import com.project.job.data.model.NominatimSearchResult
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivityMapBinding
import com.project.job.ui.service.cleaningservice.SelectServiceActivity
import com.project.job.ui.service.healthcareservice.SelectServiceHealthCareActivity
import com.project.job.ui.service.maintenanceservice.SelectServiceMaintenanceActivity
import com.project.job.utils.addFadeClickEffect
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MapActivity - Activity chính để chọn vị trí trên bản đồ
 * 
 * Chức năng chính:
 * - Hiển thị bản đồ Mapbox với khả năng tương tác
 * - Cho phép người dùng chọn vị trí bằng cách chạm vào bản đồ
 * - Tìm kiếm địa điểm bằng text search (Nominatim API)
 * - Lấy vị trí hiện tại của người dùng (GPS/Network)
 * - Chuyển đổi tọa độ thành địa chỉ (reverse geocoding)
 * - Trả kết quả về activity gọi hoặc chuyển sang service selection
 */
class MapActivity : ComponentActivity(), LocationListener {

    // ==================== UI Components ====================
    private var binding: ActivityMapBinding? = null
    private lateinit var mapView: MapView
    
    // ==================== Location Components ====================
    private lateinit var navigationLocationProvider: NavigationLocationProvider
    private lateinit var locationManager: LocationManager
    private var currentLocation: Point? = null // Vị trí hiện tại của người dùng
    private var hasMovedToCurrentLocation = false // Flag để tránh auto-move nhiều lần
    
    // ==================== Map Components ====================
    private lateinit var pointAnnotationManager: PointAnnotationManager // Quản lý marker trên map
    private var isLocationUpdatesActive = false // Flag theo dõi trạng thái location updates
    
    // ==================== Data Management ====================
    private lateinit var preferencesManager: PreferencesManager
    
    // Biến lưu vị trí và địa chỉ được chọn bằng cách kéo thả/chạm
    private var selectedLocation: Point? = null // Tọa độ được chọn
    private var selectedAddress: String? = null // Địa chỉ tương ứng với tọa độ
    
    // ==================== Search Components ====================
    private lateinit var searchResultsAdapter: SearchResultsAdapter // Adapter cho danh sách kết quả tìm kiếm
    private val gson = Gson() // JSON parser cho API responses
    
    // Debounce search - Tránh gọi API quá nhiều khi user đang gõ
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val SEARCH_DELAY_MS = 500L // 500ms delay sau khi user dừng gõ

    // ==================== Permission Handling ====================
    /**
     * Activity result launcher để xử lý kết quả yêu cầu quyền truy cập vị trí
     * Sử dụng ActivityResultContracts.RequestMultiplePermissions() để yêu cầu nhiều quyền cùng lúc
     */
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                Log.d(TAG, "Location permissions granted")
                initializeMapComponents() // Khởi tạo map khi có quyền
            }
            else -> {
                Log.e(TAG, "Location permissions denied")
                Toast.makeText(
                    this,
                    "Quyền truy cập vị trí bị từ chối. Vui lòng cấp quyền trong cài đặt.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Khởi tạo Activity và thiết lập các thành phần cơ bản
     * 
     * Thực hiện:
     * 1. Khởi tạo View Binding
     * 2. Thiết lập giao diện status bar
     * 3. Khởi tạo các service cần thiết
     * 4. Thiết lập UI và kiểm tra quyền truy cập vị trí
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Khởi tạo View Binding
        binding = ActivityMapBinding.inflate(layoutInflater)
        binding?.root?.let { setContentView(it) } ?: run {
            Log.e(TAG, "Binding initialization failed")
            finish()
            return
        }
        
        // ==================== Thiết lập Status Bar ====================
        // Thiết lập màu sắc cho status bar (Android 5.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = Color.parseColor("#FFFFFF") // Màu nền status bar trắng
        }

        // Đặt icon tối cho status bar (Android 6.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Icon tối cho nền sáng
        }

        // ==================== Khởi tạo Services ====================
        preferencesManager = PreferencesManager(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // ==================== Logging & Debug ====================
        val dateFormat = SimpleDateFormat("hh:mm a zzz, EEEE, dd MMMM yyyy", Locale.getDefault())
        Log.d(TAG, "Started at: ${dateFormat.format(Date())}")
        
        // Lấy source parameter để biết activity nào gọi MapActivity
        val source = intent.getStringExtra("source")
        Log.d(TAG, "onCreate - Received source parameter: '$source'")

        // ==================== Khởi tạo UI và Permissions ====================
        setupUI() // Thiết lập giao diện người dùng
        checkLocationPermissions() // Kiểm tra và yêu cầu quyền truy cập vị trí
    }

    /**
     * Thiết lập giao diện người dùng và các event listener
     * 
     * Bao gồm:
     * - Nút back với hiệu ứng
     * - RecyclerView cho kết quả tìm kiếm
     * - Search bar với debouncing
     * - Nút quay về vị trí hiện tại
     * - Nút xác nhận chọn vị trí
     */
    private fun setupUI() {
        // ==================== Nút Back ====================
        binding?.ivBack?.addFadeClickEffect {
            finish()
            // Thêm hiệu ứng chuyển màn khi back
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        // ==================== Setup RecyclerView cho Search Results ====================
        searchResultsAdapter = SearchResultsAdapter { result ->
            // Callback khi user click vào một kết quả tìm kiếm
            onSearchResultSelected(result)
        }
        binding?.rvSearchResults?.apply {
            layoutManager = LinearLayoutManager(this@MapActivity)
            adapter = searchResultsAdapter
        }

        // ==================== Search Bar với Debouncing ====================
        /**
         * Xử lý sự kiện tìm kiếm real-time với debouncing
         * Debouncing: Chờ 500ms sau khi user dừng gõ mới thực hiện search
         * Tránh gọi API quá nhiều khi user đang gõ liên tục
         */
        binding?.searchBar?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Không cần xử lý
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Hủy search request đang chờ (nếu có)
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                
                val query = s?.toString()?.trim() ?: ""
                
                if (query.isEmpty()) {
                    // Nếu search bar trống, ẩn kết quả tìm kiếm
                    hideSearchResults()
                } else if (query.length >= 2) {
                    // Chỉ search khi nhập >= 2 ký tự để tránh kết quả quá rộng
                    // Tạo runnable mới để search sau SEARCH_DELAY_MS
                    searchRunnable = Runnable {
                        Log.d(TAG, "Auto-searching for: $query")
                        searchLocation(query) // Gọi API tìm kiếm
                    }
                    searchHandler.postDelayed(searchRunnable!!, SEARCH_DELAY_MS)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Không cần xử lý
            }
        })
        
        // ==================== Enter Key Listener ====================
        /**
         * Listener cho phím Enter để search ngay lập tức
         * Bỏ qua debouncing khi user nhấn Enter
         */
        binding?.searchBar?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                // Hủy debounce và search ngay lập tức
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                
                val query = binding?.searchBar?.text.toString().trim()
                if (query.isNotEmpty()) {
                    Log.d(TAG, "Manual search (Enter pressed): $query")
                    searchLocation(query) // Tìm kiếm ngay
                } else {
                    Toast.makeText(this, "Vui lòng nhập địa điểm", Toast.LENGTH_SHORT).show()
                }
                true // Tiêu thụ event
            } else {
                false // Không xử lý event khác
            }
        }

        // ==================== Nút Quay Về Vị Trí Hiện Tại ====================
        /**
         * Nút để di chuyển camera về vị trí hiện tại của người dùng
         * Sử dụng GPS hoặc Network location
         */
        binding?.returnToMyLocationButton?.addFadeClickEffect {
            Log.d(TAG, "Return to current location clicked")
            moveToCurrentLocation() // Di chuyển camera về vị trí hiện tại
        }

        // ==================== Nút Xác Nhận Chọn Vị Trí ====================
        /**
         * Nút chính để xác nhận vị trí đã chọn
         * Sẽ xử lý logic dựa trên source parameter (từ activity nào gọi)
         */
        binding?.cardViewButtonConfirm?.addFadeClickEffect {
            confirmSelectedLocation() // Xác nhận và xử lý vị trí đã chọn
        }

        // Ban đầu ẩn nút confirm cho đến khi user chọn vị trí
        hideConfirmButton()
    }

    /**
     * Xử lý xác nhận chọn vị trí
     * 
     * Kiểm tra:
     * 1. Có vị trí được chọn không
     * 2. Có địa chỉ cụ thể không (nếu không thì dùng tọa độ)
     * 3. Gọi handleLocationSelection để xử lý tiếp
     */
    private fun confirmSelectedLocation() {
        val location = getSelectedLocation()
        val address = getSelectedAddress()

        when {
            location == null -> {
                Toast.makeText(this, "⚠️ Vui lòng chọn một vị trí trên bản đồ!", Toast.LENGTH_LONG).show()
                return
            }
            address.isNullOrEmpty() -> {
                // Nếu không có địa chỉ cụ thể, sử dụng tọa độ làm fallback
                val coords = formatCoordinates(location)
                handleLocationSelection(location, coords)
            }
            else -> {
                // Có địa chỉ cụ thể từ reverse geocoding
                handleLocationSelection(location, address)
            }
        }
    }

    /**
     * Xử lý chọn vị trí dựa trên source parameter
     * 
     * Source parameter cho biết activity nào đã gọi MapActivity:
     * - "update_profile": Từ UpdateProfileActivity -> trả kết quả về
     * - "healthcare_service": Từ healthcare flow -> chuyển sang SelectServiceHealthCareActivity
     * - "cleaning_service": Từ cleaning flow -> chuyển sang SelectServiceActivity
     * - "maintenance_service": Từ maintenance flow -> chuyển sang SelectServiceMaintenanceActivity
     * - null/other: Mặc định chuyển sang cleaning service
     */
    private fun handleLocationSelection(location: Point, addressInfo: String) {
        val source = intent.getStringExtra("source")
        Log.d(TAG, "handleLocationSelection - source: '$source'")
        
        when (source) {
            "update_profile" -> {
                // Trả kết quả về UpdateProfileActivity để cập nhật profile
                returnLocationToProfile(location, addressInfo)
            }
            "healthcare_service" -> {
                // Chuyển về SelectServiceHealthCareActivity với thông tin vị trí
                Log.d(TAG, "Matched healthcare_service case - calling proceedToHealthcareService")
                proceedToHealthcareService(location, addressInfo)
            }
            "cleaning_service" -> {
                // Chuyển về SelectServiceActivity với thông tin vị trí
                proceedToCleaningService(location, addressInfo)
            }
            "maintenance_service" -> {
                // Chuyển về SelectServiceMaintenanceActivity với thông tin vị trí
                proceedToMaintenanceService(location, addressInfo)
            }
            else -> {
                // Mặc định chuyển sang SelectServiceActivity (cleaning service)
                proceedToCleaningService(location, addressInfo)
            }
        }
    }

    /** Trả kết quả vị trí về UpdateProfileActivity để cập nhật thông tin profile */
    private fun returnLocationToProfile(location: Point, addressInfo: String) {
        Log.d(TAG, "Returning location to UpdateProfileActivity: $addressInfo")
        
        val resultIntent = Intent().apply {
            putExtra("selected_latitude", location.latitude())
            putExtra("selected_longitude", location.longitude())
            putExtra("selected_address", addressInfo)
            putExtra("location_source", "map_selection")
            putExtra("timestamp", System.currentTimeMillis())
        }
        
        // Hiển thị thông báo xác nhận
        Toast.makeText(
            this,
            "✅ Đã chọn vị trí:\n$addressInfo",
            Toast.LENGTH_SHORT
        ).show()
        
        setResult(RESULT_OK, resultIntent)
        finish()
        // Thêm hiệu ứng chuyển màn khi back
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    /** Chuyển sang SelectServiceActivity với thông tin vị trí đã chọn (cleaning service) */
    private fun proceedToCleaningService(location: Point, addressInfo: String) {
        Log.d(TAG, "Proceeding to SelectServiceActivity with: $addressInfo")
        // KHÔNG save vào user profile - chỉ truyền location cho job này

        val lat = location.latitude()
        val lng = location.longitude()
        preferencesManager.saveLocationCoordinatesJob(lat, lng)

        val intent = Intent(this, SelectServiceActivity::class.java).apply {
            // Truyền tọa độ
            putExtra("selected_latitude", location.latitude())
            putExtra("selected_longitude", location.longitude())

            // Truyền địa chỉ
            putExtra("selected_address", addressInfo)

            // Truyền thông tin bổ sung
            putExtra("location_source", "map_selection")
            putExtra("timestamp", System.currentTimeMillis())
            
            // Clear activity stack và quay về existing instance
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        // Hiển thị thông báo xác nhận
        Toast.makeText(
            this,
            "✅ Đã chọn vị trí:\n$addressInfo",
            Toast.LENGTH_SHORT
        ).show()

        startActivity(intent)
        finish() // Đóng MapActivity
        // Thêm hiệu ứng chuyển màn khi back
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    /** Chuyển sang SelectServiceHealthCareActivity với thông tin vị trí đã chọn (healthcare service) */
    private fun proceedToHealthcareService(location: Point, addressInfo: String) {
        Log.d(TAG, "Proceeding to SelectServiceHealthCareActivity with: $addressInfo")
        // KHÔNG save vào user profile - chỉ truyền location cho job này

        val lat = location.latitude()
        val lng = location.longitude()
        preferencesManager.saveLocationCoordinatesJob(lat, lng)

        val intent = Intent(this, SelectServiceHealthCareActivity::class.java).apply {
            // Truyền tọa độ
            putExtra("selected_latitude", location.latitude())
            putExtra("selected_longitude", location.longitude())

            // Truyền địa chỉ
            putExtra("selected_address", addressInfo)

            // Truyền thông tin bổ sung
            putExtra("location_source", "map_selection")
            putExtra("timestamp", System.currentTimeMillis())
            
            // Clear activity stack và quay về existing instance
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        // Hiển thị thông báo xác nhận
        Toast.makeText(
            this,
            "✅ Đã chọn vị trí:\n$addressInfo",
            Toast.LENGTH_SHORT
        ).show()

        startActivity(intent)
        finish() // Đóng MapActivity
        // Thêm hiệu ứng chuyển màn khi back
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    /** Chuyển sang SelectServiceMaintenanceActivity với thông tin vị trí đã chọn (maintenance service) */
    private fun proceedToMaintenanceService(location: Point, addressInfo: String) {
        Log.d(TAG, "Proceeding to SelectServiceMaintenanceActivity with: $addressInfo")
        // KHÔNG save vào user profile - chỉ truyền location cho job này

        val lat = location.latitude()
        val lng = location.longitude()
        preferencesManager.saveLocationCoordinatesJob(lat, lng)

        val intent = Intent(this, SelectServiceMaintenanceActivity::class.java).apply {
            // Truyền tọa độ
            putExtra("selected_latitude", location.latitude())
            putExtra("selected_longitude", location.longitude())

            // Truyền địa chỉ
            putExtra("selected_address", addressInfo)

            // Truyền thông tin bổ sung
            putExtra("location_source", "map_selection")
            putExtra("timestamp", System.currentTimeMillis())

            // Clear activity stack và quay về existing instance
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        // Hiển thị thông báo xác nhận
        Toast.makeText(
            this,
            "✅ Đã chọn vị trí:\n$addressInfo",
            Toast.LENGTH_SHORT
        ).show()

        startActivity(intent)
        finish() // Đóng MapActivity
        // Thêm hiệu ứng chuyển màn khi back
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    /** Hiển thị nút xác nhận chọn vị trí */
    private fun showConfirmButton() {
        binding?.cardViewButtonConfirm?.visibility = View.VISIBLE
        Log.d(TAG, "Confirm button shown")
    }

    /** Ẩn nút xác nhận chọn vị trí */
    private fun hideConfirmButton() {
        binding?.cardViewButtonConfirm?.visibility = View.GONE
        Log.d(TAG, "Confirm button hidden")
    }

    /** Kiểm tra quyền truy cập vị trí và yêu cầu nếu chưa có */
    private fun checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Location permissions already granted")
            initializeMapComponents()
        } else {
            Log.d(TAG, "Requesting location permissions")
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /** Khởi tạo các thành phần của bản đồ Mapbox và location services */
    private fun initializeMapComponents() {
        try {
            Log.d(TAG, "Initializing map components")

            // Khởi tạo MapView
            mapView = binding?.map ?: throw IllegalStateException("MapView not found")

            // Set camera tại vị trí đã lưu hoặc mặc định tại Hà Nội
            val initialLocation = getInitialLocation()
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(initialLocation)
                    .zoom(15.0)
                    .build()
            )

            // Khởi tạo annotation manager để thêm marker
            pointAnnotationManager = mapView.annotations.createPointAnnotationManager(AnnotationConfig())

            // Khởi tạo location provider và location puck
            navigationLocationProvider = NavigationLocationProvider()
            mapView.location.apply {
                setLocationProvider(navigationLocationProvider)
                locationPuck = LocationPuck2D()
                enabled = true
            }

            // Thêm listener cho việc chạm/kéo thả trên map
            setupMapClickListener()

            // Nếu có saved location từ profile, add marker tại đó
            checkAndMarkSavedLocation()

            // Bắt đầu theo dõi vị trí
            startLocationTracking()

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing map components", e)
            Toast.makeText(this, "Lỗi khởi tạo map: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /** Kiểm tra và đánh dấu vị trí đã lưu từ profile người dùng */
    private fun checkAndMarkSavedLocation() {
        val savedCoordinates = preferencesManager.getLocationCoordinates()
        
        if (savedCoordinates != null) {
            val (lat, lng) = savedCoordinates
            val location = Point.fromLngLat(lng, lat)
            
            Log.d(TAG, "Found saved location from profile: Lat=$lat, Lng=$lng")
            
            // Set làm selected location
            selectedLocation = location
            
            // Lấy địa chỉ đã lưu nếu có
            val savedAddress = intent.getStringExtra("current_location")
            if (!savedAddress.isNullOrEmpty() && savedAddress != "Chưa cập nhật") {
                // Có địa chỉ đã lưu, sử dụng luôn
                selectedAddress = savedAddress
                addMarkerAtSelectedLocation(location, savedAddress)
                showConfirmButton()
                Log.d(TAG, "Using saved address: $savedAddress")
            } else {
                // Không có địa chỉ, reverse geocode để lấy
                reverseGeocode(lat, lng)
                showConfirmButton()
            }
        } else {
            Log.d(TAG, "No saved location found")
        }
    }

    /** Thiết lập listener để xử lý sự kiện chạm vào bản đồ */
    private fun setupMapClickListener() {
        mapView.mapboxMap.addOnMapClickListener(OnMapClickListener { point ->
            // Ẩn search results nếu đang hiển thị
            hideSearchResults()
            
            // Lưu vị trí được chọn
            selectedLocation = point

            val lat = point.latitude()
            val lng = point.longitude()

            Log.d(TAG, "Map clicked at: Lat=$lat, Lng=$lng")

            // Hiển thị nút confirm ngay lập tức
            showConfirmButton()

            // Thực hiện reverse geocoding để lấy địa chỉ
            reverseGeocode(lat, lng)

            // Trả về true để tiêu thụ sự kiện click
            true
        })
    }

    /** Chuyển đổi tọa độ thành địa chỉ sử dụng Nominatim API (Reverse Geocoding) */
    private fun reverseGeocode(latitude: Double, longitude: Double) {
        Log.d(TAG, "Starting Nominatim reverse geocoding for: $latitude, $longitude")

        // Nominatim Reverse Geocoding API
        val url = "https://nominatim.openstreetmap.org/reverse?lat=$latitude&lon=$longitude&format=json&addressdetails=1&zoom=18"

        Log.d(TAG, "Nominatim Reverse URL: $url")

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "GoodJobApp/1.0") // Nominatim requires User-Agent
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Nominatim reverse geocoding failed", e)
                runOnUiThread {
                    Toast.makeText(this@MapActivity, "Không thể lấy địa chỉ: ${e.message}", Toast.LENGTH_SHORT).show()
                    showFallbackLocation(latitude, longitude)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                Log.d(TAG, "Nominatim reverse response: $json")

                try {
                    if (json.isNullOrEmpty()) {
                        runOnUiThread {
                            showFallbackLocation(latitude, longitude)
                        }
                        return
                    }

                    val result: NominatimReverseResult = gson.fromJson(json, NominatimReverseResult::class.java)
                    val address = result.getFormattedAddress()

                    runOnUiThread {
                        selectedAddress = address
                        showAddressResult(latitude, longitude, address)
                        addMarkerAtSelectedLocation(selectedLocation!!, address)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing Nominatim reverse response", e)
                    runOnUiThread {
                        Toast.makeText(this@MapActivity, "Lỗi xử lý dữ liệu địa chỉ", Toast.LENGTH_SHORT).show()
                        showFallbackLocation(latitude, longitude)
                    }
                }
            }
        })
    }

    /** Hiển thị kết quả địa chỉ đã tìm được từ reverse geocoding */
    private fun showAddressResult(latitude: Double, longitude: Double, address: String) {
        val message = """
            📍 Địa chỉ được chọn:
            $address
            
            👆 Nhấn "Chọn vị trí này" để tiếp tục
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.d(TAG, "Address found: $address")
    }

    /** Hiển thị thông tin tọa độ khi không tìm được địa chỉ cụ thể */
    private fun showFallbackLocation(latitude: Double, longitude: Double) {
        selectedAddress = null // Clear địa chỉ

        val message = """
            📍 Vị trí đã chọn:
            ${formatCoordinates(selectedLocation!!)}
            
            👆 Nhấn "Chọn vị trí này" để tiếp tục
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        selectedLocation?.let {
            addMarkerAtSelectedLocation(it, "Vị trí đã chọn")
        }
    }

    /** Thêm marker (chỉ điểm) tại vị trí được chọn trên bản đồ */
    private fun addMarkerAtSelectedLocation(location: Point, title: String) {
        try {
            // Xóa các marker cũ
            pointAnnotationManager.deleteAll()

            val bitmap = getBitmapFromVectorDrawable(this, R.drawable.ic_location_marker)
            if (bitmap != null) {
                mapView.mapboxMap.getStyle { style ->
                    style.addImage("selected-marker-icon", bitmap)

                    val pointAnnotation = PointAnnotationOptions()
                        .withPoint(location)
                        .withIconImage("selected-marker-icon")
                        .withTextField(title)

                    pointAnnotationManager.create(pointAnnotation)
                    Log.d(TAG, "Marker added at selected location")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding marker at selected location", e)
        }
    }

    /** Getter để lấy vị trí đã chọn */
    fun getSelectedLocation(): Point? {
        return selectedLocation
    }

    /** Getter để lấy địa chỉ đã chọn */
    fun getSelectedAddress(): String? {
        return selectedAddress
    }

    /** Format tọa độ thành chuỗi hiển thị đẹp */
    private fun formatCoordinates(point: Point): String {
        return "Lat: ${String.format("%.6f", point.latitude())}, Lng: ${String.format("%.6f", point.longitude())}"
    }

    /** Bắt đầu theo dõi vị trí hiện tại của người dùng */
    private fun startLocationTracking() {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted")
            return
        }

        try {
            // Kiểm tra xem GPS và Network provider có khả dụng không
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                Toast.makeText(this, "Vui lòng bật GPS hoặc kết nối mạng", Toast.LENGTH_LONG).show()
                return
            }

            // Lấy vị trí cuối cùng đã biết
            val lastKnownGps = if (isGpsEnabled) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else null

            val lastKnownNetwork = if (isNetworkEnabled) {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else null

            // Sử dụng vị trí GPS nếu có, nếu không thì dùng Network
            val lastKnownLocation = lastKnownGps ?: lastKnownNetwork

            lastKnownLocation?.let { location ->
                Log.d(TAG, "Using last known location: ${location.latitude}, ${location.longitude}")
                updateLocation(location)
            }

            // Đăng ký listener cho cập nhật vị trí
            if (isGpsEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    this,
                    Looper.getMainLooper()
                )
                Log.d(TAG, "GPS location updates requested")
            }

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    this,
                    Looper.getMainLooper()
                )
                Log.d(TAG, "Network location updates requested")
            }

            isLocationUpdatesActive = true

        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception when requesting location updates", e)
            Toast.makeText(this, "Lỗi bảo mật khi truy cập vị trí", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting location tracking", e)
            Toast.makeText(this, "Lỗi bắt đầu theo dõi vị trí: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /** Dừng theo dõi vị trí để tiết kiệm pin */
    private fun stopLocationTracking() {
        if (isLocationUpdatesActive) {
            try {
                locationManager.removeUpdates(this)
                isLocationUpdatesActive = false
                Log.d(TAG, "Location tracking stopped")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping location tracking", e)
            }
        }
    }

    /** Kiểm tra xem có quyền truy cập vị trí hay không */
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /** Cập nhật vị trí hiện tại và hiển thị trên bản đồ */
    private fun updateLocation(location: Location) {
        currentLocation = Point.fromLngLat(location.longitude, location.latitude)

        Log.d(TAG, "Location updated: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}m")

        // Tạo Mapbox Location object
        val mapboxLocation = com.mapbox.common.location.Location.Builder()
            .latitude(location.latitude)
            .longitude(location.longitude)
            .bearing(location.bearing.toDouble())
            .timestamp(location.time)
            .build()

        // Cập nhật location provider
        navigationLocationProvider.changePosition(mapboxLocation, emptyList())
        
        // Chỉ tự động di chuyển camera nếu không có tọa độ đã lưu từ profile
        if (!hasMovedToCurrentLocation && preferencesManager.getLocationCoordinates() == null) {
            moveToCurrentLocationAutomatically()
            hasMovedToCurrentLocation = true
        }
    }

    /** Callback khi vị trí thay đổi (LocationListener interface) */
    override fun onLocationChanged(location: Location) {
        updateLocation(location)
    }

    /** Callback khi location provider được bật */
    override fun onProviderEnabled(provider: String) {
        Log.d(TAG, "Provider enabled: $provider")
        Toast.makeText(this, "Đã bật $provider", Toast.LENGTH_SHORT).show()
    }

    /** Callback khi location provider bị tắt */
    override fun onProviderDisabled(provider: String) {
        Log.d(TAG, "Provider disabled: $provider")
        Toast.makeText(this, "Đã tắt $provider", Toast.LENGTH_SHORT).show()
    }

    /** Lấy vị trí khởi tạo cho camera bản đồ (từ profile hoặc mặc định Hà Nội) */
    private fun getInitialLocation(): Point {
        // Lấy tọa độ đã lưu từ preferences
        val savedCoordinates = preferencesManager.getLocationCoordinates()
        
        if (savedCoordinates != null) {
            val (lat, lng) = savedCoordinates
            Log.d(TAG, "Using saved coordinates from profile: Lat=$lat, Lng=$lng")
            return Point.fromLngLat(lng, lat)
        }
        
        // Lấy location text từ intent nếu có (từ UpdateProfileActivity)
        val savedLocation = intent.getStringExtra("current_location")
        if (!savedLocation.isNullOrEmpty() && savedLocation != "Chưa cập nhật") {
            Log.d(TAG, "Has saved location text but no coordinates: $savedLocation")
        }
        
        // Mặc định tại Hà Nội nếu không có location đã lưu
        Log.d(TAG, "Using default location: Hanoi")
        return Point.fromLngLat(105.8542, 21.0285)
    }

    /** Tự động di chuyển camera đến vị trí hiện tại (chỉ 1 lần) */
    private fun moveToCurrentLocationAutomatically() {
        currentLocation?.let { location ->
            Log.d(TAG, "Auto-moving to current location: ${location.latitude()}, ${location.longitude()}")

            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(location)
                    .zoom(16.0)
                    .build()
            )

            Log.d(TAG, "Camera moved to current location automatically")
        } ?: run {
            Log.w(TAG, "Cannot auto-move to current location: location is null")
        }
    }

    /** Di chuyển camera đến vị trí hiện tại khi user nhấn nút */
    private fun moveToCurrentLocation() {
        Log.d(TAG, "Attempting to move to current location")

        currentLocation?.let { location ->
            Log.d(TAG, "Moving to location: ${location.latitude()}, ${location.longitude()}")

            // Di chuyển camera đến vị trí hiện tại
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(location)
                    .zoom(16.0)
                    .build()
            )

            // Set làm selected location
            selectedLocation = location
            
            // Hiển thị nút confirm
            showConfirmButton()
            
            // Add marker và reverse geocode để lấy địa chỉ
            reverseGeocode(location.latitude(), location.longitude())

            Toast.makeText(this, "📍 Đã di chuyển đến vị trí hiện tại", Toast.LENGTH_SHORT).show()
        } ?: run {
            Log.w(TAG, "Current location is null, requesting fresh location")
            Toast.makeText(this, "Đang tìm vị trí hiện tại...", Toast.LENGTH_SHORT).show()

            // Request a one-time location update
            requestSingleLocationUpdate()
        }
    }

    /** Yêu cầu cập nhật vị trí một lần khi vị trí hiện tại null */
    private fun requestSingleLocationUpdate() {
        if (!hasLocationPermission()) return

        try {
            // Thử GPS trước, nếu không có thì dùng Network
            val provider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> {
                    Toast.makeText(this, "Không có provider vị trí khả dụng", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            locationManager.getCurrentLocation(
                provider,
                null,
                ContextCompat.getMainExecutor(this)
            ) { location ->
                location?.let {
                    Log.d(TAG, "Got single location update: ${it.latitude}, ${it.longitude}")
                    updateLocation(it)
                    moveToCurrentLocation()
                } ?: run {
                    Log.w(TAG, "Single location update returned null")
                    Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting single location update", e)
        }
    }

    /** Tìm kiếm địa điểm sử dụng Nominatim API (Forward Geocoding) */
    private fun searchLocation(query: String) {
        Log.d(TAG, "Starting Nominatim search for: $query")

        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        // Nominatim Search API - Forward Geocoding
        val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&addressdetails=1&limit=10&countrycodes=vn"

        Log.d(TAG, "Nominatim Search URL: $url")

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("User-Agent", "GoodJobApp/1.0") // Nominatim requires User-Agent
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Nominatim search request failed", e)
                runOnUiThread {
                    Toast.makeText(this@MapActivity, "Tìm kiếm thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                    hideSearchResults()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                Log.d(TAG, "Nominatim search response: $json")

                try {
                    if (json.isNullOrEmpty()) {
                        runOnUiThread {
                            Toast.makeText(this@MapActivity, "Không tìm thấy kết quả", Toast.LENGTH_SHORT).show()
                            hideSearchResults()
                        }
                        return
                    }

                    // Parse JSON array to List<NominatimSearchResult>
                    val type = object : TypeToken<List<NominatimSearchResult>>() {}.type
                    val results: List<NominatimSearchResult> = gson.fromJson(json, type)

                    runOnUiThread {
                        if (results.isNotEmpty()) {
                            Log.d(TAG, "Found ${results.size} results")
                            showSearchResults(results)
                        } else {
                            Toast.makeText(this@MapActivity, "Không tìm thấy kết quả cho '$query'", Toast.LENGTH_SHORT).show()
                            hideSearchResults()
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing Nominatim search response", e)
                    runOnUiThread {
                        Toast.makeText(this@MapActivity, "Lỗi xử lý dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                        hideSearchResults()
                    }
                }
            }
        })
    }

    /** Hiển thị danh sách kết quả tìm kiếm trong RecyclerView */
    private fun showSearchResults(results: List<NominatimSearchResult>) {
        searchResultsAdapter.submitList(results)
        binding?.searchResultsContainer?.visibility = View.VISIBLE
        Log.d(TAG, "Showing ${results.size} search results")
    }

    /** Ẩn danh sách kết quả tìm kiếm */
    private fun hideSearchResults() {
        binding?.searchResultsContainer?.visibility = View.GONE
        searchResultsAdapter.clearResults()
        Log.d(TAG, "Search results hidden")
    }

    /** Xử lý khi user chọn một kết quả từ danh sách tìm kiếm */
    private fun onSearchResultSelected(result: NominatimSearchResult) {
        Log.d(TAG, "Search result selected: ${result.displayName}")
        
        val location = Point.fromLngLat(result.getLongitude(), result.getLatitude())
        val address = result.getShortAddress()
        
        // Hide search results
        hideSearchResults()
        
        // Clear search bar
        binding?.searchBar?.setText("")
        
        // Show location on map
        showLocationOnMap(location, address)
        
        // Set as selected location
        selectedLocation = location
        selectedAddress = address
        showConfirmButton()
        
        Toast.makeText(this, "Đã chọn: $address", Toast.LENGTH_SHORT).show()
    }

    /** Hiển thị vị trí trên bản đồ với marker và di chuyển camera */
    private fun showLocationOnMap(location: Point, title: String) {
        // Di chuyển camera đến vị trí
        mapView.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(location)
                .zoom(16.0)
                .build()
        )

        // Xóa marker cũ
        pointAnnotationManager.deleteAll()

        // Thêm marker mới
        try {
            val bitmap = getBitmapFromVectorDrawable(this, R.drawable.ic_location_marker)
            if (bitmap != null) {
                // Thêm icon vào style
                mapView.mapboxMap.getStyle { style ->
                    style.addImage("marker-icon", bitmap)

                    // Tạo annotation
                    val pointAnnotation = PointAnnotationOptions()
                        .withPoint(location)
                        .withIconImage("marker-icon")
                        .withTextField(title)

                    pointAnnotationManager.create(pointAnnotation)

                    Log.d(TAG, "Marker added successfully for: $title")
                    Toast.makeText(this@MapActivity, "Tìm thấy: $title", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e(TAG, "Failed to decode marker bitmap")
                Toast.makeText(this@MapActivity, "Lỗi tải icon marker", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding marker", e)
            Toast.makeText(this@MapActivity, "Lỗi thêm marker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /** Chuyển đổi vector drawable thành bitmap để sử dụng làm marker icon */
    private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /** Khởi động lại location tracking khi activity resume */
    override fun onResume() {
        super.onResume()
        // Khởi động lại location tracking nếu cần thiết
        if (hasLocationPermission() && !isLocationUpdatesActive) {
            startLocationTracking()
        }
    }

    /** Tạm dừng location tracking khi activity pause để tiết kiệm pin */
    override fun onPause() {
        super.onPause()
        // Tạm dừng location tracking để tiết kiệm pin
        stopLocationTracking()
    }

    /** Cleanup resources khi activity bị destroy */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Destroying MapActivity")

        // Dừng location tracking
        stopLocationTracking()
        
        // Cleanup search handler để tránh memory leak
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        searchRunnable = null

        binding = null
    }

    companion object {
        private const val TAG = "MapActivity"
        private const val MIN_TIME_BETWEEN_UPDATES = 5000L // 5 giây
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f // 10 mét
    }
}