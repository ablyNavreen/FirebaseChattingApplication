package com.example.firebasechattingapplication.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.view.activities.MainActivity
import com.example.firebasechattingapplication.view.fragments.ChatFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var firebaseFirestore: FirebaseFirestore
    private val TAG = "FCM_TOKEN_SERVICE"
    val CHANNEL_ID = "default_channel"
    private lateinit var soundUri: Uri


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (message.data.isNotEmpty()) {
            try {
                if (message.data["sender_id"]?.isNotEmpty() == true) {
                    val senderId = message.data["sender_id"]
                    val senderName = message.data["sender_name"]
                    val senderGender = message.data["sender_gender"]
                    val senderToken = message.data["sender_token"]
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("sender_id", senderId)
                    intent.putExtra("sender_name", senderName)
                    intent.putExtra("sender_gender", senderGender)
                    intent.putExtra("sender_token", senderToken)
                    if (!ChatFragment.isChatOpen)
                        makePush(intent, senderName)
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun makePush(intent: Intent?, senderName: String?) {
        val pendingIntent = PendingIntent.getActivity(this, 0,
            intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        intent?.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        val channelId = CHANNEL_ID
        soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setContentTitle("$senderName sent a message")
            .setContentText("You have a new message.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("You have a new message."))
            .setAutoCancel(true)
            .setSound(soundUri)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
            channel.enableLights(true)
            channel.lightColor = Color.MAGENTA
            channel.setShowBadge(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun sendRegistrationToServer(token: String) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "User not logged in. Token not saved yet.")
            return
        }
        firebaseFirestore.collection(Constants.USERS_COLLECTION).document(userId)
            .update("token", token)
            .addOnSuccessListener {
                Log.d("kjgehgkhjegrkjhrgk", "FCM token successfully saved/updated for user $userId")
            }
            .addOnFailureListener { e ->
                Log.d("kjgehgkhjegrkjhrgk", "Error saving FCM token for user $userId: $e")
            }
    }

}