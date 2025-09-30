package com.project.job.ui.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.project.job.data.source.local.PreferencesManager
import com.project.job.databinding.ActivityMapBinding
import com.project.job.ui.service.cleaningservice.SelectServiceActivity
import com.project.job.ui.service.healthcareservice.SelectServiceHealthCareActivity
import com.project.job.ui.service.maintenanceservice.SelectServiceMaintenanceActivity
import com.project.job.utils.Constant
import com.project.job.utils.addFadeClickEffect
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapActivity : ComponentActivity(), LocationListener {

    private var binding: ActivityMapBinding? = null
    private lateinit var mapView: MapView
    private lateinit var navigationLocationProvider: NavigationLocationProvider
    private lateinit var locationManager: LocationManager
    private var currentLocation: Point? = null
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private var isLocationUpdatesActive = false
    private lateinit var preferencesManager: PreferencesManager
    private var hasMovedToCurrentLocation = false

    // Biến lưu vị trí và địa chỉ được chọn bằng cách kéo thả/chạm
    private var selectedLocation: Point? = null
    private var selectedAddress: String? = null

    // Activity result launcher for location permissions
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                Log.d(TAG, "Location permissions granted")
                initializeMapComponents()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        binding?.root?.let { setContentView(it) } ?: run {
            Log.e(TAG, "Binding initialization failed")
            finish()
            return
        }
        preferencesManager = PreferencesManager(this)

        // Log thời gian khởi tạo và source parameter
        val dateFormat = SimpleDateFormat("hh:mm a zzz, EEEE, dd MMMM yyyy", Locale.getDefault())
        Log.d(TAG, "Started at: ${dateFormat.format(Date())}")
        
        val source = intent.getStringExtra("source")
        Log.d(TAG, "onCreate - Received source parameter: '$source'")

        // Khởi tạo LocationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setupUI()
        checkLocationPermissions()
    }

    private fun setupUI() {
        // Nút back
        binding?.ivBack?.addFadeClickEffect {
            finish()
        }

        // Xử lý sự kiện tìm kiếm
        binding?.searchBar?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                val query = binding?.searchBar?.text.toString().trim()
                if (query.isNotEmpty()) {
                    Log.d(TAG, "Searching for: $query")
                    searchLocation(query)
                } else {
                    Toast.makeText(this, "Vui lòng nhập địa điểm", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        // Xử lý nút quay về vị trí hiện tại
        binding?.returnToMyLocationButton?.addFadeClickEffect {
            Log.d(TAG, "Return to current location clicked")
            moveToCurrentLocation()
        }

        // Xử lý nút xác nhận chọn vị trí - CHỨC NĂNG CHÍNH
        binding?.cardViewButtonConfirm?.addFadeClickEffect {
            confirmSelectedLocation()
        }

        // Ban đầu ẩn nút confirm
        hideConfirmButton()
    }

    // Xử lý xác nhận chọn vị trí
    private fun confirmSelectedLocation() {
        val location = getSelectedLocation()
        val address = getSelectedAddress()

        when {
            location == null -> {
                Toast.makeText(this, "⚠️ Vui lòng chọn một vị trí trên bản đồ!", Toast.LENGTH_LONG).show()
                return
            }
            address.isNullOrEmpty() -> {
                // Nếu không có địa chỉ cụ thể, sử dụng tọa độ
                val coords = formatCoordinates(location)
                handleLocationSelection(location, coords)
            }
            else -> {
                // Có địa chỉ cụ thể
                handleLocationSelection(location, address)
            }
        }
    }

    // Xử lý chọn vị trí dựa trên source
    private fun handleLocationSelection(location: Point, addressInfo: String) {
        val source = intent.getStringExtra("source")
        Log.d(TAG, "handleLocationSelection - source: '$source'")
        
        when (source) {
            "update_profile" -> {
                // Trả kết quả về UpdateProfileActivity
                returnLocationToProfile(location, addressInfo)
            }
            "healthcare_service" -> {
                // Chuyển về SelectServiceHealthCareActivity
                Log.d(TAG, "Matched healthcare_service case - calling proceedToHealthcareService")
                proceedToHealthcareService(location, addressInfo)
            }
            "cleaning_service" -> {
                // Chuyển về SelectServiceActivity
                proceedToCleaningService(location, addressInfo)
            }
            "maintenance_service" -> {
                proceedToMaintenanceService(location, addressInfo)
            }
            else -> {
                // Mặc định chuyển sang SelectServiceActivity (cleaning)
                proceedToCleaningService(location, addressInfo)
            }
        }
    }

    // Trả kết quả về UpdateProfileActivity
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
    }

    // Chuyển sang SelectServiceActivity với thông tin vị trí (cleaning service)
    private fun proceedToCleaningService(location: Point, addressInfo: String) {
        Log.d(TAG, "Proceeding to SelectServiceActivity with: $addressInfo")
        preferencesManager.saveAddress(addressInfo)

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
    }

    // Chuyển sang SelectServiceHealthCareActivity với thông tin vị trí (healthcare service)
    private fun proceedToHealthcareService(location: Point, addressInfo: String) {
        Log.d(TAG, "Proceeding to SelectServiceHealthCareActivity with: $addressInfo")
        preferencesManager.saveAddress(addressInfo)

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
    }

    // Chuyển sang SelectServiceMaintenanceActivity với thông tin vị trí (maintenance service)
    private fun proceedToMaintenanceService(location: Point, addressInfo: String) {
        Log.d(TAG, "Proceeding to SelectServiceHealthCareActivity with: $addressInfo")
        preferencesManager.saveAddress(addressInfo)

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
    }

    // Hiển thị nút confirm
    private fun showConfirmButton() {
        binding?.cardViewButtonConfirm?.visibility = View.VISIBLE
        Log.d(TAG, "Confirm button shown")
    }

    // Ẩn nút confirm
    private fun hideConfirmButton() {
        binding?.cardViewButtonConfirm?.visibility = View.GONE
        Log.d(TAG, "Confirm button hidden")
    }

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

            // Bắt đầu theo dõi vị trí
            startLocationTracking()

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing map components", e)
            Toast.makeText(this, "Lỗi khởi tạo map: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Thiết lập listener cho việc chạm vào map
    private fun setupMapClickListener() {
        mapView.mapboxMap.addOnMapClickListener(OnMapClickListener { point ->
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

    // Chuyển đổi tọa độ thành địa chỉ (Reverse Geocoding)
    private fun reverseGeocode(latitude: Double, longitude: Double) {
        Log.d(TAG, "Starting reverse geocoding for: $latitude, $longitude")

        if (Constant.API_KEY_MAP.isEmpty()) {
            Toast.makeText(this, "API Key không được cấu hình", Toast.LENGTH_SHORT).show()
            showFallbackLocation(latitude, longitude)
            return
        }

        // Thử nhiều phương pháp để lấy địa chỉ chi tiết
        tryDetailedGeocodingApproaches(latitude, longitude)
    }

    private fun tryDetailedGeocodingApproaches(latitude: Double, longitude: Double) {
        // Approach 1: Sử dụng SerpAPI với type=place để lấy địa chỉ chi tiết
        val serpApiKey = Constant.API_KEY_MAP
        val url = "https://serpapi.com/search.json?engine=google_maps&q=$latitude,$longitude&location=Vietnam&hl=vi&gl=vn&api_key=$serpApiKey&type=place"

        Log.d(TAG, "Detailed reverse geocoding URL: $url")

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Detailed reverse geocoding failed, trying fallback", e)
                // Fallback to basic approach
                tryBasicReverseGeocoding(latitude, longitude)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                Log.d(TAG, "Detailed reverse geocoding response: $json")

                try {
                    val jsonObject = JSONObject(json ?: "")
                    var detailedAddress: String? = null

                    // Thử lấy địa chỉ chi tiết từ các nguồn khác nhau
                    detailedAddress = extractDetailedAddress(jsonObject)

                    runOnUiThread {
                        if (!detailedAddress.isNullOrEmpty()) {
                            selectedAddress = detailedAddress
                            showAddressResult(latitude, longitude, detailedAddress)
                            addMarkerAtSelectedLocation(selectedLocation!!, detailedAddress)
                        } else {
                            // Fallback to basic approach
                            tryBasicReverseGeocoding(latitude, longitude)
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing detailed geocoding response", e)
                    runOnUiThread {
                        tryBasicReverseGeocoding(latitude, longitude)
                    }
                }
            }
        })
    }

    private fun extractDetailedAddress(jsonObject: JSONObject): String? {
        var address: String? = null

        // 1. Thử lấy từ place_results với nhiều field
        val placeResults = jsonObject.optJSONObject("place_results")
        if (placeResults != null) {
            // Thử lấy address đầy đủ
            address = placeResults.optString("address", null)
            
            if (address.isNullOrEmpty()) {
                // Thử kết hợp title + plus_code để tạo địa chỉ có ý nghĩa
                val title = placeResults.optString("title", null)
                val plusCode = placeResults.optString("plus_code", null)
                
                if (!plusCode.isNullOrEmpty()) {
                    // Parse plus_code để lấy thông tin khu vực
                    address = parseAddressFromPlusCode(plusCode, title)
                }
            }
        }

        // 2. Thử lấy từ local_results
        if (address.isNullOrEmpty()) {
            val localResults = jsonObject.optJSONArray("local_results")
            if (localResults != null && localResults.length() > 0) {
                for (i in 0 until localResults.length()) {
                    val result = localResults.getJSONObject(i)
                    val resultAddress = result.optString("address", null)
                    val resultTitle = result.optString("title", null)
                    
                    if (!resultAddress.isNullOrEmpty()) {
                        address = resultAddress
                        break
                    } else if (!resultTitle.isNullOrEmpty() && resultTitle.contains(",")) {
                        // Nếu title có dấu phẩy, có thể là địa chỉ
                        address = resultTitle
                        break
                    }
                }
            }
        }

        return address
    }

    private fun parseAddressFromPlusCode(plusCode: String, title: String?): String? {
        // Parse plus_code format: "98PV+MGG Vũ Thư, Thái Bình, Việt Nam"
        if (plusCode.contains(" ")) {
            val parts = plusCode.split(" ", limit = 2)
            if (parts.size >= 2) {
                val locationPart = parts[1] // "Vũ Thư, Thái Bình, Việt Nam"
                
                // Tạo địa chỉ có ý nghĩa hơn
                return if (!title.isNullOrEmpty() && title != parts[0]) {
                    "$title, $locationPart"
                } else {
                    locationPart
                }
            }
        }
        return plusCode
    }

    private fun tryBasicReverseGeocoding(latitude: Double, longitude: Double) {
        val serpApiKey = Constant.API_KEY_MAP
        val url = "https://serpapi.com/search.json?engine=google_maps&q=$latitude,$longitude&location=Vietnam&hl=vi&gl=vn&api_key=$serpApiKey&type=search"

        Log.d(TAG, "Basic reverse geocoding URL: $url")

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Basic reverse geocoding failed", e)
                runOnUiThread {
                    Toast.makeText(this@MapActivity, "Không thể lấy địa chỉ: ${e.message}", Toast.LENGTH_SHORT).show()
                    showFallbackLocation(latitude, longitude)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                Log.d(TAG, "Basic reverse geocoding response: $json")

                try {
                    val jsonObject = JSONObject(json ?: "")
                    val address = extractDetailedAddress(jsonObject)

                    runOnUiThread {
                        if (!address.isNullOrEmpty()) {
                            selectedAddress = address
                            showAddressResult(latitude, longitude, address)
                            addMarkerAtSelectedLocation(selectedLocation!!, address)
                        } else {
                            showFallbackLocation(latitude, longitude)
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing basic geocoding response", e)
                    runOnUiThread {
                        Toast.makeText(this@MapActivity, "Lỗi xử lý dữ liệu địa chỉ", Toast.LENGTH_SHORT).show()
                        showFallbackLocation(latitude, longitude)
                    }
                }
            }
        })
    }

    // Hiển thị kết quả địa chỉ
    private fun showAddressResult(latitude: Double, longitude: Double, address: String) {
        val message = """
            📍 Địa chỉ được chọn:
            $address
            
            👆 Nhấn "Chọn vị trí này" để tiếp tục
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.d(TAG, "Address found: $address")
    }

    // Hiển thị thông tin dự phòng khi không tìm được địa chỉ
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

    // Thêm marker tại vị trí được chọn
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

    // Hàm để lấy vị trí đã chọn
    fun getSelectedLocation(): Point? {
        return selectedLocation
    }

    // Hàm để lấy địa chỉ đã chọn
    fun getSelectedAddress(): String? {
        return selectedAddress
    }

    // Hàm để lấy thông tin đầy đủ về vị trí đã chọn
    fun getSelectedLocationInfo(): Pair<Point?, String?> {
        return Pair(selectedLocation, selectedAddress)
    }

    // Hàm để format tọa độ thành string đẹp
    private fun formatCoordinates(point: Point): String {
        return "Lat: ${String.format("%.6f", point.latitude())}, Lng: ${String.format("%.6f", point.longitude())}"
    }

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

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

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

    // LocationListener implementation
    override fun onLocationChanged(location: Location) {
        updateLocation(location)
    }

    override fun onProviderEnabled(provider: String) {
        Log.d(TAG, "Provider enabled: $provider")
        Toast.makeText(this, "Đã bật $provider", Toast.LENGTH_SHORT).show()
    }

    override fun onProviderDisabled(provider: String) {
        Log.d(TAG, "Provider disabled: $provider")
        Toast.makeText(this, "Đã tắt $provider", Toast.LENGTH_SHORT).show()
    }

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

    private fun moveToCurrentLocation() {
        Log.d(TAG, "Attempting to move to current location")

        currentLocation?.let { location ->
            Log.d(TAG, "Moving to location: ${location.latitude()}, ${location.longitude()}")

            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(location)
                    .zoom(16.0)
                    .build()
            )

            Toast.makeText(this, "Đã di chuyển đến vị trí hiện tại", Toast.LENGTH_SHORT).show()
        } ?: run {
            Log.w(TAG, "Current location is null, requesting fresh location")
            Toast.makeText(this, "Đang tìm vị trí hiện tại...", Toast.LENGTH_SHORT).show()

            // Request a one-time location update
            requestSingleLocationUpdate()
        }
    }

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

    private fun searchLocation(query: String) {
        Log.d(TAG, "Starting location search for: $query")

        if (Constant.API_KEY_MAP.isEmpty()) {
            Toast.makeText(this, "API Key không được cấu hình", Toast.LENGTH_SHORT).show()
            return
        }

        val serpApiKey = Constant.API_KEY_MAP
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        val url = "https://serpapi.com/search.json?engine=google_maps&q=$encodedQuery&location=Vietnam&hl=vi&gl=vn&api_key=$serpApiKey&type=search"

        Log.d(TAG, "Search URL: $url")

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Search request failed", e)
                runOnUiThread {
                    Toast.makeText(this@MapActivity, "Tìm kiếm thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                Log.d(TAG, "Search response: $json")

                try {
                    val jsonObject = JSONObject(json ?: "")
                    val localResults = jsonObject.optJSONArray("local_results")

                    if (localResults != null && localResults.length() > 0) {
                        val firstResult = localResults.getJSONObject(0)
                        val gpsCoordinates = firstResult.optJSONObject("gps_coordinates")
                        handleCoordinates(gpsCoordinates, firstResult.optString("title", query))
                    } else {
                        // fallback sang place_results
                        val placeResults = jsonObject.optJSONObject("place_results")
                        if (placeResults != null) {
                            val gpsCoordinates = placeResults.optJSONObject("gps_coordinates")
                            handleCoordinates(gpsCoordinates, placeResults.optString("title", query))
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@MapActivity, "Không tìm thấy kết quả cho '$query'", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing search response", e)
                    runOnUiThread {
                        Toast.makeText(this@MapActivity, "Lỗi xử lý dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun handleCoordinates(gpsCoordinates: JSONObject?, title: String) {
        if (gpsCoordinates != null) {
            val lat = gpsCoordinates.optDouble("latitude", Double.NaN)
            val lng = gpsCoordinates.optDouble("longitude", Double.NaN)
            if (!lat.isNaN() && !lng.isNaN()) {
                val location = Point.fromLngLat(lng, lat)
                runOnUiThread {
                    showLocationOnMap(location, title)
                    // Tự động set làm vị trí đã chọn
                    selectedLocation = location
                    selectedAddress = title
                    showConfirmButton()
                }
            }
        }
    }

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

    override fun onResume() {
        super.onResume()
        // Khởi động lại location tracking nếu cần thiết
        if (hasLocationPermission() && !isLocationUpdatesActive) {
            startLocationTracking()
        }
    }

    override fun onPause() {
        super.onPause()
        // Tạm dừng location tracking để tiết kiệm pin
        stopLocationTracking()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Destroying MapActivity")

        // Dừng location tracking
        stopLocationTracking()

        binding = null
    }

    companion object {
        private const val TAG = "MapActivity"
        private const val MIN_TIME_BETWEEN_UPDATES = 5000L // 5 giây
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f // 10 mét

        // Keys cho Intent extras
        const val EXTRA_SELECTED_LATITUDE = "selected_latitude"
        const val EXTRA_SELECTED_LONGITUDE = "selected_longitude"
        const val EXTRA_SELECTED_ADDRESS = "selected_address"
        const val EXTRA_LOCATION_SOURCE = "location_source"
        const val EXTRA_TIMESTAMP = "timestamp"
    }
}