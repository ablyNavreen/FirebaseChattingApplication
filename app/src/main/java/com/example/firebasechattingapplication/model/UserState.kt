package com.example.firebasechattingapplication.model

import com.example.firebasechattingapplication.model.dataclasses.User

sealed class UserState {

    object Loading : UserState()
    data class Success(val users: List<User>) : UserState()
    data class Error(val message: String) : UserState()
}