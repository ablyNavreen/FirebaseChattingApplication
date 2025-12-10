package com.example.firebasechattingapplication.view.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.ActivityMainBinding
import com.example.firebasechattingapplication.model.AuthState
import com.example.firebasechattingapplication.utils.ProgressIndicator
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
    private val viewModel: AuthViewModel by viewModels()
    private var pressedTime: Long = 0
    private lateinit var binding: ActivityMainBinding
    companion object{
        var isDataLoaded = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

      /*  window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )*/
        setUpNavHost()
       checkUserSession()
        onBackPressedDispatcher.addCallback(this@MainActivity) {
            setupBackNavigation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPause() {
        super.onPause()
        Log.d("ekjfwhkjwehfw", "onPause: of onPause")
        updateOnlineStatus(false)
    }

    override fun onStop() {
        super.onStop()
        isDataLoaded = false
        Log.d("ekjfwhkjwehfw", "onStop: of onStop")
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
                    ProgressIndicator.show(this@MainActivity)
                }
                is AuthState.Success -> {
                    ProgressIndicator.hide()
                    Log.d("lfwejkfew", "AuthState: $state")
                    navController.navigate(R.id.homeFragment)
                }
            }
        }
    }


  private  fun setupBackNavigation() {
            val currentDestinationId = navController.currentDestination?.id
            Log.d("wlkjkwhjekfw", "handleBackPressLogic: setupBackNavigation tiramisu")

            when (currentDestinationId) {
                R.id.homeFragment -> {
                    if (pressedTime + 2000 > System.currentTimeMillis())  finish()
                    else {
                        pressedTime = System.currentTimeMillis();
                        showToast("Press back again to exit")
                    }
                }
                R.id.loginFragment -> finish()
                R.id.chatsListFragment , R.id.settingsFragment -> navController.navigate(R.id.homeFragment)
                else -> navController.popBackStack()
            }
      super.onBackPressedDispatcher.onBackPressed()
    }


   private fun setUpNavHost() {
        val nestedNavHostFragment = supportFragmentManager.findFragmentById(R.id.frameLayout) as? NavHostFragment
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
            viewModel.updateOnlineStatusFlow(this@MainActivity, isOnline, false, getCurrentUtcDateTimeModern(), "")
                .collect { state ->
                    when (state) {
                        is AuthState.Error -> {
                            Log.d("wekjfbjhebfw", "updateOnlineStatusFlow  Error : main   $isOnline")
                            if (!isFinishing && !isDestroyed) ProgressIndicator.hide()
                            showToast(state.message)
                        }
                        AuthState.Loading -> {
                            if (!isFinishing && !isDestroyed)  ProgressIndicator.show(this@MainActivity)
                        }
                        is AuthState.Success -> {
                            if (!isFinishing && !isDestroyed)  ProgressIndicator.hide()
                            Log.d("wekjfbjhebfw", "updateOnlineStatusFlow  Success : main   $isOnline")
                        }
                    }
                }
        }
    }


}