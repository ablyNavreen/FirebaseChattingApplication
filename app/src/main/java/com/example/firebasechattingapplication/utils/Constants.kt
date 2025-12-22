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
    const val OAUTH_ACCESS_TOKEN = "ya29.a0Aa7pCA8ZufIZnSIZmbpz_llM8nEi3DbwhBMZLegOIBAFwBissyBFV-Yvul6KEL1k0YmIJD9BXMVI_LIiBF61_SCpVjb3fdIVIANImEv7J8j_FkozKf0H_QpP85syxnUDtvgY6OhD9fdWWdYWtAq0q4SAsm-CGzJAaeTEeK8BiVEz60eSW-aJPWgVtRvVq3TjEZQpXGsaCgYKAZ0SARMSFQHGX2MiEUfILO_K8sX7hey0_JygGQ0206"
    const val FCM_BASE_URL = "https://fcm.googleapis.com/"
    const val SEND_PUSH = "v1/projects/fir-chattingapplication-1de73/messages:send"

    const val G_PLUS_SCOPE: String = "oauth2:https://www.googleapis.com/auth/plus.me"
    const val USERINFO_SCOPE: String = "https://www.googleapis.com/auth/userinfo.profile"


}