package com.example.firebasechattingapplication.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firebasechattingapplication.model.AuthState
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.model.dataclasses.OnlineUser
import com.example.firebasechattingapplication.model.dataclasses.User
import com.example.firebasechattingapplication.model.repository.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authenticationRepository: AuthenticationRepository) :
    ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Loading)
    val authState: LiveData<AuthState> = _authState


    private val _state = MutableLiveData<AuthState>(AuthState.Loading)
    val state: LiveData<AuthState> = _state


    fun registerUser(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = authenticationRepository.registerUser(email, password)
                Log.d("wekjfbjhebfw", "registerUser: ${result?.user}")
                _authState.value = AuthState.Success(result?.user?.uid ?: "")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    suspend fun getUserData(): List<User>? {
        return try {
            val snapshot = authenticationRepository.getUserData()
            snapshot?.documents?.mapNotNull { document ->
                document.toObject(User::class.java)
            }
        } catch (e: Exception) {
            Log.e("wekjfbjhebfw", "Error fetching users: ${e.message}")
            null
        }
    }

    fun loginUser(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = authenticationRepository.loginUser(email, password)
                _authState.value = AuthState.Success(result?.user?.uid ?: "")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun addUserToFirestore(user: User) {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = authenticationRepository.addUserToFirestore(user)
                Log.d("wekjfbjhebfw", "addUserToFirestore: $result")
                _state.value = AuthState.Success(user.id.toString())
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun updateOnlineStatusFlow(
        context: Context,
        isOnline: Boolean,
        isTyping: Boolean,
        lastSeen: String,
        typingToUserId: String
    ): Flow<AuthState> = flow {
        emit(AuthState.Loading)
        try {
            authenticationRepository.updateOnlineStatus(context, isOnline, isTyping, lastSeen,typingToUserId)
            emit(AuthState.Success("Status updated."))
        } catch (e: Exception) {
            emit(AuthState.Error(e.message ?: "Failed to update status."))
        }
    }

    fun sendMessageToUser(message: Message) {
        _state.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = authenticationRepository.sendMessageToUser(message)
                Log.d("wekjfbjhebfw", "sendMessageToUser: $result")
                _state.value = AuthState.Success("")
            } catch (e: Exception) {
                _state.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }


    fun isUserLogged() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = authenticationRepository.isUserLogged()
                if (result != null)
                    _authState.value = AuthState.Success(result?.uid ?: "")
                else
                    _authState.value = AuthState.Error("Login Failed")
            } catch (e: java.lang.Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun logoutUser() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = authenticationRepository.logoutUser()
                if (result != null)
                    _authState.value = AuthState.Success("Logged out successfully")
                else
                    _authState.value = AuthState.Error("Logout Failed")
            } catch (e: java.lang.Exception) {
                _authState.value = AuthState.Error(e.message ?: "Logout failed")
            }
        }
    }


    // Expose the messages as a StateFlow for the UI
    fun getMessages(chatId: String, chatId2: String): StateFlow<List<Message>> {
        // fetch the lists based on both chat ids
        val flow1 = authenticationRepository.getRealTimeMessages(chatId)
        val flow2 = authenticationRepository.getRealTimeMessages(chatId2)
        //combine the flows
        val combinedFlow = combine(flow1, flow2) { messages1, messages2 ->
            //whenerver there is change in list by flow1 or flow2 -> we get new list
            val combined = messages1 + messages2
           //combined list is sorted and returned
            combined.sortedBy { it.time }
        }
        // convert the resulting Flow into a StateFlow
        return combinedFlow
            .catch { e ->
                Log.e("ChatFlow", "Error combining messages: ${e.message}", e)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList() //initially list is empty
            )
    }
    fun getAllMessages(): StateFlow<List<Message>> {
        // fetch the lists based on both chat ids
        val flow = authenticationRepository.getAllMessages()
        // convert the resulting Flow into a StateFlow
        return flow
            .catch { e ->
                Log.e("ChatFlow", "Error combining messages: ${e.message}", e)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList() //initially list is empty
            )
    }
 fun getOnlineUsers(context: Context): StateFlow<List<OnlineUser>> {

        val flow = authenticationRepository.getOnlineUsers(context)
        return flow
            .catch { e ->
                Log.e("ChatFlow", "Error: ${e.message}", e)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList() //initially list is empty
            )
    }

}