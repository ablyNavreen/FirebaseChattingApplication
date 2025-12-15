package com.example.firebasechattingapplication.utils

object Constants {

    const val SP_Name = "ChatAppSP"

    const val USERS_COLLECTION = "users"  //base collection-stores the registered users data
    const val MESSAGES_COLLECTION = "messages"  //base collection- store the messages
    const val ONLINE_USERS_COLLECTION = "online_users"  //base collection- collection store the list of users who are active/online
    const val MESSAGES_SUB_COLLECTION = "sub_messages"  //sub collection store the messages between two users

    const val USER_ID = "user_id"
    const val USER_NAME = "user_name"
    const val USER_EMAIL = "user_email"
    const val USER_GENDER = "user_gender"
    const val USER_TOKEN = "user_token"
    const val FIREBASE_SERVER_KEY = "BMiRQWCNKMMiT4GLuoKiHig81J-9U3FmTNLYxzoc5Zf-H6TUNUBvbeb_oD6M9SRl-lJiIb9ENB2iRfqVhgd58G0"
    const val FCM_BASE_URL = "https://fcm.googleapis.com/v1/projects/fir-chattingapplication-1de73/"
}