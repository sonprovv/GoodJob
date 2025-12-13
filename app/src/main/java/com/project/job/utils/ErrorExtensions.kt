package com.project.job.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import android.view.View

/**
 * Extension functions để hiển thị error messages với suggestions
 */

/**
 * Hiển thị error message với Toast
 */
fun Context.showErrorToast(errorMessage: String?) {
    val message = ErrorHandler.handleChatBotError(errorMessage)
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

/**
 * Hiển thị error message với Snackbar và action suggestion
 */
fun View.showErrorSnackbar(errorMessage: String?, onRetryClick: (() -> Unit)? = null) {
    val message = ErrorHandler.handleChatBotError(errorMessage)
    val suggestion = ErrorHandler.getSuggestionAction(errorMessage)
    
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    
    // Thêm action button nếu có suggestion hoặc retry callback
    when {
        ErrorHandler.isAuthError(errorMessage) -> {
            snackbar.setAction("Đăng nhập lại") {
                // Navigate to login screen
                // This should be handled by the calling activity/fragment
            }
        }
        ErrorHandler.isNetworkError(Exception(errorMessage ?: "")) -> {
            snackbar.setAction("Thử lại") {
                onRetryClick?.invoke()
            }
        }
        onRetryClick != null -> {
            snackbar.setAction("Thử lại") {
                onRetryClick.invoke()
            }
        }
    }
    
    snackbar.show()
}

/**
 * Extension cho Fragment để hiển thị error
 */
fun Fragment.showErrorMessage(errorMessage: String?, onRetryClick: (() -> Unit)? = null) {
    view?.showErrorSnackbar(errorMessage, onRetryClick)
}

/**
 * Kiểm tra xem có cần logout không (khi gặp auth error)
 */
fun String?.shouldLogout(): Boolean {
    return ErrorHandler.isAuthError(this)
}

/**
 * Format error message cho logging
 */
fun String?.toLogFormat(): String {
    return "Error: ${this ?: "Unknown error"} | Suggestion: ${ErrorHandler.getSuggestionAction(this) ?: "None"}"
}