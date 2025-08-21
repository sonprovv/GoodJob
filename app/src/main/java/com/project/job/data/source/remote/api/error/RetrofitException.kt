package com.project.job.data.source.remote.api.error

import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.Response // Giả sử bạn có class ErrorResponse

// Giả sử bạn có một data class hoặc class ErrorResponse như sau:
// data class ErrorResponse(val messages: String?, val code: Int?)

class RetrofitException : RuntimeException {

    val errorType: String
    private var httpResponse: Response<*>? = null // Đổi tên để rõ ràng hơn
    private var serverErrorResponse: ErrorResponse? = null // Giữ lại để lưu lỗi từ server

    // Constructor cho lỗi mạng hoặc lỗi không mong muốn (unexpected)
    private constructor(type: String, cause: Throwable) : super(cause.message, cause) {
        this.errorType = type
    }

    // Constructor cho lỗi HTTP từ Retrofit Response
    private constructor(type: String, response: Response<*>) : super("HTTP ${response.code()} ${response.message()}") {
        this.errorType = type
        this.httpResponse = response
    }

    // Constructor cho lỗi cụ thể từ server (đã parse ErrorResponse)
    private constructor(type: String, errorResponse: ErrorResponse) : super(errorResponse.messages) { // Giả sử ErrorResponse có thuộc tính messages
        this.errorType = type
        this.serverErrorResponse = errorResponse
    }

    fun getErrorResponse(): ErrorResponse? = serverErrorResponse

    fun getMessageError(): String? {
        return when (errorType) {
            Type.SERVER -> {
                serverErrorResponse?.messages ?: "Server error with no specific message."
            }
            Type.NETWORK -> {
                // cause đã được truyền cho super class, có thể lấy từ super.cause
                getNetworkErrorMessage(super.cause)
            }
            Type.HTTP -> {
                httpResponse?.code()?.getHttpErrorMessage() ?: "HTTP error with no specific message."
            }
            Type.UNEXPECTED -> {
                // cause đã được truyền cho super class
                super.cause?.message ?: "An unexpected error occurred."
            }
            else -> "Unknown error type."
        }
    }

    private fun getNetworkErrorMessage(throwable: Throwable?): String {
        return when (throwable) {
            is SocketTimeoutException -> "Connection timed out. Please check your internet connection."
            is UnknownHostException -> "Cannot connect to server. Please check your internet connection."
            is IOException -> "Network error. Please check your internet connection."
            else -> throwable?.message ?: "A network error occurred."
        }
    }

    private fun Int.getHttpErrorMessage(): String {
        return when (this) {
            in HttpURLConnection.HTTP_MULT_CHOICE..HttpURLConnection.HTTP_USE_PROXY ->
                // Redirection
                "It was transferred to a different URL. I'm sorry for causing you trouble"
            in HttpURLConnection.HTTP_BAD_REQUEST..HttpURLConnection.HTTP_UNSUPPORTED_TYPE ->
                // Client error
                "An error occurred on the application side. Please try again later!"
            in HttpURLConnection.HTTP_INTERNAL_ERROR..HttpURLConnection.HTTP_VERSION ->
                // Server error
                "A server error occurred. Please try again later!"
            else ->
                // Unofficial error
                "An error occurred ($this). Please try again later!"
        }
    }

    // Định nghĩa object Type bên trong companion object hoặc bên ngoài class
    object Type {
        const val NETWORK = "Network"
        const val HTTP = "Http"
        const val SERVER = "Server" // Lỗi từ server đã được parse
        const val UNEXPECTED = "Unexpected"
    }

    companion object {
        fun toNetworkError(cause: Throwable): RetrofitException {
            return RetrofitException(Type.NETWORK, cause)
        }

        fun toHttpError(response: Response<*>): RetrofitException {
            return RetrofitException(Type.HTTP, response)
        }

        fun toUnexpectedError(cause: Throwable): RetrofitException {
            return RetrofitException(Type.UNEXPECTED, cause)
        }

        // Giả sử ErrorResponse là một class bạn định nghĩa để parse lỗi từ server
        fun toServerError(response: ErrorResponse): RetrofitException {
            return RetrofitException(Type.SERVER, response)
        }
    }
}

// Bạn cần định nghĩa class ErrorResponse ở đâu đó, ví dụ:
data class ErrorResponse(val messages: String?, val code: Int? = null /* hoặc các trường khác */)