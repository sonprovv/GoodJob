package com.project.job.data.source.remote

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Extension functions cho NetworkResult để xử lý các trường hợp đặc biệt
 */

/**
 * Kiểm tra xem lỗi có phải là 401 (Unauthorized) không
 */
fun NetworkResult.Error.isUnauthorized(): Boolean {
    return message.contains("HTTP 401")
}

/**
 * Kiểm tra xem lỗi có phải là 403 (Forbidden) không
 */
fun NetworkResult.Error.isForbidden(): Boolean {
    return message.contains("HTTP 403")
}

/**
 * Kiểm tra xem lỗi có phải là network error không
 */
fun NetworkResult.Error.isNetworkError(): Boolean {
    return message.contains("Network error") || message.contains("IOException")
}

/**
 * Enhanced safeApiCall với xử lý đặc biệt cho token refresh
 */
suspend fun <T> safeApiCallWithTokenHandling(
    call: suspend () -> Response<T>,
    onTokenExpired: suspend () -> Unit = {}
): NetworkResult<T> =
    try {
        val res = call()
        
        if (res.isSuccessful && res.body() != null) {
            NetworkResult.Success(res.body()!!)
        } else {
            // Xử lý đặc biệt cho 401/403 errors
            if (res.code() == 401 || res.code() == 403) {
                Log.w("NetworkResult", "Token expired or forbidden, code: ${res.code()}")
                onTokenExpired()
            }
            
            val errorMessage = res.errorBody()?.string()
                ?.let { body ->
                    try {
                        val errorJson = JSONObject(body)
                        errorJson.getString("message")
                    } catch (e: JSONException) {
                        null
                    }
                } ?: res.message()

            NetworkResult.Error("HTTP ${res.code()} – $errorMessage")
        }
    } catch (e: IOException) {
        Log.e("NetworkResult", "Network error: ${e.message}", e)
        NetworkResult.Error("Network error: ${e.message}")
    } catch (e: HttpException) {
        Log.e("NetworkResult", "HTTP exception: ${e.message}", e)
        NetworkResult.Error("HTTP exc: ${e.message}")
    } catch (e: Exception) {
        Log.e("NetworkResult", "Unknown error: ${e.message}", e)
        NetworkResult.Error("Unknown: ${e.message}")
    }

/**
 * Utility function để log NetworkResult
 */
fun <T> NetworkResult<T>.logResult(tag: String, operation: String) {
    when (this) {
        is NetworkResult.Success -> {
            Log.d(tag, "$operation successful: ${data}")
        }
        is NetworkResult.Error -> {
            Log.e(tag, "$operation failed: $message")
        }
    }
}

/**
 * Map NetworkResult to another type
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> NetworkResult.Error(message)
    }
}

/**
 * FlatMap NetworkResult
 */
inline fun <T, R> NetworkResult<T>.flatMap(transform: (T) -> NetworkResult<R>): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> transform(data)
        is NetworkResult.Error -> NetworkResult.Error(message)
    }
}

/**
 * Get data or null
 */
fun <T> NetworkResult<T>.getOrNull(): T? {
    return when (this) {
        is NetworkResult.Success -> data
        is NetworkResult.Error -> null
    }
}

/**
 * Get data or default value
 */
fun <T> NetworkResult<T>.getOrDefault(defaultValue: T): T {
    return when (this) {
        is NetworkResult.Success -> data
        is NetworkResult.Error -> defaultValue
    }
}
