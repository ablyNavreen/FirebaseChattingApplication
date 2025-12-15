package com.example.firebasechattingapplication.firebase

import FcmRequest
import FcmResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FcmApiService {
    @POST("/messages:send")
    suspend fun sendNotification(
        @Header("Authorization") serverKey: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: FcmRequest
    ): Response<FcmResponse>
}