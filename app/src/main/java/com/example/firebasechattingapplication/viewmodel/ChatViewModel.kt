package com.example.firebasechattingapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firebasechattingapplication.model.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(firebaseRepository: FirebaseRepository) : ViewModel() {

    // Create a new user with a first and last name
  /*  val user = hashMapOf(
        "first" to "Ada",
        "last" to "Lovelace",
        "born" to 1815
    )*/


    fun addChatData(data: HashMap<String, String>){

    }

}