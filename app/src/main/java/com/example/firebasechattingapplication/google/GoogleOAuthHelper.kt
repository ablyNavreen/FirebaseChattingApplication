package com.example.firebasechattingapplication.google

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.firebasechattingapplication.view.activities.MainActivity
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

class GoogleOAuthHelper(private val context: Context) {


    // IMPORTANT: Use the Client ID and Redirect URI configured in your console and manifest
    private val CLIENT_ID = "192786858308-8h0oqh00go7b3rn7rgrl33muvm6923u5.apps.googleusercontent.com"
    private val REDIRECT_URI = "com.example.firebasechattingapplication:/oauth2redirect"
    private val SCOPES = listOf(
        "https://www.googleapis.com/auth/firebase.messaging",
    ).joinToString(" ")

    private val authService = AuthorizationService(context)
    private var listener: TokenAcquisitionListener? = null


    fun acquireToken(activity: Activity, listener: TokenAcquisitionListener) {
        this.listener = listener

        val serviceConfiguration = AuthorizationServiceConfiguration(
            Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"),
            Uri.parse("https://oauth2.googleapis.com/token")
        )
        val authRequest = AuthorizationRequest.Builder(
            serviceConfiguration,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URI)
        )
            .setScope(SCOPES)
            .setAdditionalParameters(mapOf("access_type" to "offline")) // Request Refresh Token
            .build()

        val completionIntent = createTokenExchangePendingIntent(activity)
        authService.performAuthorizationRequest(
            authRequest,
            completionIntent,
            completionIntent
        )
    }

    // Creates a PendingIntent that redirects the result to a static method in the Activity/Helper.
    private fun createTokenExchangePendingIntent(activity: Activity): PendingIntent {
        val intent = Intent(activity, MainActivity::class.java)
        return PendingIntent.getActivity(
            activity,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    fun dispose() {
        authService.dispose()
    }
}