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
     * Xử lý lỗi login và trả về message phù hợp
     */
    fun handleLoginError(errorMessage: String?): String {
        return when {
            errorMessage?.contains("Không tìm thấy thông tin người dùng") == true -> 
                "Tài khoản không tồn tại. Vui lòng kiểm tra lại email hoặc đăng ký tài khoản mới."
            
            errorMessage?.contains("Mật khẩu không đúng") == true -> 
                "Mật khẩu không chính xác. Vui lòng thử lại."
            
            errorMessage?.contains("Email không đúng định dạng") == true -> 
                "Email không đúng định dạng. Vui lòng nhập email hợp lệ."
            
            errorMessage?.contains("Tài khoản đã bị khóa") == true -> 
                "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ hỗ trợ."
            
            errorMessage?.contains("Email đã được sử dụng") == true -> 
                "Email này đã được đăng ký. Vui lòng sử dụng email khác hoặc đăng nhập."
            
            errorMessage?.contains("Token không hợp lệ") == true -> 
                "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
            
            else -> errorMessage ?: "Đăng nhập thất bại. Vui lòng thử lại."
        }
    }
    
    /**
     * Xử lý lỗi register và trả về message phù hợp
     */
    fun handleRegisterError(errorMessage: String?): String {
        return when {
            errorMessage?.contains("Email đã được sử dụng") == true -> 
                "Email này đã được đăng ký. Vui lòng sử dụng email khác hoặc đăng nhập."
            
            errorMessage?.contains("Email không đúng định dạng") == true -> 
                "Email không đúng định dạng. Vui lòng nhập email hợp lệ."
            
            errorMessage?.contains("Mật khẩu quá ngắn") == true -> 
                "Mật khẩu phải có ít nhất 6 ký tự."
            
            errorMessage?.contains("Mật khẩu không khớp") == true || 
            errorMessage?.contains("Mật khẩu không trùng khớp") == true -> 
                "Mật khẩu xác nhận không khớp. Vui lòng kiểm tra lại."
            
            errorMessage?.contains("Mật khẩu quá yếu") == true -> 
                "Mật khẩu quá yếu. Vui lòng sử dụng mật khẩu mạnh hơn."
            
            errorMessage?.contains("Tên người dùng đã tồn tại") == true -> 
                "Tên người dùng đã được sử dụng. Vui lòng chọn tên khác."
            
            errorMessage?.contains("Số điện thoại đã được sử dụng") == true -> 
                "Số điện thoại này đã được đăng ký. Vui lòng sử dụng số khác."
            
            errorMessage?.contains("Thông tin không hợp lệ") == true -> 
                "Thông tin đăng ký không hợp lệ. Vui lòng kiểm tra lại."
            
            else -> errorMessage ?: "Đăng ký thất bại. Vui lòng thử lại."
        }
    }
    
    /**
     * Xử lý lỗi Google Sign-In
     */
    fun handleGoogleSignInError(errorMessage: String?): String {
        return when {
            errorMessage?.contains("Không tìm thấy thông tin người dùng") == true -> 
                "Tài khoản Google chưa được đăng ký. Vui lòng đăng ký tài khoản trước."
            
            errorMessage?.contains("Token không hợp lệ") == true -> 
                "Phiên đăng nhập Google đã hết hạn. Vui lòng thử lại."
            
            errorMessage?.contains("Email đã được sử dụng") == true -> 
                "Email này đã được đăng ký bằng phương thức khác. Vui lòng đăng nhập bằng email/mật khẩu."
            
            errorMessage?.contains("Tài khoản đã bị khóa") == true -> 
                "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ hỗ trợ."
            
            else -> errorMessage ?: "Đăng nhập Google thất bại. Vui lòng thử lại."
        }
    }
    
    /**
     * Xử lý lỗi HTTP status codes
     */
    fun handleHttpError(errorMessage: String?): String {
        return when {
            errorMessage?.contains("HTTP 500") == true -> 
                "Lỗi máy chủ. Vui lòng thử lại sau."
            
            errorMessage?.contains("HTTP 401") == true -> 
                "Thông tin đăng nhập không chính xác."
            
            errorMessage?.contains("HTTP 403") == true -> 
                "Bạn không có quyền truy cập. Vui lòng liên hệ hỗ trợ."
            
            errorMessage?.contains("HTTP 404") == true -> 
                "Không tìm thấy dịch vụ. Vui lòng kiểm tra kết nối mạng."
            
            errorMessage?.contains("HTTP 400") == true -> 
                "Thông tin không hợp lệ. Vui lòng kiểm tra lại."
            
            errorMessage?.contains("HTTP 429") == true -> 
                "Quá nhiều yêu cầu. Vui lòng thử lại sau ít phút."
            
            errorMessage?.contains("timeout") == true -> 
                "Kết nối quá chậm. Vui lòng thử lại."
            
            errorMessage?.contains("Unable to resolve host") == true -> 
                "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng."
            
            else -> errorMessage ?: "Có lỗi xảy ra. Vui lòng thử lại."
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
        return when {
            errorMessage?.contains("Không tìm thấy thông tin người dùng") == true -> 
                "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
            
            errorMessage?.contains("Bạn không có quyền truy cập") == true -> 
                "Bạn cần đăng nhập với tài khoản worker để tìm kiếm công việc."
            
            errorMessage?.contains("Không có công việc phù hợp") == true -> 
                "Hiện tại không có công việc phù hợp. Vui lòng thử tìm kiếm với từ khóa khác."
            
            errorMessage?.contains("Rate limit") == true -> 
                "Quá nhiều yêu cầu. Vui lòng thử lại sau ít phút."
            
            else -> errorMessage ?: "Có lỗi xảy ra với chatbot. Vui lòng thử lại."
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