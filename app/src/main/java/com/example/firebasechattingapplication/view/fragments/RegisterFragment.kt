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
    private val viewModel: AuthViewModel by viewModels()  //get viewmodel instance

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

        //handle the click listeners logic on views
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        binding.apply {
            loginTV.setOnClickListener {
                //navigate to login screen
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            registerBT.setOnClickListener {
                //start registeration process
                if (validateRegister())
                    hitRegisterUser()
            }
        }
    }

    private fun validateRegister(): Boolean {
        //checks register validations
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
                return true
            }
        }

    }

    private fun hitRegisterUser() {
        val  gender = if (binding.maleCB.isChecked) 0 else 1  //0-male, 1-female
        val user =User( name = binding.nameET.text.toString(),
            email = binding.emailET.text.toString(),
            gender = gender,
            password = binding.passwordET.text.toString().trim())
        //hit register using firebase
        viewModel.registerUser(user)  //data is passed by putting into model class directly
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Error -> {
                    ProgressIndicator.hide()
                    showToast( state.message)
                }
                AuthState.Loading -> {
                    ProgressIndicator.show(requireContext())
                }
                is AuthState.Success -> {
                    //save user id and move to home
                    SpUtils.saveString(requireContext(), Constants.USER_ID, state.userId)
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }
    }


}