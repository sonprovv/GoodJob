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

    // Lưu token xác thực
    fun saveAuthToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
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
            putString(KEY_USER_PROVIDER, user.google.toString())
            apply()
        }
    }

    // Xóa tất cả dữ liệu đăng nhập
    fun clearAuthData() {
        sharedPreferences.edit().apply {
            remove(KEY_AUTH_TOKEN)
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
    }
}
