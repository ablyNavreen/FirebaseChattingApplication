package com.example.firebasechattingapplication.model.dataclasses

data class Message(
    val senderId: String?=null,
    val senderName: String?=null,
    val receiverId: String?=null,
    val receiverName: String?=null,
    val message: String?=null,
    val time: String?=null,
    val read: Boolean?=null,
    val senderGender: Int?=null,
    val receiverGender: Int?=null,
    val gender: Int?=null,
    val unreadMessages: Int?=null,
    val sentByMe: Boolean?=null,
    val senderToken: String?=null,
    val receiverToken: String?=null,
    val image: String?=null
)