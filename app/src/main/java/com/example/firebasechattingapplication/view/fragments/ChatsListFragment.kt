package com.example.firebasechattingapplication.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.FragmentChatListBinding
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.ProgressIndicator
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.utils.gone
import com.example.firebasechattingapplication.utils.showToast
import com.example.firebasechattingapplication.utils.visible
import com.example.firebasechattingapplication.view.adapters.ChatsAdapter
import com.example.firebasechattingapplication.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ChatsListFragment : Fragment() {
    private val messages = ArrayList<Message>()
    private lateinit var binding: FragmentChatListBinding
    private val viewModel: AuthViewModel by viewModels()

    private var chatsAdapter: ChatsAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatListBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setChatsAdapter()
        fetchAllMessages()
    }

    private fun fetchAllMessages() {
        viewModel.getAllMessages()
            .onEach { messageList ->
                messages.clear()
                val myChats = messageList.filter {
                    it.senderId == SpUtils.getString(
                        requireContext(),
                        Constants.USER_ID
                    ) || it.receiverId == SpUtils.getString(requireContext(), Constants.USER_ID)
                }
                for (m in myChats) {
                    val sentByMe = if (m.senderId == SpUtils.getString(
                            requireContext(),
                            Constants.USER_ID
                        )
                    ) true else false
                    messages.add(
                        Message(
                            senderId = if (!sentByMe) m.senderId else m.receiverId,
                            senderName = if (!sentByMe) m.senderName else m.receiverName,
                            senderToken = if (!sentByMe) m.senderToken else m.receiverToken,
                            time = m.time,
                            read = m.read,
                            message = m.message,
                            gender = if (!sentByMe) m.senderGender else m.receiverGender,
                            senderGender = if (!sentByMe) m.senderGender else m.receiverGender,
                            sentByMe = sentByMe,
                            image = m.image,
                            audio = m.audio
                        )
                    )

                }
                val sortedList = messages.sortedBy { it.time }
                    .groupBy { it.senderId }.values.mapNotNull { it.lastOrNull() }
                messages.clear()
                messages.addAll(sortedList.sortedByDescending { it.time })
                if (myChats.isNotEmpty()) {
                    binding.noMessagesTV.gone()
                    chatsAdapter?.notifyDataSetChanged()
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

    private fun setChatsAdapter() {
        chatsAdapter = ChatsAdapter(requireContext(), messages = messages)
        binding.chatsRV.adapter = chatsAdapter
        chatsAdapter?.messageUser = { userId, userName, userGender, userToken ->
            findNavController().navigate(R.id.chatFragment, Bundle().apply {
                putString(Constants.USER_ID, userId)
                putString(Constants.USER_NAME, userName)
                putInt(Constants.USER_GENDER, userGender)
                putString(Constants.USER_TOKEN, userToken)
            })
        }
    }


}