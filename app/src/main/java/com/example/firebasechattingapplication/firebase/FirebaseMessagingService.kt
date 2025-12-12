package com.example.firebasechattingapplication.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService  : FirebaseMessagingService() {
    @Inject
    lateinit var firebaseAuth: FirebaseAuth
    @Inject
    lateinit var firebaseFirestore: FirebaseFirestore
    private val TAG = "FCM_TOKEN_SERVICE"
    private val USERS_COLLECTION = "users" // Collection where user data, including token, is stored

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // This function will attempt to save the new token to Firestore.
        sendRegistrationToServer(token)
    }


    private fun sendRegistrationToServer(token: String) {
        val userId = firebaseAuth.currentUser?.uid

        if (userId == null) {
            // User is not logged in or session is null. Token will be saved upon login.
            Log.w(TAG, "User not logged in. Token not saved yet.")
            return
        }

        val userRef = firebaseFirestore.collection(USERS_COLLECTION).document(userId)

        // Data map containing only the token field to update
        val tokenData = hashMapOf(
            "fcmToken" to token
        )

        userRef.set(tokenData as Map<String, Any>, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "FCM token successfully saved/updated for user $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error saving FCM token for user $userId: $e")
            }
    }

}