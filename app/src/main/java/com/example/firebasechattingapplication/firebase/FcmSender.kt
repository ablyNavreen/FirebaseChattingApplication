package com.example.firebasechattingapplication.firebase


import com.example.firebasechattingapplication.model.dataclasses.Data
import com.example.firebasechattingapplication.model.dataclasses.FcmRequestBody
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.model.dataclasses.MessageX
import com.example.firebasechattingapplication.utils.Constants.FCM_BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FcmSender() {

    private val authInterceptor = Interceptor { chain ->
        // Get the original request
        val originalRequest = chain.request()

        // Build a new request with the headers added
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ya29.a0Aa7pCA-pFUks3tsJH21ptn6-f6XStYbenVpYodk0y2-9H8EKmT4ecVoWcsUjM3TeYPKpN0f-ZX28jP4jrXjRMQr1ssqOSBMxf1TvYIg9ve5t4YJtDPahEd7itQojzU6Al5qB6VOW8B88WD13dnIN6QinMlB0UHgkz7_aSttL3RdwuAY96VUHRXfdJ_h37y8K6yr670oaCgYKAcwSARMSFQHGX2MiuKfvDexTcwKvVZMQthQofg0206")
            .header("Content-Type", "application/json")
            .build()

        // Proceed with the new request
        chain.proceed(newRequest)
    }
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Set the level to BODY to log headers, request, and response payloads
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
//        .addInterceptor(authInterceptor) // Attach the custom interceptor
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
                    sender_token = messageContent.senderToken.toString()),
                notification = null,
                token = receiverToken
            )
        )

        try {
            val authHeader = "Bearer $accessToken"
            val response = api.sendNotification(authHeader, "application/json", requestBody)
           /* if (response.isSuccessful && response.body()?.success == 1) {
                println("FCM Push Sent Successfully to token: $receiverToken")
            } else {
                println("FCM Push Failed: Code ${response.code()}, Error: ${response.errorBody()?.string()}")
            }*/
        } catch (e: Exception) {
            e.printStackTrace()
            println("Exception during FCM network call: ${e.message}")
        }
    }
}