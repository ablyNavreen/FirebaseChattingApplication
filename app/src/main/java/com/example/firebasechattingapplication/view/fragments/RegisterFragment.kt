package com.example.firebasechattingapplication.view.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.FragmentRegisterBinding
import com.example.firebasechattingapplication.model.AuthState
import com.example.firebasechattingapplication.model.dataclasses.User
import com.example.firebasechattingapplication.utils.CommonFunctions.showToast
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.ProgressIndicator
import com.example.firebasechattingapplication.utils.SharedPreferencesHelper.saveString
import com.example.firebasechattingapplication.utils.isValidEmail
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //handle the click listeners logic on views
        setUpClickListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpClickListeners() {
        binding.apply {
            loginTV.setOnClickListener {
                //navigate to login screen
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            registerBT.setOnClickListener {
                //start registration process
                if (validateRegister())
                    hitRegisterUser()
            }
            femaleCB.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked){
                    if (maleCB.isChecked)
                        maleCB.isChecked = false
                }
            }
            maleCB.setOnCheckedChangeListener {  buttonView, isChecked ->
                if (isChecked){
                    if (femaleCB.isChecked)
                        femaleCB.isChecked = false
                }
            }
        }
    }

    private fun validateRegister(): Boolean {
        //checks register validations
        binding.apply {
            if (nameET.text.toString().isEmpty()) {
                showToast(requireContext(),"Please enter name")
                return false
            }  else if (!maleCB.isChecked && !femaleCB.isChecked) {
                showToast(requireContext(),"Please select gender")
                return false
            }
            else if (emailET.text.toString().trim().isEmpty()) {
                showToast(requireContext(),"Please enter email address")
                return false
            } else if (! isValidEmail(emailET.text.toString().trim())) {
                showToast(requireContext(),"Please enter valid email address")
                return false
            }
            else if (passwordET.text.toString().trim().isEmpty()) {
                showToast(requireContext(),"Please enter password")
                return false
            } else if (passwordET.text.toString().trim().length < 6) {
                showToast(requireContext(),"Password should be least 6 characters.")
                return false
            } else if (cpasswordET.text.toString().trim().isEmpty()) {
                showToast(requireContext(),"Please enter confirm password.")
                return false
            } else if (passwordET.text.toString().trim() != cpasswordET.text.toString().trim()) {
                showToast(requireContext(),"Password and confirm password are not same.")
                return false
            } else {
                return true
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hitRegisterUser() {
        val  gender = if (binding.maleCB.isChecked) 0 else 1  //0-male, 1-female
        val user =User( name = binding.nameET.text.toString(),
            email = binding.emailET.text.toString().trim(),
            gender = gender,
            password = binding.passwordET.text.toString().trim())
        //hit register using firebase
        viewModel.registerUser(user)  //data is passed by putting into model class directly
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Error -> {
                    ProgressIndicator.hide()
                    showToast(requireContext(),"Failed to register. Please try again later.")
                }
                AuthState.Loading -> {
                    ProgressIndicator.show(requireContext())
                }
                is AuthState.Success -> {
                    ProgressIndicator.hide()
                    //save user id and move to home
                    saveString(requireContext(), Constants.USER_ID, state.userId)
                    saveString(requireContext(), Constants.USER_GENDER, gender.toString())
                    saveString(requireContext(), Constants.USER_NAME, binding.nameET.text.toString().trim())
                    saveString(requireContext(), Constants.USER_EMAIL, binding.emailET.text.toString().trim())
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }
    }


}