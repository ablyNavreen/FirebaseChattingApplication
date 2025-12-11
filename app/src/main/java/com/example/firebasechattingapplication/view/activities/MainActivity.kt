package com.example.firebasechattingapplication.view.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.firebasechattingapplication.R
import com.example.firebasechattingapplication.databinding.ActivityMainBinding
import com.example.firebasechattingapplication.model.AuthState
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
    private val viewModel: AuthViewModel by viewModels()
    private var pressedTime: Long = 0
    private lateinit var binding: ActivityMainBinding

    companion object {
        var isDataLoaded = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(window, statusBarBgView = binding.statusBarBackgroundView)
        setUpNavHost()
        checkUserSession()
        onBackPressedDispatcher.addCallback(this@MainActivity) {
            setupBackNavigation()
        }
    }

   private fun setStatusBarColor(window: Window, statusBarBgView: View) {
            statusBarBgView.setBackgroundResource(R.drawable.maroon_black_gradient_bg)
            ViewCompat.setOnApplyWindowInsetsListener(statusBarBgView) { view, insets ->
                val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.updateLayoutParams {
                    height = systemBarsInsets.top
                }
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                insetsController.isAppearanceLightStatusBars = false
                WindowInsetsCompat.CONSUMED
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPause() {
        super.onPause()
        //make user offline when app is closed or in recents
        updateOnlineStatus(false)
    }

    override fun onStop() {
        super.onStop()
        //refresh the user online status when user comes from recents
        isDataLoaded = false
    }

    private fun checkUserSession() {
        if (SpUtils.getString(this@MainActivity, Constants.USER_ID)!=null){
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

    }


    private fun setupBackNavigation() {
        val currentDestinationId = navController.currentDestination?.id
        Log.d("wlkjkwhjekfw", "handleBackPressLogic: setupBackNavigation tiramisu")

        when (currentDestinationId) {
            R.id.homeFragment -> {
                if (pressedTime + 2000 > System.currentTimeMillis())
                    finish()
                else {
                    pressedTime = System.currentTimeMillis();
                    showToast("Press back again to exit")
                }
            }

            R.id.loginFragment -> finish()
            R.id.chatsListFragment, R.id.settingsFragment -> {
                navController.navigate(R.id.homeFragment)
                binding.bottomNav.selectedItemId = R.id.home
            }

            else -> navController.popBackStack()
        }
//      super.onBackPressedDispatcher.onBackPressed()
    }


    private fun setUpNavHost() {
        val nestedNavHostFragment =
            supportFragmentManager.findFragmentById(R.id.frameLayout) as? NavHostFragment
        navController = nestedNavHostFragment?.navController!!
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    navController.navigate(R.id.homeFragment)
                }
                 R.id.chat -> navController.navigate(R.id.chatsListFragment)
                R.id.settings -> navController.navigate(R.id.settingsFragment)
            }
            true
        }


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
               R.id.loginFragment, R.id.registerFragment, R.id.chatFragment, R.id.profileFragment -> {
                    binding.bottomNav.gone()
                }
                else -> binding.bottomNav.visible()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateOnlineStatus(isOnline: Boolean) {
        lifecycleScope.launch {
            viewModel.updateOnlineStatusFlow(
                this@MainActivity,
                isOnline,
                false,
                getCurrentUtcDateTimeModern(),
                ""
            )
                .collect { state ->
                    when (state) {
                        is AuthState.Error -> {
                            Log.d(
                                "wekjfbjhebfw",
                                "updateOnlineStatusFlow  Error : main   $isOnline"
                            )
                            if (!isFinishing && !isDestroyed) ProgressIndicator.hide()
                            showToast(state.message)
                        }

                        AuthState.Loading -> {
                            if (!isFinishing && !isDestroyed) ProgressIndicator.show(this@MainActivity)
                        }

                        is AuthState.Success -> {
                            if (!isFinishing && !isDestroyed) ProgressIndicator.hide()
                            Log.d(
                                "wekjfbjhebfw",
                                "updateOnlineStatusFlow  Success : main   $isOnline"
                            )
                        }
                    }
                }
        }
    }
    fun performLogoutAndResetUI() {
        // 1. Define the NavOptions to clear the entire back stack
        val options = androidx.navigation.navOptions {
            // Pop up to the LoginFragment, removing all authenticated screens
            popUpTo(R.id.loginFragment) {
                inclusive = true
            }
        }

        // 2. CRUCIAL: Manually reset the selected item to R.id.home.
        // This prepares the BottomNav for the next time it becomes visible (after next login).
        // We set it to Home because that is the destination that should be highlighted later.
        binding.bottomNav.selectedItemId = R.id.home

        // 3. Navigate to the LoginFragment
        navController.navigate(
            R.id.loginFragment,
            null, // No arguments
            options
        )
    }

}