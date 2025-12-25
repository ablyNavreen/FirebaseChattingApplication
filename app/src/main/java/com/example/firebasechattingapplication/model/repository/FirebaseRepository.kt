package com.example.firebasechattingapplication.model.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.firebasechattingapplication.model.AuthState
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.model.dataclasses.OnlineUser
import com.example.firebasechattingapplication.model.dataclasses.User
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.Constants.ONLINE_USERS_COLLECTION
import com.example.firebasechattingapplication.utils.Constants.USERS_COLLECTION
import com.example.firebasechattingapplication.utils.SharedPreferencesHelper.getString
import com.example.firebasechattingapplication.utils.getCurrentUtcDateTimeModern
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirebaseRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore
) {

    //register user
    suspend fun registerUser(email: String, password: String): AuthResult? {
        return firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    //login user
    suspend fun loginUser(email: String, password: String): AuthResult? {
        return firebaseAuth.signInWithEmailAndPassword(
            email,
            password
        )  //pass the email & password to check the user login if account exists or not
            .await()  //pause the coroutine till we get result
    }

    //check current user session
    fun isUserLogged(): FirebaseUser? {
        val currentUser = firebaseAuth.currentUser
        return currentUser
    }

    //save user data to database
    suspend fun addUserToFirestore(user: User) {
        firebaseFirestore.collection(USERS_COLLECTION)
            .document(user.id.toString())
            .set(user).await()
    }

    //send message
/*    suspend fun sendMessageToUser(message: Message) {
        firebaseFirestore.collection(Constants.MESSAGES_COLLECTION)
            .document(message.senderId + message.receiverId)
            .collection(Constants.MESSAGES_SUB_COLLECTION)
            .add(message)
            .await()
    }*/

    suspend fun sendMessageToUser(message: Message) {
        val db = firebaseFirestore
        val parentDocRef = db
            .collection(Constants.MESSAGES_COLLECTION)
            .document(message.senderId + message.receiverId)
        val subCollectionRef = parentDocRef.collection(Constants.MESSAGES_SUB_COLLECTION)
        db.runBatch { batch ->
            //add count to the parent document actual data -> resolve shallow doc issue
            val parentData = mapOf("messagesCount" to message.unreadMessages)
            batch.set(parentDocRef, parentData, SetOptions.merge())
            //add the message to the sub-collection
            val newMessageRef = subCollectionRef.document()
            batch.set(newMessageRef, message)
        }.await()
    }

    //manage active status
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateOnlineStatus(
        context: Context,
        isOnline: Boolean,
        isTyping: Boolean,
        lastSeen: String,
        typingToUserId: String,
        isRecording: Boolean
    ) {
        val userId = getString(context, Constants.USER_ID)
        if (userId.isNullOrEmpty()) {
            Log.e("Firebase", "User ID is null or empty. Cannot update status.")
            return
        }
        val documentRef = firebaseFirestore.collection(ONLINE_USERS_COLLECTION).document(userId)
        try {
            documentRef.update("online", isOnline).await()
            documentRef.update("lastSeen", lastSeen).await()
            documentRef.update("typing", isTyping).await()
            documentRef.update("recording", isRecording).await()
            documentRef.update("typingToUserId", typingToUserId).await()
        } catch (e: Exception) {
            if (e.message?.contains("NOT_FOUND") == true) {
                val newPresenceData = OnlineUser(
                    online = isOnline,
                    id = userId,
                    typing = isTyping,
                    typingToUserId = typingToUserId,
                    lastSeen = getCurrentUtcDateTimeModern(),
                    currentTime = getCurrentUtcDateTimeModern(),
                    name = getString(context, Constants.USER_NAME),
                    email = getString(context, Constants.USER_EMAIL),
                    gender = getString(context, Constants.USER_GENDER)?.toInt())
                documentRef.set(newPresenceData).await()
            }
        }
    }

    //logout
    fun logoutUser() {
        return firebaseAuth.signOut()
    }

    //delete
    suspend fun deleteAccount(): Void? {
        return firebaseAuth.currentUser?.delete()?.await()
    }

    //delete user data
   suspend fun deleteUserData(userId: String?) {
        if (!userId.isNullOrEmpty()) {
            firebaseFirestore.collection(USERS_COLLECTION).document(userId).delete().await()
            firebaseFirestore.collection(ONLINE_USERS_COLLECTION).document(userId).delete().await()
            try {
                // get all room documents from the base 'messages' collection
                val querySnapshot = firebaseFirestore.collection(Constants.MESSAGES_COLLECTION).get().await()
                // filter for rooms that belong to this specific user (e.g., "user1_user2")
                val roomsToDelete = querySnapshot.documents.filter { it.id.contains(userId) }
                if (roomsToDelete.isEmpty()) return
                for (roomDoc in roomsToDelete) {
                    val roomRef = roomDoc.reference
                    // fetch all documents in the 'sub_messages' collection for this room
                    val subMessagesSnapshot = roomRef.collection(Constants.MESSAGES_SUB_COLLECTION)
                        .get()
                        .await()
                    // combine parent reference and child references into one list to delete
                    val allRefsToDelete = subMessagesSnapshot.documents.map { it.reference }.toMutableList()
                    allRefsToDelete.add(roomRef) // Add the parent document last
                    // delete data in chunks of 500 (Firestore Batch Limit) -> error crash
                    allRefsToDelete.chunked(500).forEach { chunk ->
                        val batch = firebaseFirestore.batch()
                        for (ref in chunk)
                            batch.delete(ref)
                        batch.commit().await()
                    }
                }
            } catch (e: Exception) {
            }
        }
    }


    //update message status as read
    fun updateMessageStatus(chatId: String) {
        val documentRef = firebaseFirestore.collection(Constants.MESSAGES_COLLECTION)
            .document(chatId)
            .collection(Constants.MESSAGES_SUB_COLLECTION)
        documentRef.whereEqualTo("read", false)  //remove unnecessary rewrites
            .get().addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    println("No messages found to update.")
                    return@addOnSuccessListener
                }
                val batch: WriteBatch = firebaseFirestore.batch()
                for (document in querySnapshot.documents) {
                    val docRef = document.reference
                    batch.update(docRef, "read", true)
                }
                batch.commit()
                    .addOnSuccessListener {
                        println("Successfully updated all ${querySnapshot.size()} messages to 'true'")
                    }
                    .addOnFailureListener { e -> println("Error committing batch update: $e") }
            }
            .addOnFailureListener { e -> println("Error fetching messages for update: $e") }
    }

    //get messages btw two users
    fun getRealTimeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val query = firebaseFirestore
            .collection(Constants.MESSAGES_COLLECTION)
            .document(chatId)
            .collection(Constants.MESSAGES_SUB_COLLECTION)
        // add real time snapshot Listener
        //MetadataChanges.INCLUDE - calls listener when metadata changes -> device comes online to send pending message
        val subscription = query.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, exception ->
            if (exception != null) {
                Log.e("Firestore", "Listen failed for chat ${exception.message}", exception)
                close(exception)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                //Map the QuerySnapshot to a messages list
                val messages = snapshot.documents.mapNotNull { document ->
                   val msg = document.toObject(Message::class.java)
                    Log.d("askncnscmasc", "getRealTimeMessages: msg = ${msg?.message},has = ${document.metadata.hasPendingWrites()}")
                    msg?.apply {
                        isPending = document.metadata.hasPendingWrites()
                    }
                }
                //send the new list to the Flow collector
                trySend(messages)
            } else if (snapshot != null && snapshot.isEmpty) {
                // Emit an empty list if the collection exists but has no documents
                trySend(emptyList())
            }
        }
        //remove the listener when the flow is cancelled or closed to prevent memory leaks
        awaitClose {
            subscription.remove()
        }
    }

    //get all data in base messages list
    fun getAllMessages(): Flow<List<Message>> = callbackFlow {
        val query = firebaseFirestore
            .collectionGroup(Constants.MESSAGES_SUB_COLLECTION)
        // add real time snapshot Listener
        val subscription = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Log the error and close the flow with the exception
                Log.e(
                    "FirestoreFlow",
                    "Listen failed for chat : ${exception.message}",
                    exception
                )
                close(exception)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                //Map the QuerySnapshot to a List<Message>
                val messages = snapshot.documents.mapNotNull { document ->
                    document.toObject(Message::class.java)
                }
                //end the new list to the Flow collector
                trySend(messages)
            } else if (snapshot != null && snapshot.isEmpty) {
                // Emit an empty list if the collection exists but has no documents
                trySend(emptyList())
            }
        }
        //remove the listener when the flow is cancelled or closed to prevent memory leaks
        awaitClose {
            subscription.remove()
        }
    }

  /*  fun getUserData(): QuerySnapshot? {
        return firebaseFirestore.collection(Constants.USERS_COLLECTION).get().await()
    }*/

    //get active users list
    fun getOnlineUsers(context: Context): Flow<List<OnlineUser>> = callbackFlow {  //flow builder
        //code here runs when observer start observing when .collect() is called

        //create query which points to the collection you want to listen
        val query = firebaseFirestore
            .collection(ONLINE_USERS_COLLECTION)  //fetch data from collection -> online_users
        // add real time snapshot Listener for live data update
        val subscription = query.addSnapshotListener { snapshot, exception ->
            //this code block is called when there is data change
            if (exception != null) {
                close(exception)  //terminates flow in case of exception
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                //map the snapshot (raw data) to a list of users
                val messages = snapshot.documents.mapNotNull { document ->
                    document.toObject(OnlineUser::class.java)
                }
                //emit the new list to the collector
                trySend(messages.filter {
                    //filter the list -> user doesn't see themselves in the active list
                    it.id != getString(context = context, Constants.USER_ID)
                })
            } else if (snapshot != null && snapshot.isEmpty) {
                // Emit an empty list if no docs
                trySend(emptyList())
            }
        }
        //remove the listener when the flow is cancelled or closed to prevent memory leaks
        awaitClose {  //called when collector stops
            subscription.remove()
        }
    }
    fun getUserData(): Flow<List<User>> = callbackFlow {  //flow builder
        val query = firebaseFirestore
            .collection(USERS_COLLECTION)  //fetch data from collection
        val subscription = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                close(exception)  //terminates flow in case of exception
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val messages = snapshot.documents.mapNotNull { document ->
                    document.toObject(User::class.java)
                }
                trySend(messages)
            } else if (snapshot != null && snapshot.isEmpty) {
                trySend(emptyList())
            }
        }
        awaitClose {  //called when collector stops
            subscription.remove()
        }
    }


    fun getAndSaveFCMToken(onTokenReady: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(
                    "kjgehgkhjegrkjhrgk",
                    "Fetching FCM registration token failed",
                    task.exception
                )
                return@addOnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            onTokenReady(token)
        }
    }


    //save user fcm token to firestore
    suspend fun updateFCMToken(userId: String, token: String): AuthState {
        if (userId.isEmpty()) {
            Log.e("Firebase", "User ID is null or empty. Cannot update status.")
            return AuthState.Error("User ID is null")
        }
        val documentRef = firebaseFirestore.collection(USERS_COLLECTION).document(userId)
        val documentRef2 = firebaseFirestore.collection(ONLINE_USERS_COLLECTION).document(userId)
        return try {
            documentRef.update("token", token).await() // .await()
            documentRef2.update("token", token).await() // .await()
            AuthState.Success("Update successful.")
        } catch (e: Exception) {
            AuthState.Error(e.message ?: "Update failed.")
        }

    }
}