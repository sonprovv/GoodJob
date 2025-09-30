package com.project.job.data.source.remote

import org.json.JSONException
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

sealed class NetworkResult <out T>{
    data class Success<T> (val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
}

/*try-catch dung lai cho moi call api */
suspend fun <T> safeApiCall(
    call: suspend () -> Response<T>,
): NetworkResult<T> =
    try {
        val res = call()
        // neu call api thanh cong -> tra ve du lieu
        if (res.isSuccessful && res.body() != null) {
            NetworkResult.Success(res.body()!!)
        } else {
            /*
            * giả sử server trả về 400 | 401 | 500 thì json có thể có dạng
            * {
              "code": 401,
              "message": "Invalid credentials"
            }
            *
            * --> khi đó
            * NetworkResult.Error("HTTP ${res.code()} – ${res.message()}") // res.message() thường là "Unauthorized" hoặc "Bad Request"
            *
            * do đó cần parse để lấy message
            *  */

            //parse để lấy message
            val errorMessage = res.errorBody()?.string() // res.errorBody() trả về nội dung phần body lỗi dưới dạng ResponseBody?
                // chuyển thành String JSON dạng sau
                // "{"code":401,"message":"Invalid credentials"}"
                ?.let { body -> //là chuỗi JSON, ví dụ "{"message":"Invalid credentials"}"
                    try { //Cố gắng phân tích chuỗi JSON thành JSONObject
                        val errorJson = JSONObject(body)
                        errorJson.getString("message")
                    }catch (e: JSONException){
                        null
                    }
                } ?: res.message() //Nếu không lấy được message từ errorBody (do bị null hoặc lỗi JSON),
            // thì fallback dùng res.message()
            // – đây là chuỗi thông báo ngắn gọn mặc định từ Retrofit (vd: "Unauthorized")

            NetworkResult.Error("HTTP ${res.code()} – $errorMessage")
        }
    } catch (e: IOException) {
        NetworkResult.Error("Network error: ${e.message}")
    } //catch : loss internet, timeout, socket closed,... -> server khong phan hoi
    catch (e: HttpException) {
        NetworkResult.Error("HTTP exc: ${e.message}")
    } // catch: error Http response dạng 4xx/5xx
    catch (e: Exception) {
        NetworkResult.Error("Unknown: ${e.message}")
    }// catch: các exception khác