package com.example.firebasechattingapplication.di

import com.example.firebasechattingapplication.firebase.FcmSender
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun getFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }


    @Provides
    @Singleton
    fun getFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }
    @Provides
    @Singleton
    fun getFCMSender(): FcmSender {
        return FcmSender()
    }

    @Provides
    @Singleton
    fun getFirebaseStorage(): FirebaseStorage {
        return Firebase.storage
    }
}