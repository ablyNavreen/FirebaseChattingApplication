package com.example.firebasechattingapplication.view.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.FragmentHomeBinding
import com.example.firebasechattingapplication.model.AuthState
import com.example.firebasechattingapplication.model.dataclasses.Message
import com.example.firebasechattingapplication.model.dataclasses.OnlineUser
import com.example.firebasechattingapplication.model.dataclasses.User
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.ProgressIndicator
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.utils.showToast
import com.example.firebasechattingapplication.view.adapters.ActiveUsersAdapter
import com.example.firebasechattingapplication.view.adapters.UsersAdapter
import com.example.firebasechattingapplication.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: AuthViewModel by viewModels()
    private var activeUsersAdapter: ActiveUsersAdapter?=null
    private val onlineUser = ArrayList<OnlineUser>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        SpUtils.getString(requireContext(), Constants.USER_ID)
        showActiveUsersList()
        getUserData()
        getActiveUsers()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        updateOnlineStatus(true)

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateOnlineStatus(isOnline: Boolean) {
        lifecycleScope.launch {
            viewModel.updateOnlineStatusFlow(requireContext(), isOnline, false)
                .collect { state ->
                    when (state) {
                        is AuthState.Error -> {
                            ProgressIndicator.hide()
                            Log.d("wekjfbjhebfw", "updateOnlineStatusFlow  Error : main   $isOnline")
                            showToast(state.message)
                        }
                        AuthState.Loading -> {
                            ProgressIndicator.show(requireContext())
                        }
                        is AuthState.Success -> {
                            Log.d("wekjfbjhebfw", "updateOnlineStatusFlow  Success : main   $isOnline")
                            ProgressIndicator.hide()
                        }
                    }
                }
        }
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onStop() {
//        super.onStop()
//        updateOnlineStatus(false)
//    }
    private fun getActiveUsers() {
        viewModel.getOnlineUsers()
            .onEach { messageList ->
                onlineUser.clear()
                onlineUser.addAll(messageList.filter { it.online==true })
                Log.d("wekjfbjhebfw", "getActiveUsers Update: $onlineUser getActiveUsers")
              if (onlineUser.isNotEmpty())
                  activeUsersAdapter?.notifyDataSetChanged()
            }
            .catch { e ->
                Log.e("Chat", "Error collecting getActiveUsers : ${e.message}")
                showToast("Error loading messages.")
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)  //starts collection -> tied to view
    }

    private fun getUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val userList = viewModel.getUserData()
            if (userList != null) {
               val userData =  userList.filter { it.id == SpUtils.getString(requireContext(), Constants.USER_ID) }
                if (userData.isNotEmpty()) {
                    SpUtils.saveString(
                        requireContext(),
                        Constants.USER_ID,
                        userData[0].id.toString()
                    )
                    SpUtils.saveString(
                        requireContext(),
                        Constants.USER_NAME,
                        userData[0].name.toString()
                    )
                    SpUtils.saveString(
                        requireContext(),
                        Constants.USER_EMAIL,
                        userData[0].email.toString()
                    )
                    Log.d("wekjfbjhebfw", "userList : ${userList.size}")
                    showUsersList(userList.filter { it.id != SpUtils.getString(requireContext(), Constants.USER_ID) })
                }
            } else {
                showToast("Could not load user data.")
            }
        }
    }

    private fun showUsersList(userList: List<User>) {
        val adapter = UsersAdapter(requireContext(),userList)
        binding.usersRV.adapter = adapter
        adapter.messageUser={ userId , userName->
            findNavController().navigate(R.id.chatFragment, Bundle().apply {
                putString(Constants.USER_ID, userId)
                putString(Constants.USER_NAME, userName)
            })
        }
    }

    private fun showActiveUsersList() {
        activeUsersAdapter = ActiveUsersAdapter(requireContext(),onlineUser)
        binding.activeUsersRV.adapter = activeUsersAdapter
        activeUsersAdapter?.messageUser={ userId , userName->
            findNavController().navigate(R.id.chatFragment, Bundle().apply {
                putString(Constants.USER_ID, userId)
                putString(Constants.USER_NAME, userName)
            })
        }
    }

}