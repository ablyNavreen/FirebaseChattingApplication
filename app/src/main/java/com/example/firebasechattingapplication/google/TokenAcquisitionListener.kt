package com.example.firebasechattingapplication.google

interface TokenAcquisitionListener {
    fun onTokenAcquired(accessToken: String, refreshToken: String?, idToken: String?)
    fun onError(errorMessage: String)
}