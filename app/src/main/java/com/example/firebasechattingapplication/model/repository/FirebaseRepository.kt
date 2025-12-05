package com.example.firebasechattingapplication.model.repository

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(){
    //initialise firestore
    val db = Firebase.firestore


    fun addChatData(data : HashMap<String, String>){

        db.collection("chats")
            .add(data) //add doc to collection
            .addOnSuccessListener { documentReference ->
                Log.d("jhghjghjgh", "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("jhghjghjgh", "Error adding document", e)
            }
    }

    fun readChatData(){
        db.collection("chats")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    fun getChatRoomData(){
        db.collection("chatRoom")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }



}