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
    const val OAUTH_ACCESS_TOKEN = "ya29.a0Aa7pCA-VnCYH0Tx4brC5AFvrRZ6CNwrPMo4V0s-WQs_He5I09YQhlK_mqJAmMwE5XQ-1BDIjRrsmU1onGk38b7IPwuTUH3qBuIp8ZC7Jt0saunMw7s955mogb8rwaVLsdzKDPFW3mknKm4LJU5Krckpaa5ZiZvhIlxcLBzpFEG7VcKaFNXu7fxhP7fKUmChThUXZoGUaCgYKAcwSARMSFQHGX2Mi3dDHkZvyeYsloxGlQOtemw0206"
    const val FCM_BASE_URL = "https://fcm.googleapis.com/"
    const val SEND_PUSH = "v1/projects/fir-chattingapplication-1de73/messages:send"

    const val G_PLUS_SCOPE: String = "oauth2:https://www.googleapis.com/auth/plus.me"
    const val USERINFO_SCOPE: String = "https://www.googleapis.com/auth/userinfo.profile"


}