package com.project.job.ui.map

import android.Manifest
import android.content.Context
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
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.project.job.R
import com.project.job.databinding.ActivityMapBinding
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
        binding?.ivBack?.addFadeClickEffect {
            finish()
        }

        // Log thời gian khởi tạo
        val dateFormat = SimpleDateFormat("hh:mm a zzz, EEEE, dd MMMM yyyy", Locale.getDefault())
        Log.d(TAG, "Started at: ${dateFormat.format(Date())}")

        // Khởi tạo LocationManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setupUI()
        checkLocationPermissions()

    }

    private fun setupUI() {
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

            // Set camera mặc định tại Hà Nội
            mapView.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(105.8542, 21.0285))
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

            // Bắt đầu theo dõi vị trí
            startLocationTracking()

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing map components", e)
            Toast.makeText(this, "Lỗi khởi tạo map: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
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
                runOnUiThread { showLocationOnMap(location, title) }
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
    }
}