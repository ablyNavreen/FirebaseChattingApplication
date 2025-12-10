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
import com.example.firebasechattingapplication.model.dataclasses.OnlineUser
import com.example.firebasechattingapplication.model.dataclasses.User
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.ProgressIndicator
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.utils.getCurrentUtcDateTimeModern
import com.example.firebasechattingapplication.utils.gone
import com.example.firebasechattingapplication.utils.showToast
import com.example.firebasechattingapplication.utils.visible
import com.example.firebasechattingapplication.view.activities.MainActivity.Companion.isDataLoaded
import com.example.firebasechattingapplication.view.adapters.ActiveUsersAdapter
import com.example.firebasechattingapplication.view.adapters.UsersAdapter
import com.example.firebasechattingapplication.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: AuthViewModel by viewModels()  //get viewmodel instance
    private var activeUsersAdapter: ActiveUsersAdapter? = null  //top active users adapter
    private val onlineUser = ArrayList<OnlineUser>()  //list observed by active users adapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showActiveUsersList()  //setup adapter
        getActiveUsers()       //fetch active users lit
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        getUserData()
        Log.d("ekjfwhkjwehfw", "onResume: $isDataLoaded")
        if (!isDataLoaded)
            updateOnlineStatus()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateOnlineStatus() {
        lifecycleScope.launch {
            viewModel.updateOnlineStatusFlow(
                requireContext(),
                true,
                false,
                getCurrentUtcDateTimeModern(), ""
            )
                .collect { state ->
                    when (state) {
                        is AuthState.Error -> {
                            ProgressIndicator.hide()
                            showToast(state.message)
                        }
                        AuthState.Loading -> {
                            ProgressIndicator.show(requireContext())
                        }

                        is AuthState.Success -> {
                            Log.d("wekjfbjhebfw", "updateOnlineStatusFlow  Success : main   true")
                            ProgressIndicator.hide()
                            isDataLoaded = true
                        }
                    }
                }
        }
    }

    private fun getActiveUsers() {
        viewModel.getOnlineUsers(requireContext())
            .onEach { messageList ->
                onlineUser.clear()
                val activeUsers = messageList.filter { it.online == true }
                onlineUser.addAll(activeUsers)
                binding.activeUsersTV.text =
                    getString(R.string.active_users) + "(" + activeUsers.size + ")"
                if (onlineUser.isNotEmpty()) {
                    binding.noUsersTV.gone()
                    activeUsersAdapter?.notifyDataSetChanged()
                } else {
                    binding.noUsersTV.visible()
                }
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
                val userData = userList.filter { it.id == SpUtils.getString(requireContext(), Constants.USER_ID) }
                if (userData.isNotEmpty()) {
                    SpUtils.saveString(requireContext(), Constants.USER_ID, userData[0].id.toString())
                    SpUtils.saveString(requireContext(), Constants.USER_GENDER, userData[0].gender.toString())
                    SpUtils.saveString(requireContext(), Constants.USER_NAME, userData[0].name.toString())
                    SpUtils.saveString(requireContext(), Constants.USER_EMAIL, userData[0].email.toString())
                    showUsersList(userList.filter { it.id != SpUtils.getString(requireContext(), Constants.USER_ID) })
                }
            } else {
//                showToast("Could not load user data.")
            }
        }
    }

    private fun showUsersList(userList: List<User>) {
        if (userList.isEmpty())
            binding.noUsersTV2.visible()
        val adapter = UsersAdapter(requireContext(), userList)
        binding.usersRV.adapter = adapter
        adapter.messageUser = { userId, userName, userGender ->
            findNavController().navigate(R.id.chatFragment, Bundle().apply {
                putString(Constants.USER_ID, userId)
                putString(Constants.USER_NAME, userName)
                putInt(Constants.USER_GENDER, userGender)
            })
        }
    }

    private fun showActiveUsersList() {
        activeUsersAdapter = ActiveUsersAdapter(requireContext(), onlineUser)
        binding.activeUsersRV.adapter = activeUsersAdapter
        activeUsersAdapter?.messageUser = { userId, userName, userGender ->
            findNavController().navigate(R.id.chatFragment, Bundle().apply {
                putString(Constants.USER_ID, userId)
                putString(Constants.USER_NAME, userName)
                putInt(Constants.USER_GENDER, userGender)
            })
        }
    }

}