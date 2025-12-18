package com.example.firebasechattingapplication.firebase


import com.example.firebasechattingapplication.model.dataclasses.Data
import com.example.firebasechattingapplication.model.dataclasses.FcmRequestBody
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.model.dataclasses.MessageX
import com.example.firebasechattingapplication.utils.Constants.FCM_BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FcmSender() {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()
    private val api: FcmApiService by lazy {
        Retrofit.Builder()
            .baseUrl(FCM_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FcmApiService::class.java)
    }


    suspend fun sendPushNotification(
        accessToken: String,
        receiverToken: String,
        messageContent: Message,
    ) = withContext(Dispatchers.IO) {

        val requestBody = FcmRequestBody(
            message = MessageX(
                data = Data(
                    sender_gender = messageContent.senderGender.toString(),
                    sender_id = messageContent.senderId.toString(),
                    sender_name = messageContent.senderName.toString(),
                    sender_token = messageContent.senderToken.toString(),
                    message = messageContent.message.toString()),
                notification = null,
                token = receiverToken
            )
        )

        try {
            val authHeader = "Bearer $accessToken"
           api.sendNotification(authHeader, "application/json", requestBody)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Exception during FCM network call: ${e.message}")
        }
    }
}