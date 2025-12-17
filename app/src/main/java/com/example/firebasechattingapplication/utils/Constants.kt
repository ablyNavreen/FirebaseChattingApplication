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
    const val OAUTH_ACCESS_TOKEN = "ya29.a0Aa7pCA-spt21CszJJ0aCknabgNu-o-KiV9fsssvNNwrdg8AtDR133WS-gTpffQnIxF9r25Acq4gc5GVjr5ZfNUDOP_dClNs7JHlBpOK68B9iIJRPziOY6sS81ZsDI0oIU559LUbiGQm05HipS7WFOYiNDlSPQd8kVpYLqeI9Ru9kJ1IOp20-E__yhZyF_byxcqAmjjUaCgYKAfoSARMSFQHGX2MicVDxOrdns8nRInmW_y55YA0206"
    const val FCM_BASE_URL = "https://fcm.googleapis.com/"
    const val SEND_PUSH = "v1/projects/fir-chattingapplication-1de73/messages:send"

    const val G_PLUS_SCOPE: String = "oauth2:https://www.googleapis.com/auth/plus.me"
    const val USERINFO_SCOPE: String = "https://www.googleapis.com/auth/userinfo.profile"


}