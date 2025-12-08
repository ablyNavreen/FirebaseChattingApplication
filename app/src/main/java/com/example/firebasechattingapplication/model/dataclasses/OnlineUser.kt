package com.example.firebasechattingapplication.model.dataclasses

import com.google.firebase.firestore.PropertyName

data class OnlineUser (
    val online : Boolean?=null,
    val typing : Boolean?=null,
    val lastSeen : String?=null,
    val name : String?=null,
    val id : String?=null,

)