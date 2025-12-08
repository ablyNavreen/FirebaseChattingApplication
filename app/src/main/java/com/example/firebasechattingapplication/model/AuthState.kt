package com.example.firebasechattingapplication.model

sealed class AuthState {

    object Loading : AuthState()
    data class Success(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}