package com.project.job.data.repository

import com.project.job.data.model.CheckNotiMes
//import com.client.job.service.RetrofitClient
import retrofit2.Response
//
class NotiMesRepo {
    suspend fun checkFirebase(): Response<CheckNotiMes> {
//        return RetrofitClient.apiService.checkFirebase()
        return Response.success(CheckNotiMes("true", "hihi"))
    }
}