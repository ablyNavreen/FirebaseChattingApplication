package com.example.firebasechattingapplication.view.activities

import android.os.Build
import android.os.Bundle
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


    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(window, statusBarBgView = binding.statusBarBackgroundView)
        setUpNavHost()

        binding.root.post { handleNotification() }
        checkUserSession()
        onBackPressedDispatcher.addCallback(this@MainActivity) {
            setupBackNavigation()
        }
    }

    private fun handleNotification() {
        val senderId = intent.getStringExtra("sender_id")
        if (senderId != null) {
              navController.navigate(R.id.chatFragment, Bundle().apply {
                    putString(Constants.USER_ID, senderId)
                    putString(Constants.USER_NAME, intent.getStringExtra("sender_name"))
                    putString(Constants.USER_GENDER, intent.getStringExtra("sender_gender"))
                    putString(Constants.USER_TOKEN, intent.getStringExtra("sender_token"))
                })
        } else {
            //failure or null case - normal execution through splash

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
    override fun onStop() {
        super.onStop()
        //offline when app is closed or in recents
        updateOnlineStatus(false)
    }


    private fun checkUserSession() {
        if (SpUtils.getString(this@MainActivity, Constants.USER_ID) != null) {
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
                        navController.navigate(R.id.homeFragment)
                    }
                }
            }
        }

    }


    private fun setupBackNavigation() {
        val currentDestinationId = navController.currentDestination?.id
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
                            showToast(state.message)
                        }

                        else -> {}
                    }
                }
        }
    }

    fun performLogoutAndResetUI() {
        val options = androidx.navigation.navOptions {
            popUpTo(R.id.loginFragment) {
                inclusive = true
            }
        }

        binding.bottomNav.selectedItemId = R.id.home

        navController.navigate(
            R.id.loginFragment,
            null,
            options
        )
    }

}