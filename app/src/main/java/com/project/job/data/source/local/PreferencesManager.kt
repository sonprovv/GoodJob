package com.project.job.data.source.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.project.job.data.source.remote.api.response.User

class PreferencesManager(context: Context) {
    internal val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveFCMToken(fcmToken: String) {
        sharedPreferences.edit().putString("fcm_token", fcmToken).apply()
    }
    fun getFCMToken(): String? {
        return sharedPreferences.getString("fcm_token", "")
    }

    fun saveNameAndPhone(name: String, phone: String) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply()
        sharedPreferences.edit().putString(KEY_USER_PHONE, phone).apply()
    }

    fun saveAddress(address: String) {
        sharedPreferences.edit().putString(KEY_USER_LOCATION, address).apply()
    }

    fun saveLocationCoordinates(latitude: Double, longitude: Double) {
        sharedPreferences.edit().apply {
            putFloat(KEY_USER_LATITUDE, latitude.toFloat())
            putFloat(KEY_USER_LONGITUDE, longitude.toFloat())
            apply()
        }
    }

    fun getLocationCoordinates(): Pair<Double, Double>? {
        val latitude = sharedPreferences.getFloat(KEY_USER_LATITUDE, Float.NaN)
        val longitude = sharedPreferences.getFloat(KEY_USER_LONGITUDE, Float.NaN)
        
        return if (!latitude.isNaN() && !longitude.isNaN()) {
            Pair(latitude.toDouble(), longitude.toDouble())
        } else {
            null
        }
    }

    // Lưu token xác thực
    fun saveAuthToken(token: String?) {
        if (!token.isNullOrEmpty()) {
            sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
            Log.d("PreferencesManager", "Auth token saved successfully")
        } else {
            Log.w("PreferencesManager", "Attempted to save null or empty auth token")
        }
    }

    // Lấy token xác thực
    fun getAuthToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }
    
    // Lưu đối tượng User
    fun saveUser(user: User) {
        Log.d("PreferencesManager", "Saving user data: $user")
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, user.uid)
            putString(KEY_USER_EMAIL, user.email)
            // Sử dụng displayName nếu username rỗng
            val displayName = user.username
            putString(KEY_USER_NAME, displayName)
            putString(KEY_USER_AVATAR, user.avatar ?: "")
            putString(KEY_USER_ROLE, user.role)
            putString(KEY_USER_LOCATION, user.location ?: "")
            putString(KEY_USER_GENDER, user.gender ?: "")
            putString(KEY_USER_BIRTHDATE, user.dob ?: "")
            putString(KEY_USER_PHONE, user.tel ?: "")
            putString(KEY_USER_PROVIDER, user.provider ?: "")
            apply()
        }
    }

    // Lưu refresh token
    fun saveRefreshToken(refreshToken: String?) {
        if (!refreshToken.isNullOrEmpty()) {
            sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply()
            Log.d("PreferencesManager", "Refresh token saved successfully")
        } else {
            Log.w("PreferencesManager", "Attempted to save null or empty refresh token")
        }
    }

    // Lấy refresh token
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    // Lưu thời gian hết hạn của token (timestamp)
    fun saveTokenExpirationTime(expirationTime: Long) {
        sharedPreferences.edit().putLong(KEY_TOKEN_EXPIRATION, expirationTime).apply()
        Log.d("PreferencesManager", "Token expiration time saved: ${java.util.Date(expirationTime)}")
    }

    // Lấy thời gian hết hạn của token
    fun getTokenExpirationTime(): Long {
        return sharedPreferences.getLong(KEY_TOKEN_EXPIRATION, 0L)
    }

    // Lưu Google Access Token (từ OAuth)
    fun saveGoogleAccessToken(accessToken: String) {
        sharedPreferences.edit().putString(KEY_GOOGLE_ACCESS_TOKEN, accessToken).apply()
    }

    // Lấy Google Access Token
    fun getGoogleAccessToken(): String? {
        return sharedPreferences.getString(KEY_GOOGLE_ACCESS_TOKEN, null)
    }

    // Kiểm tra trạng thái tokens để debug
    fun getTokensInfo(): String {
        val authToken = getAuthToken()
        val refreshToken = getRefreshToken()
        val googleAccessToken = getGoogleAccessToken()
        val expirationTime = getTokenExpirationTime()
        
        return """
            Auth Token: ${if (authToken.isNullOrEmpty()) "None" else "Present (${authToken.length} chars)"}
            Refresh Token: ${if (refreshToken.isNullOrEmpty()) "None" else "Present (${refreshToken.length} chars)"}
            Google Access Token: ${if (googleAccessToken.isNullOrEmpty()) "None" else "Present (${googleAccessToken.length} chars)"}
            Token Expiration: ${if (expirationTime == 0L) "None" else java.util.Date(expirationTime)}
        """.trimIndent()
    }

    // Xóa tất cả dữ liệu đăng nhập
    fun clearAuthData() {
        sharedPreferences.edit().apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_GOOGLE_ACCESS_TOKEN)
            remove(KEY_TOKEN_EXPIRATION)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_USER_AVATAR)
            remove(KEY_USER_ROLE)
            remove(KEY_USER_LOCATION)
            remove(KEY_USER_GENDER)
            remove(KEY_USER_BIRTHDATE)
            remove(KEY_USER_PHONE)
            remove(KEY_USER_PROVIDER)
            apply()
        }
        Log.d("PreferencesManager", "All auth data cleared")
    }

    // Kiểm tra đã đăng nhập chưa
    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrEmpty()
    }

    // Lấy thông tin người dùng
    fun getUserData(): Map<String, String> {
        return mapOf(
            KEY_USER_ID to (sharedPreferences.getString(KEY_USER_ID, "") ?: ""),
            KEY_USER_EMAIL to (sharedPreferences.getString(KEY_USER_EMAIL, "") ?: ""),
            KEY_USER_NAME to (sharedPreferences.getString(KEY_USER_NAME, "") ?: ""),
            KEY_USER_AVATAR to (sharedPreferences.getString(KEY_USER_AVATAR, "") ?: ""),
            KEY_USER_ROLE to (sharedPreferences.getString(KEY_USER_ROLE, "user") ?: "user"),
            KEY_USER_LOCATION to (sharedPreferences.getString(KEY_USER_LOCATION, "") ?: ""),
            KEY_USER_GENDER to (sharedPreferences.getString(KEY_USER_GENDER, "") ?: ""),
            KEY_USER_BIRTHDATE to (sharedPreferences.getString(KEY_USER_BIRTHDATE, "") ?: ""),
            KEY_USER_PHONE to (sharedPreferences.getString(KEY_USER_PHONE, "") ?: ""),
            KEY_USER_PROVIDER to (sharedPreferences.getString(KEY_USER_PROVIDER, "") ?: "")
        )
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_GOOGLE_ACCESS_TOKEN = "google_access_token"
        private const val KEY_TOKEN_EXPIRATION = "token_expiration"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_AVATAR = "user_avatar"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_LOCATION = "user_location"
        private const val KEY_USER_GENDER = "user_gender"
        private const val KEY_USER_BIRTHDATE = "user_birthdate"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_USER_PROVIDER = "user_provider"
        private const val KEY_USER_LATITUDE = "user_latitude"
        private const val KEY_USER_LONGITUDE = "user_longitude"
    }
}
