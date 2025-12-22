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
import com.example.firebasechattingapplication.model.dataclasses.OnlineUser
import com.example.firebasechattingapplication.model.dataclasses.User
import com.example.firebasechattingapplication.utils.CommonFunctions.showToast
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.utils.gone
import com.example.firebasechattingapplication.utils.visible
import com.example.firebasechattingapplication.view.adapters.ActiveUsersAdapter
import com.example.firebasechattingapplication.view.adapters.UsersAdapter
import com.example.firebasechattingapplication.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: AuthViewModel by viewModels()  //get viewmodel instance
    private var activeUsersAdapter: ActiveUsersAdapter? = null  //top active users adapter
    private var usersAdapter: UsersAdapter? = null  //all users adapter
    private val onlineUser = ArrayList<OnlineUser>()  //list observed by active users adapter
    private val allUsers = ArrayList<User>()  //list observed by all users adapter


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
        Log.d("tokennnnnn", "onViewCreated home: ${SpUtils.getString(requireContext(), Constants.USER_TOKEN)}")
        showUsersList()
        getUserData()
        showActiveUsersList()  //setup adapter
        getActiveUsers()       //fetch active users lit
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
//        updateOnlineStatus()
    }


  /*  @RequiresApi(Build.VERSION_CODES.O)
    private fun updateOnlineStatus() {
        lifecycleScope.launch {
            viewModel.updateOnlineStatusFlow(
                isOnline = true,
                isTyping = false,
                lastSeen = getCurrentUtcDateTimeModern(), ""
            )
                .collect { state ->
                    when (state) {
                        is AuthState.Error -> {
                            showToast(state.message)
                        }
                        else -> {}
                    }
                }
        }
    }*/

    private fun getActiveUsers() {
        viewModel.getOnlineUsers()
            .onEach { messageList ->
                onlineUser.clear()

                val activeUsers = messageList.filter { it.online == true && it.name!=null }
                onlineUser.addAll(activeUsers)
                if (activeUsers.size > 0)
                    binding.activeUsersTV.text = buildString {
                        append(getString(R.string.active_users))
                        append("(")
                        append(onlineUser.size)
                        append(")")
                    }
                else
                    binding.activeUsersTV.text = getString(R.string.active_users)
                if (onlineUser.isNotEmpty()) {
                    binding.activeUsersRV.visible()
                    binding.noUsersTV.gone()
                    activeUsersAdapter?.notifyDataSetChanged()
                } else {
                    binding.activeUsersRV.gone()
                    binding.noUsersTV.visible()
                }
            }
            .catch { e ->
                Log.e("Chat", "Error collecting getActiveUsers : ${e.message}")
                showToast(requireContext(),"Error loading messages.")
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)  //starts collection -> tied to view
    }

    private fun getUserData() {
      viewModel.getUserData()
            .onEach { userList ->
                allUsers.clear()
                val userData = userList.filter { it.id == SpUtils.getString(requireContext(), Constants.USER_ID) }
                if (userData.isNotEmpty()) {
                SpUtils.saveString(
                    requireContext(),
                    Constants.USER_ID,
                    userData[0].id.toString()
                )
                SpUtils.saveString(
                    requireContext(),
                    Constants.USER_GENDER,
                    userData[0].gender.toString()
                )
                SpUtils.saveString(
                    requireContext(),
                    Constants.USER_NAME,
                    userData[0].name.toString()
                )
                SpUtils.saveString(
                    requireContext(),
                    Constants.USER_EMAIL,
                    userData[0].email.toString())
            }
                allUsers.addAll(userList.filter {
                    it.id != SpUtils.getString(requireContext(), Constants.USER_ID) && it.name!=null
                }.sortedByDescending { it.currentTime})

                if (allUsers.isEmpty()) {
                    binding.noUsersTV2.visible()
                    binding.usersRV.gone()
                } else {
                    binding.noUsersTV2.gone()
                    binding.usersRV.visible()
                    usersAdapter?.notifyDataSetChanged()
                }
            }
          .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showUsersList() {
        usersAdapter = UsersAdapter(requireContext(), allUsers)
        binding.usersRV.adapter = usersAdapter
        usersAdapter?.messageUser = { userId, userName, userGender , userToken->
            findNavController().navigate(R.id.chatFragment, Bundle().apply {
                putString(Constants.USER_ID, userId)
                putString(Constants.USER_NAME, userName)
                putInt(Constants.USER_GENDER, userGender)
                putString(Constants.USER_TOKEN, userToken)
            })
        }
    }

    private fun showActiveUsersList() {
        activeUsersAdapter = ActiveUsersAdapter(requireContext(), onlineUser)
        binding.activeUsersRV.adapter = activeUsersAdapter
        activeUsersAdapter?.messageUser = { userId, userName, userGender, userToken ->
            findNavController().navigate(R.id.chatFragment, Bundle().apply {
                putString(Constants.USER_ID, userId)
                putString(Constants.USER_NAME, userName)
                putInt(Constants.USER_GENDER, userGender)
                putString(Constants.USER_TOKEN, userToken)
            })
        }
    }

}