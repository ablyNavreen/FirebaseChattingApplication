package com.example.firebasechattingapplication.model.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.model.dataclasses.OnlineUser
import com.example.firebasechattingapplication.model.dataclasses.User
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.Constants.ONLINE_USERS_COLLECTION
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.utils.getCurrentUtcDateTimeModern
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
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


    suspend fun registerUser(email: String, password: String): AuthResult? {
        return firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun getUserData(): QuerySnapshot? {
        return firebaseFirestore.collection(Constants.USERS_COLLECTION).get().await()
    }

    suspend fun loginUser(email: String, password: String): AuthResult? {
        return firebaseAuth.signInWithEmailAndPassword(email, password)  //pass the email & password to check the user login if account exists or not
            .await()  //pause the coroutine till we get result

    }

    fun isUserLogged(): FirebaseUser? {
        val currentUser = firebaseAuth.currentUser
        return currentUser
    }

    suspend fun addUserToFirestore(user: User) {
        firebaseFirestore.collection(Constants.USERS_COLLECTION).document(user.id.toString())
            .set(user).await()
    }

    suspend fun sendMessageToUser(message: Message) {
        firebaseFirestore.collection(Constants.MESSAGES_COLLECTION)
            .document(message.senderId + message.receiverId)
            .collection(Constants.MESSAGES_SUB_COLLECTION)
            .add(message)
            .await()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateOnlineStatus(
        context: Context,
        isOnline: Boolean,
        isTyping: Boolean,
        lastSeen: String,
        typingToUserId: String
    ) {
        val userId = SpUtils.getString(context, Constants.USER_ID)
        Log.d("ekjfwhkjwehfw", "userId: $userId")
        if (userId.isNullOrEmpty()) {
            Log.e("wekjfbjhebfw", "User ID is null or empty. Cannot update status.")
            return
        }
        val documentRef = firebaseFirestore.collection(ONLINE_USERS_COLLECTION).document(userId)
        try {
            documentRef.update("online", isOnline).await()

            documentRef.update("lastSeen", lastSeen).await()
                documentRef.update("typing", isTyping).await()
                   documentRef.update("typingToUserId", typingToUserId).await()

        } catch (e: Exception) {
            if (e.message?.contains("NOT_FOUND") == true) {
                Log.d("wekjfbjhebfw", "Document not found. Creating new presence document.")
                val newPresenceData = OnlineUser(
                    online = isOnline,
                    id = userId,
                    typing = isTyping,
                    typingToUserId = typingToUserId,
                    lastSeen = getCurrentUtcDateTimeModern(),
                    name = SpUtils.getString(context, Constants.USER_NAME),
                    gender = SpUtils.getString(context, Constants.USER_GENDER)?.toInt()
                )
                documentRef.set(newPresenceData).await()
            }
        }
    }

    suspend fun logoutUser() {
        return firebaseAuth.signOut()
    }


    fun getRealTimeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val query = firebaseFirestore
            .collection(Constants.MESSAGES_COLLECTION)
            .document(chatId)
            .collection(Constants.MESSAGES_SUB_COLLECTION)
        // add real time snapshot Listener
        val subscription = query.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                // Log the error and close the flow with the exception
                Log.e(
                    "FirestoreFlow",
                    "Listen failed for chat $chatId: ${exception.message}",
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
                    it.id != SpUtils.getString(context = context, Constants.USER_ID)
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

}