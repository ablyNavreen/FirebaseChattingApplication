package com.example.firebasechattingapplication.view.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.FragmentSettingsBinding
import com.example.firebasechattingapplication.model.AuthState
import com.example.firebasechattingapplication.utils.CommonFunctions.showToast
import com.example.firebasechattingapplication.utils.CommonFunctions.showYesNoDialog
import com.example.firebasechattingapplication.utils.ProgressIndicator
import com.example.firebasechattingapplication.utils.getCurrentUtcDateTimeModern
import com.example.firebasechattingapplication.view.activities.MainActivity
import com.example.firebasechattingapplication.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpClickListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpClickListeners() {
        binding.apply {
            deleteLayout.setOnClickListener {
                showYesNoDialog(
                    requireActivity(),
                    "Logout",
                    "Are you sure you want to delete your account?",
                    "Delete Account",
                    "Cancel",
                    onPositiveClick = {
                        updateOnlineStatus(1)
                    })
            }
            logoutLayout.setOnClickListener {
                showYesNoDialog(
                    requireActivity(),
                    "Logout",
                    "Are you sure you want to logout?",
                    "Logout",
                    "Cancel",
                    onPositiveClick = {
                        updateOnlineStatus(0)
                    })
            }
            profileLayout.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_profileFragment)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateOnlineStatus(requestType: Int) {
        lifecycleScope.launch {
            viewModel.updateOnlineStatusFlow(
                isOnline = false,
                isTyping = false,
                getCurrentUtcDateTimeModern(), ""
            )
                .collect { state ->
                    when (state) {
                        is AuthState.Error -> {
                            ProgressIndicator.hide()
                            showToast(requireContext(), state.message)
                        }

                        AuthState.Loading -> {
                            ProgressIndicator.show(requireContext())
                        }

                        is AuthState.Success -> {
                            if (requestType == 0)
                                logoutUser()
                            else
                                deleteAccount()
                        }
                    }
                }
        }
    }

    private fun logoutUser() {
        viewModel.logoutUser()
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Error -> {
                    ProgressIndicator.hide()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }

                AuthState.Loading -> {
                    ProgressIndicator.show(requireContext())
                }

                is AuthState.Success -> {
                    ProgressIndicator.hide()
                    (activity as MainActivity).performLogoutAndResetUI()
                }
            }
        }
    }
    private fun deleteAccount() {
        viewModel.deleteAccount()
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Error -> {
                    ProgressIndicator.hide()
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }

                AuthState.Loading -> {
                    ProgressIndicator.show(requireContext())
                }

                is AuthState.Success -> {
                    ProgressIndicator.hide()
                    (activity as MainActivity).performLogoutAndResetUI()
                }
            }
        }
    }
}