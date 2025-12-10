package com.example.firebasechattingapplication.view.fragments

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.firebasechattingapplication.databinding.FragmentChatScreenBinding
import com.example.firebasechattingapplication.model.AuthState
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.model.dataclasses.OnlineUser
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.ProgressIndicator
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.utils.getCurrentUtcDateTimeModern
import com.example.firebasechattingapplication.utils.gone
import com.example.firebasechattingapplication.utils.showToast
import com.example.firebasechattingapplication.utils.toLastSeenTime
import com.example.firebasechattingapplication.utils.toTimestampMillis
import com.example.firebasechattingapplication.utils.visible
import com.example.firebasechattingapplication.view.adapters.MessagesAdapter
import com.example.firebasechattingapplication.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


const val TYPING_TIMEOUT_MS = 1500L // 1.5 seconds

@AndroidEntryPoint
class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatScreenBinding
    private val authViewModel: AuthViewModel by viewModels()
    var receiverId: String? = ""
    var receiverName: String? = ""
    var receiverGender: Int? = null
    private val messages = ArrayList<Message>()
    private var messagesAdapter: MessagesAdapter? = null
    private val onlineUser = ArrayList<OnlineUser>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatScreenBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        receiverId = requireArguments().getString(Constants.USER_ID)
        receiverName = requireArguments().getString(Constants.USER_NAME)
        receiverGender = requireArguments().getInt(Constants.USER_GENDER)
        binding.tv.text = receiverName
        Log.d("kwejfkjwehfkjehfw", "onViewCreated: $receiverId")
        setChatsAdapter()
        getMessages(SpUtils.getString(requireContext(), Constants.USER_ID), receiverId)
        getActiveUsers()
        setUpClickListeners()
        setupTypingDetector()
    }

    private fun getActiveUsers() {
        authViewModel.getOnlineUsers(requireContext())
            .onEach { messageList ->
                onlineUser.clear()
                onlineUser.addAll(messageList)
                for (m in messageList)
                    if (m.id == receiverId)
                        if (m.typing == true && m.typingToUserId == SpUtils.getString(requireContext(), Constants.USER_ID))
                            binding.lastSeenTV.text = "typing..."
                        else if (m.online == true)
                            binding.lastSeenTV.text = "Online"
                        else
                            binding.lastSeenTV.text =
                                m.lastSeen?.toTimestampMillis()?.toLastSeenTime(requireContext())
            }
            .catch { e ->
                showToast("Error loading messages.")
            }.launchIn(viewLifecycleOwner.lifecycleScope)  //starts collection -> tied to view
    }

    private fun getMessages(
        senderId: String?,
        receiverId: String?
    ) { // Added receiverId for clarity
        if (senderId == null) {
            //user id is null
            return
        }

        authViewModel.getMessages(
            chatId = senderId + receiverId,
            chatId2 = receiverId + senderId
        )
            .onEach { messageList ->
                messages.clear()
                messages.addAll(messageList)
                messagesAdapter?.notifyDataSetChanged()
                if (messageList.isNotEmpty()) {
                    binding.noMessagesTV.gone()
                    binding.messagesRV.smoothScrollToPosition(messageList.size - 1)
                } else {
                    binding.noMessagesTV.visible()
                }
            }
            .catch { e ->
                Log.e("Chat", "Error collecting combined messages: ${e.message}")
                showToast("Error loading messages.")
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)  //starts collection -> tied to view
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpClickListeners() {
        binding.backIV.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.sendBT.setOnClickListener {
            if (binding.messageET.text.toString().trim().isNotEmpty()) {
                val message = Message(
                    senderId = SpUtils.getString(requireContext(), Constants.USER_ID),
                    receiverId = receiverId,
                    senderName = SpUtils.getString(requireContext(), Constants.USER_NAME),
                    receiverName = receiverName,
                    message = binding.messageET.text.toString().trim(),
                    time = getCurrentUtcDateTimeModern(),
                    isRead = false,
                    gender =  SpUtils.getString(requireContext(), Constants.USER_GENDER)?.toInt())
                Log.d("tyry4utyiuyui", "setUpClickListeners: $message")
                authViewModel.sendMessageToUser(message)
                authViewModel.authState.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        is AuthState.Error -> {
                            ProgressIndicator.hide()
                            showToast(state.message)
                        }

                        AuthState.Loading -> {
                            ProgressIndicator.show(requireContext())
                        }

                        is AuthState.Success -> {
                            binding.messageET.text=null
                            messagesAdapter?.notifyDataSetChanged()
                            binding.noMessagesTV.gone()
                            ProgressIndicator.hide()
                        }
                    }
                }
            }
        }
    }

    private fun setChatsAdapter() {
        messagesAdapter = MessagesAdapter(requireContext(), messages = messages)
        binding.messagesRV.adapter = messagesAdapter
    }


    private val typingHandler = Handler(Looper.getMainLooper())

    @RequiresApi(Build.VERSION_CODES.O)
    private val typingRunnable = Runnable {
        onTypingStopped()
    }

    fun setupTypingDetector() {
        binding.messageET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0 || before > 0) {
                    onTypingStarted()
                }
            }
            @RequiresApi(Build.VERSION_CODES.O)
            override fun afterTextChanged(s: Editable?) {
                typingHandler.removeCallbacks(typingRunnable)
                if (s.isNullOrEmpty()) {
                    onTypingStopped()
                } else {
                    typingHandler.postDelayed(typingRunnable, TYPING_TIMEOUT_MS)
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onTypingStarted() {
        Log.d("TypingStatus", "User STARTING to type...")
        updateTypingStatus(true, true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onTypingStopped() {
        Log.d("TypingStatus", "User STOPPED typing.")
        updateTypingStatus(true, false)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTypingStatus(isOnline: Boolean, isTyping: Boolean) {
        lifecycleScope.launch {
            authViewModel.updateOnlineStatusFlow(
                requireContext(),
                isOnline,
                isTyping,
                getCurrentUtcDateTimeModern(),
                receiverId.toString()
            )
                .collect { state ->
                    when (state) {
                        is AuthState.Error -> {
                            showToast(state.message)
                        }
                        AuthState.Loading -> {
                        }
                        is AuthState.Success -> {
                        }
                    }
                }
        }
    }


}