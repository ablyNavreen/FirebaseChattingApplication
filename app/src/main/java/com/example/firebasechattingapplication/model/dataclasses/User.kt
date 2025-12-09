package com.example.firebasechattingapplication.model.dataclasses

import com.google.firebase.firestore.DocumentId

data class User (
    val id : String?=null,
    val name : String?=null,
    val email : String?=null,
    val gender : Int?=null,  //0-male, //1-female
    val password : String?=null

)