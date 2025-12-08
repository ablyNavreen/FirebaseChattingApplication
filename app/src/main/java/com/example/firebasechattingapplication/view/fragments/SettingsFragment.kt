package com.example.firebasechattingapplication.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.FragmentSettingsBinding
import com.example.firebasechattingapplication.model.AuthState
import com.example.firebasechattingapplication.utils.ProgressIndicator
import com.example.firebasechattingapplication.utils.showToast
import com.example.firebasechattingapplication.utils.showYesNoDialog
import com.example.firebasechattingapplication.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUpClickListeners()
    }

    private fun setUpClickListeners() {
            binding.logoutLayout.setOnClickListener {
                showYesNoDialog(requireActivity(),"Logout", "Are you sure you want to logout?", "Logout", "Cancel", onPositiveClick = {
                    logoutUser()
                })
            }
            binding.profileLayout.setOnClickListener {
                findNavController().navigate(R.id.action_settingsFragment_to_profileFragment)
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
                    findNavController().navigate(R.id.loginFragment)
                }
            }
        }
    }
}