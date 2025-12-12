package com.example.firebasechattingapplication.firebase


import FcmNotification
import FcmRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FcmSender(private val serverKey: String) {

    private val FCM_BASE_URL = "https://fcm.googleapis.com/"

    private val api: FcmApiService by lazy {
        Retrofit.Builder()
            .baseUrl(FCM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FcmApiService::class.java)
    }


    suspend fun sendPushNotification(
        recipientToken: String,
        senderName: String,
        messageContent: String,
        customChatData: Map<String, String>
    ) = withContext(Dispatchers.IO) {

        val requestBody = FcmRequest(
            to = recipientToken,
            notification = FcmNotification(
                title = senderName,
                body = messageContent
            ),
            data = customChatData
        )

        try {
            val authHeader = "key=$serverKey"
            val response = api.sendNotification(authHeader, "application/json", requestBody)
            if (response.isSuccessful && response.body()?.success == 1) {
                println("FCM Push Sent Successfully to token: $recipientToken")
            } else {
                println("FCM Push Failed: Code ${response.code()}, Error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Exception during FCM network call: ${e.message}")
        }
    }
}