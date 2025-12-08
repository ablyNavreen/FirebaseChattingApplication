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

    private val authViewModel : AuthViewModel by viewModels()
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
        setUpClickListeners()
    }

    private fun setUpClickListeners() {
        binding.apply {
            registerTV.setOnClickListener {
                //navigate to register screen
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            loginBT.setOnClickListener {
                //login user
                if (validateData())
                    hitUserLogin()
            }
        }
    }

    private fun hitUserLogin() {
        authViewModel.loginUser(binding.emailET.text.toString().trim(), binding.passwordET.text.toString().trim())
        authViewModel.authState.observe(viewLifecycleOwner){it->
            when(it){
                is AuthState.Error -> {
                    ProgressIndicator.hide()
                    showToast(it.message)
                }
                AuthState.Loading -> {
                    ProgressIndicator.show(requireContext())
                }
                is AuthState.Success -> {
                    ProgressIndicator.hide()
                    SpUtils.saveString(requireContext(), Constants.USER_ID, it.userId)
                    findNavController().navigate(R.id.homeFragment)
                }
            }
        }
    }

    /*    fun createNewUser(email: String, password: String) {
            val auth = Firebase.auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = auth.currentUser
    //                   updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()
    //                 updateUI(null)
                    }
                }
        }*/





    fun validateData() : Boolean {
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