package com.example.firebasechattingapplication.model.dataclasses

data class Message(
    val senderId: String?=null,
    val senderName: String?=null,
    val receiverId: String?=null,
    val receiverName: String?=null,
    val message: String?=null,
    val time: String?=null,
    val isRead: Boolean?=null,
    val senderGender: Int?=null,
    val receiverGender: Int?=null,
    val gender: Int?=null,
)