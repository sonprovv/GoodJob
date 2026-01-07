package com.project.job.utils

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.ConnectException
import javax.net.ssl.SSLException

/**
 * Utility class để xử lý error messages một cách tập trung
 */
object ErrorHandler {
    
    /**
     * Xử lý và làm sạch error message, chỉ lấy phần sau dấu "–" hoặc "-"
     */
    private fun cleanErrorMessage(errorMessage: String?): String? {
        if (errorMessage.isNullOrEmpty()) return errorMessage
        
        // Tìm dấu "–" (em dash) hoặc "-" (hyphen) và lấy phần sau
        val dashIndex = errorMessage.indexOf("–").takeIf { it != -1 } 
            ?: errorMessage.indexOf("-").takeIf { it != -1 }

        return if (dashIndex != null && dashIndex != -1 && dashIndex < errorMessage.length - 1) {
            errorMessage.substring(dashIndex + 1).trim()
        } else {
            errorMessage
        }
    }
    
    /**
     * Xử lý lỗi login và trả về message phù hợp
     */
    fun handleLoginError(errorMessage: String?): String {
        val cleanMessage = cleanErrorMessage(errorMessage)
        return when {
            cleanMessage?.contains("Không tìm thấy thông tin người dùng") == true -> 
                "Tài khoản không tồn tại. Vui lòng kiểm tra lại email hoặc đăng ký tài khoản mới."
            
            cleanMessage?.contains("Mật khẩu không đúng") == true -> 
                "Mật khẩu không chính xác. Vui lòng thử lại."
            
            cleanMessage?.contains("Email không đúng định dạng") == true -> 
                "Email không đúng định dạng. Vui lòng nhập email hợp lệ."
            
            cleanMessage?.contains("Tài khoản đã bị khóa") == true -> 
                "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ hỗ trợ."
            
            cleanMessage?.contains("Email đã được sử dụng") == true -> 
                "Email này đã được đăng ký. Vui lòng sử dụng email khác hoặc đăng nhập."
            
            cleanMessage?.contains("Token không hợp lệ") == true -> 
                "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
            
            else -> cleanMessage ?: "Đăng nhập thất bại. Vui lòng thử lại."
        }
    }
    
    /**
     * Xử lý lỗi register và trả về message phù hợp
     */
    fun handleRegisterError(errorMessage: String?): String {
        val cleanMessage = cleanErrorMessage(errorMessage)
        return when {
            cleanMessage?.contains("Email đã được sử dụng") == true ||
            cleanMessage?.contains("The email address is already in use by another account") == true -> 
                "Email này đã được đăng ký. Vui lòng sử dụng email khác hoặc đăng nhập."
            
            cleanMessage?.contains("Email không đúng định dạng") == true -> 
                "Email không đúng định dạng. Vui lòng nhập email hợp lệ."
            
            cleanMessage?.contains("Mật khẩu quá ngắn") == true -> 
                "Mật khẩu phải có ít nhất 6 ký tự."
            
            cleanMessage?.contains("Mật khẩu không khớp") == true || 
            cleanMessage?.contains("Mật khẩu không trùng khớp") == true -> 
                "Mật khẩu xác nhận không khớp. Vui lòng kiểm tra lại."
            
            cleanMessage?.contains("Mật khẩu quá yếu") == true -> 
                "Mật khẩu quá yếu. Vui lòng sử dụng mật khẩu mạnh hơn."
            
            cleanMessage?.contains("Tên người dùng đã tồn tại") == true -> 
                "Tên người dùng đã được sử dụng. Vui lòng chọn tên khác."
            
            cleanMessage?.contains("Số điện thoại đã được sử dụng") == true -> 
                "Số điện thoại này đã được đăng ký. Vui lòng sử dụng số khác."
            
            cleanMessage?.contains("Thông tin không hợp lệ") == true -> 
                "Thông tin đăng ký không hợp lệ. Vui lòng kiểm tra lại."
            
            else -> cleanMessage ?: "Đăng ký thất bại. Vui lòng thử lại."
        }
    }
    
    /**
     * Xử lý lỗi Google Sign-In
     */
    fun handleGoogleSignInError(errorMessage: String?): String {
        val cleanMessage = cleanErrorMessage(errorMessage)
        return when {
            cleanMessage?.contains("Không tìm thấy thông tin người dùng") == true -> 
                "Tài khoản Google chưa được đăng ký. Vui lòng đăng ký tài khoản trước."
            
            cleanMessage?.contains("Token không hợp lệ") == true -> 
                "Phiên đăng nhập Google đã hết hạn. Vui lòng thử lại."
            
            cleanMessage?.contains("Email đã được sử dụng") == true -> 
                "Email này đã được đăng ký bằng phương thức khác. Vui lòng đăng nhập bằng email/mật khẩu."
            
            cleanMessage?.contains("Tài khoản đã bị khóa") == true -> 
                "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ hỗ trợ."
            
            else -> cleanMessage ?: "Đăng nhập Google thất bại. Vui lòng thử lại."
        }
    }
    
    /**
     * Xử lý lỗi HTTP status codes
     */
    fun handleHttpError(errorMessage: String?): String {
        val cleanMessage = cleanErrorMessage(errorMessage)
        return when {
            errorMessage?.contains("HTTP 500") == true -> 
                cleanMessage ?: "Lỗi máy chủ. Vui lòng thử lại sau."
            
            errorMessage?.contains("HTTP 401") == true -> 
                "Thông tin đăng nhập không chính xác."
            
            errorMessage?.contains("HTTP 403") == true -> 
                "Bạn không có quyền truy cập. Vui lòng liên hệ hỗ trợ."
            
            errorMessage?.contains("HTTP 404") == true -> 
                "Không tìm thấy dịch vụ. Vui lòng kiểm tra kết nối mạng."
            
            errorMessage?.contains("HTTP 400") == true -> 
                cleanMessage ?: "Thông tin không hợp lệ. Vui lòng kiểm tra lại."
            
            errorMessage?.contains("HTTP 429") == true -> 
                "Quá nhiều yêu cầu. Vui lòng thử lại sau ít phút."
            
            cleanMessage?.contains("timeout") == true -> 
                "Kết nối quá chậm. Vui lòng thử lại."
            
            cleanMessage?.contains("Unable to resolve host") == true -> 
                "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng."
            
            else -> cleanMessage ?: "Có lỗi xảy ra. Vui lòng thử lại."
        }
    }
    
    /**
     * Xử lý lỗi Exception
     */
    fun handleException(exception: Exception): String {
        return when (exception) {
            is SocketTimeoutException -> 
                "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng và thử lại."
            
            is UnknownHostException -> 
                "Không có kết nối internet. Vui lòng kiểm tra mạng và thử lại."
            
            is ConnectException -> 
                "Không thể kết nối đến máy chủ. Vui lòng thử lại sau."
            
            is SSLException -> 
                "Lỗi bảo mật kết nối. Vui lòng thử lại."
            
            is IllegalArgumentException -> 
                "Thông tin không hợp lệ. Vui lòng kiểm tra lại."
            
            is SecurityException -> 
                "Lỗi bảo mật. Vui lòng thử lại."
            
            else -> 
                exception.message ?: "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại."
        }
    }
    
    /**
     * Xử lý lỗi ChatBot API
     */
    fun handleChatBotError(errorMessage: String?): String {
        val cleanMessage = cleanErrorMessage(errorMessage)
        return when {
            cleanMessage?.contains("Không tìm thấy thông tin người dùng") == true -> 
                "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
            
            cleanMessage?.contains("Bạn không có quyền truy cập") == true -> 
                "Bạn cần đăng nhập với tài khoản worker để tìm kiếm công việc."
            
            cleanMessage?.contains("Không có công việc phù hợp") == true -> 
                "Hiện tại không có công việc phù hợp. Vui lòng thử tìm kiếm với từ khóa khác."
            
            cleanMessage?.contains("Rate limit") == true -> 
                "Quá nhiều yêu cầu. Vui lòng thử lại sau ít phút."
            
            else -> cleanMessage ?: "Có lỗi xảy ra với chatbot. Vui lòng thử lại."
        }
    }
    
    /**
     * Kiểm tra xem có phải lỗi network không
     */
    fun isNetworkError(exception: Exception): Boolean {
        return exception is SocketTimeoutException ||
               exception is UnknownHostException ||
               exception is ConnectException
    }
    
    /**
     * Kiểm tra xem có phải lỗi authentication không
     */
    fun isAuthError(errorMessage: String?): Boolean {
        return errorMessage?.contains("HTTP 401") == true ||
               errorMessage?.contains("Token không hợp lệ") == true ||
               errorMessage?.contains("Phiên đăng nhập đã hết hạn") == true
    }
}