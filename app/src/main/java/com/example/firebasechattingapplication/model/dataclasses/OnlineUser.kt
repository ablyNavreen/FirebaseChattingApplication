package com.example.firebasechattingapplication.model.dataclasses

data class OnlineUser (
    val online : Boolean?=null,
    val typing : Boolean?=null,
    val typingToUserId : String?=null,
    val lastSeen : String?=null,
    val name : String?=null,
    val email : String?=null,
    val id : String?=null,
    val gender : Int?=null,
    val currentTime : String?=null,
    val token : String?=null,

)