package com.example.firebasechattingapplication.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.FragmentLoginBinding
import com.example.firebasechattingapplication.model.AuthState
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.ProgressIndicator
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.utils.showToast
import com.example.firebasechattingapplication.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.flow


@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val authViewModel : AuthViewModel by viewModels()  //get viewmodel instance
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //handle the click listeners logic on views
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        binding.apply {
            registerTV.setOnClickListener {
                //navigate to register screen
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            loginBT.setOnClickListener {
                //start login process
                if (validateData())
                    hitUserLogin()
            }
        }
    }

    private fun hitUserLogin() {
        //hit user login using firebase auth
        authViewModel.loginUser(binding.emailET.text.toString().trim(), binding.passwordET.text.toString().trim())
        authViewModel.authState.observe(viewLifecycleOwner){it->
            when(it){
                is AuthState.Error -> {
                    ProgressIndicator.hide()
                    showToast("The username or password you entered is incorrect. Please try again.")
                }
                AuthState.Loading -> {
                    ProgressIndicator.show(requireContext())
                }
                is AuthState.Success -> {
                    ProgressIndicator.hide()
                    //save user id and navigate to home
                    SpUtils.saveString(requireContext(), Constants.USER_ID, it.userId)
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }
    }

    fun validateData() : Boolean {  //checks for login validations
        binding.apply {
            if (emailET.text.toString().trim().isEmpty()) {
                showToast("Email can't be empty")
                return false
            } else if (passwordET.text.toString().trim().isEmpty()) {
                showToast("Password can't be empty")
                return false
            } else {
              return true
            }
        }
    }


}