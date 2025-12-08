package com.example.firebasechattingapplication.view.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.ActivityMainBinding
import com.example.firebasechattingapplication.model.AuthState
import com.example.firebasechattingapplication.model.dataclasses.OnlineUser
import com.example.firebasechattingapplication.utils.Constants
import com.example.firebasechattingapplication.utils.ProgressIndicator
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.utils.getCurrentUtcDateTimeModern
import com.example.firebasechattingapplication.utils.gone
import com.example.firebasechattingapplication.utils.showToast
import com.example.firebasechattingapplication.utils.visible
import com.example.firebasechattingapplication.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var navController: NavController //nav controller

    //    private var auth: FirebaseAuth? = null
    private val viewModel: AuthViewModel by viewModels()
    private var isBackPressed = false
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpNavHost()
        setupBackNavigation()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        checkUserSession()
//        updateOnlineStatus(true)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {
        super.onStop()
        updateOnlineStatus(false)
    }


    private fun checkUserSession() {
        viewModel.isUserLogged()
        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Error -> {
                    ProgressIndicator.hide()
                    showToast("Session expired. Please login.")
                    navController.navigate(R.id.loginFragment)
                }

                AuthState.Loading -> {
                    ProgressIndicator.show(this)
                }

                is AuthState.Success -> {
                    ProgressIndicator.hide()
                    Log.d("lfwejkfew", "AuthState: $state")
                    navController.navigate(R.id.homeFragment)
                }
            }
        }
    }


    fun setupBackNavigation() {
        val handleBackPressLogic = {
            val currentDestinationId = navController.currentDestination?.id

            when (currentDestinationId) {
                R.id.homeFragment -> {
                    if (isBackPressed)
                        finish()
                    else {
                        isBackPressed = true
                        showToast("Press back again to exit")
                    }
                }

                R.id.loginFragment -> {
                    finish()
                }

                else -> {
                    navController.popBackStack()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // --- Android 13 (API 33) and above ---
            val callback = OnBackInvokedCallback {
                handleBackPressLogic()
            }
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                callback
            )

        } else {
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackPressLogic() // Execute the logic when the back button is pressed
                }
            }
            onBackPressedDispatcher.addCallback(this, callback)
        }
    }


    fun setUpNavHost() {
        val nestedNavHostFragment =
            supportFragmentManager.findFragmentById(R.id.frameLayout) as? NavHostFragment
        navController = nestedNavHostFragment?.navController!!

        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> navController.navigate(R.id.homeFragment)
                R.id.chat -> navController.navigate(R.id.chatsListFragment)
                R.id.settings -> navController.navigate(R.id.settingsFragment)
            }
            true
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment, R.id.chatFragment -> {
                    binding.bottomNav.gone()
                }

                else -> binding.bottomNav.visible()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateOnlineStatus(isOnline: Boolean) {
        lifecycleScope.launch {
            viewModel.updateOnlineStatusFlow(this@MainActivity, isOnline, false)
                .collect { state ->
                    when (state) {
                        is AuthState.Error -> {
//                            ProgressIndicator.hide()
                            Log.d(
                                "wekjfbjhebfw",
                                "updateOnlineStatusFlow  Error : main   $isOnline"
                            )

                            showToast(state.message)
                        }

                        AuthState.Loading -> {
//                            ProgressIndicator.show(this@MainActivity)
                        }

                        is AuthState.Success -> {
                            Log.d(
                                "wekjfbjhebfw",
                                "updateOnlineStatusFlow  Success : main   $isOnline"
                            )
//                            ProgressIndicator.hide()
                        }
                    }
                }
        }
    }


}