# Hướng dẫn triển khai Refresh Token tự động

## Tổng quan
Đã triển khai thành công cơ chế tự động refresh token khi gặp lỗi 403 (Forbidden) do accessToken hết hạn. Hệ thống sẽ tự động:

1. Phát hiện khi accessToken hết hạn (HTTP 403)
2. Tự động gọi API refresh token để lấy token mới
3. Retry request ban đầu với token mới
4. Tự động logout và chuyển về màn hình đăng nhập nếu refresh token cũng hết hạn

## Các thành phần đã triển khai

### 1. **ApiTokenManager.kt** (data/manager/)
- Quản lý việc refresh API token một cách tập trung
- Sử dụng Mutex để tránh race condition khi có nhiều request đồng thời
- Tự động lưu token mới sau khi refresh thành công
- Callback khi token expired để thông báo cho AuthenticationManager

**Cách sử dụng:**
```kotlin
val apiTokenManager = ApiTokenManager(tokenRepository) {
    // Callback khi token expired
    authenticationManager.onTokenExpired()
}

// Refresh token an toàn
val result = apiTokenManager.refreshTokenSafely()
```

### 1.1. **TokenManagerIntegration.kt**
- Quản lý API Access/Refresh Token từ backend một cách tập trung
- Cung cấp interface thống nhất để làm việc với API tokens
- Tích hợp với AuthenticationManager để xử lý logout tự động

**Cách sử dụng:**
```kotlin
val tokenManager = RetrofitClient.tokenManager

// Lấy API access token
val apiToken = tokenManager.getCurrentAccessToken()

// Refresh token an toàn
val refreshResult = tokenManager.refreshTokenSafely()

// Kiểm tra trạng thái authentication
val authStatus = tokenManager.getAuthenticationStatus()

// Debug info
val debugInfo = tokenManager.getDebugInfo()
```

### 2. **AuthenticationManager.kt**
- Quản lý trạng thái đăng nhập của ứng dụng
- Tự động logout và chuyển về màn hình đăng nhập khi token hết hạn
- Sử dụng StateFlow để theo dõi trạng thái authentication
- Singleton pattern để đảm bảo chỉ có một instance

**Cách sử dụng:**
```kotlin
// Lấy instance
val authManager = AuthenticationManager.getInstance(context, tokenRepository)

// Theo dõi trạng thái đăng nhập
authManager.isAuthenticated.collect { isAuthenticated ->
    if (!isAuthenticated) {
        // User đã logout
    }
}

// Theo dõi trạng thái token expired
authManager.tokenExpired.collect { expired ->
    if (expired) {
        // Token đã hết hạn, hiển thị thông báo
    }
}
```

### 3. **AuthInterceptor.kt** (Đã cập nhật)
- Tự động thêm Authorization header cho các request cần authentication
- Tự động phát hiện lỗi 403 và trigger refresh token
- Retry request với token mới sau khi refresh thành công
- Tích hợp với TokenManagerIntegration và AuthenticationManager

### 4. **NetworkResultExtensions.kt**
- Extension functions để xử lý NetworkResult dễ dàng hơn
- Utility functions để kiểm tra loại lỗi (401, 403, network error)
- Enhanced safeApiCall với xử lý đặc biệt cho token refresh

**Cách sử dụng:**
```kotlin
// Kiểm tra loại lỗi
if (result is NetworkResult.Error) {
    when {
        result.isUnauthorized() -> // 401 error
        result.isForbidden() -> // 403 error  
        result.isNetworkError() -> // Network error
    }
}

// Map result
val mappedResult = result.map { data -> 
    // Transform data
}

// Get data or null
val data = result.getOrNull()
```

### 5. **API Changes**
- **ApiService.kt**: Sửa endpoint refreshToken để sử dụng đúng request object
- **TokenRepository.kt**: Thêm method refreshAccessToken()
- **UserRemote.kt**: Thêm method refreshToken()
- **UserDataSource.kt**: Thêm interface method refreshToken()

## Cách hoạt động

### Flow khi gặp lỗi 403:
1. **AuthInterceptor** phát hiện response 403
2. Gọi **TokenManagerIntegration.refreshTokenSafely()** 
3. **ApiTokenManager** gọi API refresh token
4. Nếu thành công: lưu token mới và retry request ban đầu
5. Nếu thất bại: gọi callback **AuthenticationManager.onTokenExpired()**
6. **AuthenticationManager** clear tokens và chuyển về màn hình đăng nhập

### Tránh Race Condition:
- Sử dụng **Mutex** trong ApiTokenManager
- Chỉ có một refresh token request tại một thời điểm
- Các request khác sẽ đợi cho đến khi refresh hoàn thành

## Cách tích hợp vào ứng dụng

### 1. Khởi tạo trong Application class:
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Khởi tạo RetrofitClient với context
        RetrofitClient.initialize(this)
    }
}
```

### 2. Sử dụng trong Activity/Fragment:
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var authManager: AuthenticationManager
    private lateinit var tokenManager: TokenManagerIntegration
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Lấy managers
        authManager = RetrofitClient.authManager
        tokenManager = RetrofitClient.tokenManager
        
        // Setup token expired listener (tự động unregister khi activity destroy)
        setupTokenExpiredListener(this) {
            // Token đã hết hạn, chuyển về màn hình đăng nhập
            showTokenExpiredDialog()
        }
        
        // Theo dõi trạng thái authentication
        lifecycleScope.launch {
            authManager.isAuthenticated.collect { isAuthenticated ->
                if (!isAuthenticated) {
                    // Chuyển về màn hình đăng nhập
                }
            }
        }
        
        // Theo dõi token expired
        lifecycleScope.launch {
            authManager.tokenExpired.collect { expired ->
                if (expired) {
                    // Hiển thị thông báo "Phiên đăng nhập đã hết hạn"
                    showTokenExpiredDialog()
                }
            }
        }
        
        // Debug token status
        lifecycleScope.launch {
            val debugInfo = tokenManager.getDebugInfo()
            Log.d("MainActivity", debugInfo)
        }
    }
    
    private fun showTokenExpiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Phiên đăng nhập hết hạn")
            .setMessage("Vui lòng đăng nhập lại để tiếp tục sử dụng ứng dụng")
            .setPositiveButton("Đăng nhập lại") { _, _ ->
                // Chuyển về màn hình đăng nhập
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setCancelable(false)
            .show()
    }
}
```

### 3. Xử lý đăng nhập thành công:
```kotlin
// Trong LoginViewModel hoặc LoginFragment
private fun onLoginSuccess() {
    // Thông báo cho AuthenticationManager
    RetrofitClient.authManager.onLoginSuccess()
}
```

### 4. Xử lý logout:
```kotlin
private fun logout() {
    lifecycleScope.launch {
        RetrofitClient.authManager.logout()
    }
}
```

## Lưu ý quan trọng

### 1. **Cập nhật LoginActivity path**
Trong `AuthenticationManager.kt`, cần cập nhật đúng package name và class name:
```kotlin
val loginIntent = Intent().apply {
    setClassName(context, "com.project.job.ui.auth.LoginActivity") // Cập nhật đúng path
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
}
```

### 2. **Xử lý UI khi token expired**
Nên hiển thị dialog thông báo cho user biết phiên đăng nhập đã hết hạn:
```kotlin
private fun showTokenExpiredDialog() {
    AlertDialog.Builder(this)
        .setTitle("Phiên đăng nhập hết hạn")
        .setMessage("Vui lòng đăng nhập lại để tiếp tục sử dụng ứng dụng")
        .setPositiveButton("Đăng nhập lại") { _, _ ->
            // Chuyển về màn hình đăng nhập
        }
        .setCancelable(false)
        .show()
}
```

### 3. **Testing**
Để test cơ chế refresh token:
1. Đăng nhập và lấy token
2. Đợi token hết hạn hoặc manually set token cũ
3. Thực hiện một API call cần authentication
4. Verify rằng hệ thống tự động refresh và retry

### 4. **Logging**
Tất cả các thành phần đều có logging chi tiết với tag riêng:
- `TokenManager`: Log quá trình refresh token
- `AuthInterceptor`: Log việc thêm header và xử lý 403
- `AuthenticationManager`: Log trạng thái authentication

## Kết luận

Cơ chế refresh token đã được triển khai hoàn chỉnh với:
- ✅ **Tự động phát hiện token hết hạn** (HTTP 403)
- ✅ **Tự động refresh token** sử dụng refresh token
- ✅ **Retry request** với token mới sau khi refresh thành công
- ✅ **Tự động logout** khi refresh token hết hạn
- ✅ **Thread-safe** với Mutex pattern
- ✅ **Quản lý trạng thái authentication** với StateFlow
- ✅ **Logging chi tiết** cho debugging
- ✅ **Extension functions** để xử lý lỗi dễ dàng
- ✅ **Đơn giản hóa** - chỉ quản lý API tokens, không cần Firebase tokens

### Cấu trúc cuối cùng:
```
📁 data/manager/
  ├── ApiTokenManager.kt           // Quản lý API refresh token với Mutex
  ├── TokenManagerIntegration.kt   // Interface thống nhất cho API tokens  
  └── AuthenticationManager.kt     // Quản lý trạng thái đăng nhập

📁 data/source/remote/
  ├── interceptor/AuthInterceptor.kt // Tự động refresh khi 403
  └── NetworkResultExtensions.kt    // Utility functions
```

Hệ thống sẽ hoạt động **hoàn toàn tự động** và **trong suốt** - user sẽ không cảm nhận được việc refresh token diễn ra ngầm!
