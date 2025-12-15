package com.example.firebasechattingapplication.viewmodel

import FcmData
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.firebasechattingapplication.firebase.FcmSender
import com.example.firebasechattingapplication.model.AuthState
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.model.dataclasses.OnlineUser
import com.example.firebasechattingapplication.model.dataclasses.User
import com.example.firebasechattingapplication.model.repository.FirebaseRepository
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.utils.getCurrentUtcDateTimeModern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: FirebaseRepository,
    private val fcmSender: FcmSender,
    application: Application
) :
    AndroidViewModel(application = application) {

    private val _authState = MutableLiveData<AuthState>(AuthState.Loading)
    val authState: LiveData<AuthState> = _authState


    fun addUserToFirestore(user: User) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                repository.addUserToFirestore(user)
                _authState.value = AuthState.Success(user.id.toString())
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun registerUser(userData: User) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                //pass email & password to repo method for registeration
                val result = repository.registerUser(userData.email.toString(), userData.password.toString())
                //after sucessfull registeration save user info to firestore
                repository.getAndSaveFCMToken { token ->  //fetch fcm token

                    Log.d("tokennnnnn", "registerUser: $token")
                    val user = User(
                        name = userData.name.toString(),
                        email = userData.email, gender = userData.gender,
                        password = userData.password,
                        id = result?.user?.uid ?: "",
                        currentTime = getCurrentUtcDateTimeModern(),
                        token = token)
                    addUserToFirestore(user)
                }

            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun loginUser(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                //hit repo method to check user login
                val result = repository.loginUser(email, password)
                SpUtils.saveString(application, Constants.USER_ID, result?.user?.uid)
                updateFCMToken()
//                _authState.value = AuthState.Success(result?.user?.uid ?: "")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }


    fun sendMessageToUser(message: Message) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                repository.sendMessageToUser(message)
                sendNotificationToUser(message, message.receiverToken?:"")
                _authState.value = AuthState.Success("")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }


    fun isUserLogged() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val result = repository.isUserLogged()
                if (result != null)
                    _authState.value = AuthState.Success(result.uid ?: "")
                else
                    _authState.value = AuthState.Error("Login Failed")
            } catch (e: java.lang.Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun logoutUser(context: Context) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                repository.logoutUser()
                SpUtils.cleanPref(application)
                _authState.value = AuthState.Success("Logged out successfully")
            } catch (e: java.lang.Exception) {
                _authState.value = AuthState.Error(e.message ?: "Logout failed")
            }
        }
    }

    // Expose the messages as a StateFlow for the UI
    fun getMessages(chatId: String, chatId2: String): StateFlow<List<Message>> {
        // fetch the lists based on both chat ids
        val flow1 = repository.getRealTimeMessages(chatId)
        val flow2 = repository.getRealTimeMessages(chatId2)
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
        val flow = repository.getAllMessages()
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
    fun getUserData(): StateFlow<List<User>> {
        val flow = repository.getUserData()
        return flow
            .catch { e ->
                Log.d("wkqjwqhw", "Error: ${e.message}", e)
            }
            .stateIn(  //coverts to a hot StateFlow
                scope = viewModelScope,   //resists configuration changes
                started = SharingStarted.WhileSubscribed(5000),  //connection waits for observer till 5 sec othweise shut down
                initialValue = emptyList() //initially list is empty to eliminate null
            )
    }


    fun getOnlineUsers(): StateFlow<List<OnlineUser>> {

        val flow = repository.getOnlineUsers(application)
        return flow
            .catch { e ->
                Log.e("ChatFlow", "Error: ${e.message}", e)
            }
            .stateIn(  //coverts to a hot StateFlow
                scope = viewModelScope,   //resists configuration changes
                started = SharingStarted.WhileSubscribed(5000),  //connection waits for observer till 5 sec othweise shut down
                initialValue = emptyList() //initially list is empty to eliminate null
            )
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
            repository.updateOnlineStatus(application, isOnline, isTyping, lastSeen, typingToUserId)
            emit(AuthState.Success("Status updated."))
        } catch (e: Exception) {
            emit(AuthState.Error(e.message ?: "Failed to update status."))
        }
    }

    fun updateMessageStatus(
        context: Context,
        chatId: String
    ): Flow<AuthState> = flow {
        emit(AuthState.Loading)
        try {
            repository.updateMessageStatus(chatId)
            emit(AuthState.Success("Status updated."))
        } catch (e: Exception) {
            emit(AuthState.Error(e.message ?: "Failed to update status."))
        }
    }

    fun sendNotificationToUser(message: Message, recipientFCMToken: String) {
        viewModelScope.launch {
            fcmSender.sendPushNotification(
                recipientToken = recipientFCMToken,
                senderName = SpUtils.getString(application, Constants.USER_NAME) ?: "",
                messageContent = message.message ?: "...",
                customChatData = FcmData( senderId = message.senderId?:"",
                    senderToken = message.senderToken?:"",
                    senderGender = message.senderGender.toString(),
                    senderName = message.senderName?:""),
                /*customChatData = mapOf(
                    "chatId" to message.receiverId!! + message.senderId!!,
                    "senderId" to message.senderId!!
                )*/
            )
        }

    }

    fun updateFCMToken() {
        _authState.value = AuthState.Loading
        repository.getAndSaveFCMToken { token->
            Log.d("tokennnnnn", "updateFCMToken: $token")
            viewModelScope.launch {
                val status = repository.updateFCMToken(SpUtils.getString(application, Constants.USER_ID)?:""
                    , token)

                when (status) {
                    is AuthState.Success -> {
                        SpUtils.saveString(application, Constants.USER_TOKEN, token)
                        _authState.value = AuthState.Success("")
                    }
                    is AuthState.Error -> {
                        _authState.value = AuthState.Error("Failed fetching user token.")
                    }
                    else -> {}
                }
            }
        }
    }

    fun uploadImage(imageUri: String, message: Message) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
//                val imageUrl = repository.uploadImage(imageUri, message.senderId)
                    val options = BitmapFactory.Options().apply { inSampleSize = 2 }
                    val bitmap = BitmapFactory.decodeFile(imageUri, options)
                    val outputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val imageBytes = outputStream.toByteArray()
                    sendMessageToUser(message = message.copy(image= Base64.encodeToString(imageBytes, Base64.NO_WRAP) ))
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Image upload failed")
            }
        }
    }
}