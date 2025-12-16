package com.example.firebasechattingapplication.firebase

import com.example.firebasechattingapplication.model.dataclasses.FcmRequestBody
import com.example.firebasechattingapplication.utils.Constants.SEND_PUSH
import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FcmApiService {
    @POST(SEND_PUSH)
    suspend fun sendNotification(
        @Header("Authorization") serverKey: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: FcmRequestBody
    ): JsonElement
}