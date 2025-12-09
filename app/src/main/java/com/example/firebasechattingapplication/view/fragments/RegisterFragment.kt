package com.example.firebasechattingapplication.view.fragments

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.FragmentRegisterBinding
import com.example.firebasechattingapplication.model.AuthState
import com.example.firebasechattingapplication.model.dataclasses.User
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.ProgressIndicator
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.utils.isValidEmail
import com.example.firebasechattingapplication.utils.showToast
import com.example.firebasechattingapplication.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        binding.apply {
            loginTV.setOnClickListener {
                //navigate to login screen
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            registerBT.setOnClickListener {
                if (validateRegisteration())
                    hitRegisterUser()
            }
        }
    }

    private fun validateRegisteration(): Boolean {
        //check conditons for null or empty input by the user
        binding.apply {
            if (nameET.text.toString().isEmpty()) {
                showToast("Please enter name")
                return false
            }  else if (!maleCB.isChecked && !femaleCB.isChecked) {
                showToast("Please select gender")
                return false
            }
            else if (emailET.text.toString().trim().isEmpty()) {
                showToast("Please enter email address")
                return false
            } else if (! isValidEmail(emailET.text.toString())) {
                showToast("Please enter valid email address")
                return false
            }
            else if (passwordET.text.toString().trim().isEmpty()) {
                showToast("Please enter password")
                return false
            } else if (passwordET.text.toString().trim().length < 6) {
                showToast("Password should be atleast 6 characters.")
                return false
            } else if (cpasswordET.text.toString().trim().isEmpty()) {
                showToast("Please enter confirm password.")
                return false
            } else if (passwordET.text.toString().trim() != cpasswordET.text.toString().trim()) {
                showToast("Password and confirm password are not same.")
                return false
            } else {
                //else ->register the usr
                return true
            }
        }

    }

    private fun hitRegisterUser() {
        viewModel.registerUser(binding.emailET.text.toString(), binding.passwordET.text.toString())
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Error -> {
                    ProgressIndicator.hide()
                    Log.d("wekjfbjhebfw", "hitRegisterUser: Error ${state.message}")
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }

                AuthState.Loading -> {
                    Log.d("wekjfbjhebfw", "hitRegisterUser: Loading ${state}")
                    ProgressIndicator.show(requireContext())
                }

                is AuthState.Success -> {
                    Log.d("wekjfbjhebfw", "hitRegisterUser: Success ${state}")
                    addUserToFirestore(state.userId)
                }
            }
        }
    }

    private fun addUserToFirestore(userId: String) {
        val  gender = if (binding.maleCB.isChecked) 0 else 1
        viewModel.addUserToFirestore(User(id = userId, name = binding.nameET.text.toString(), email = binding.emailET.text.toString(), gender, binding.passwordET.text.toString().trim()))
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Error -> {
                    ProgressIndicator.hide()
                    Log.d("wekjfbjhebfw", "addUserToFirestore: Error ${state.message}")
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                AuthState.Loading -> {
                    Log.d("wekjfbjhebfw", "addUserToFirestore: Loading ${state}")
                    ProgressIndicator.show(requireContext())
                }
                is AuthState.Success -> {
                    ProgressIndicator.hide()
                    Log.d("wekjfbjhebfw", "addUserToFirestore: Success ${state}")
                    SpUtils.saveString(requireContext(), Constants.USER_ID, userId)
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }
    }


}