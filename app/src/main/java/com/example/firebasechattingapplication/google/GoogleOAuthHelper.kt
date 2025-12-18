package com.example.firebasechattingapplication.google

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.firebasechattingapplication.google.TokenAcquisitionListener
import com.example.firebasechattingapplication.view.activities.MainActivity
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

class GoogleOAuthHelper(private val context: Context) {

    private val TAG = "GoogleOAuthHelper"

    // IMPORTANT: Use the Client ID and Redirect URI configured in your console and manifest
    private val CLIENT_ID = "192786858308-8h0oqh00go7b3rn7rgrl33muvm6923u5.apps.googleusercontent.com"
    private val REDIRECT_URI = "com.example.firebasechattingapplication:/oauth2redirect"
    private val SCOPES = listOf(
        "https://www.googleapis.com/auth/firebase.messaging",
//        "openid",
//        "email"
    ).joinToString(" ")

    private val authService = AuthorizationService(context)
    private var listener: TokenAcquisitionListener? = null


    fun acquireToken(activity: Activity, listener: TokenAcquisitionListener) {
        this.listener = listener

        // 1. Discover the Google OAuth endpoints
        val serviceConfiguration = AuthorizationServiceConfiguration(
            Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"),
            Uri.parse("https://oauth2.googleapis.com/token")
        )

        // 2. Build the Authorization Request (requesting the Auth Code and Refresh Token)
        val authRequest = AuthorizationRequest.Builder(
            serviceConfiguration,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URI)
        )
            .setScope(SCOPES)
            .setAdditionalParameters(mapOf("access_type" to "offline")) // Request Refresh Token
            .build()

        // 3. Define the intent that starts the token exchange process upon redirect
        val completionIntent = createTokenExchangePendingIntent(activity)

        // 4. Start the browser flow
        authService.performAuthorizationRequest(
            authRequest,
            completionIntent,
            completionIntent // Same intent for cancellation
        )
    }

    // Creates a PendingIntent that redirects the result to a static method in the Activity/Helper.
    private fun createTokenExchangePendingIntent(activity: Activity): PendingIntent {
        // 1. Target the TokenExchangeActivity
        val intent = Intent(activity, MainActivity::class.java)

        // 2. Use PendingIntent.getActivity
        return PendingIntent.getActivity(
            activity,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    // Cleanup
    fun dispose() {
        authService.dispose()
    }
}