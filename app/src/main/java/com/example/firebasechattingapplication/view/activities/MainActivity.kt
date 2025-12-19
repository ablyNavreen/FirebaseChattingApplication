package com.example.firebasechattingapplication.view.activities

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
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
import com.example.firebasechattingapplication.google.GoogleOAuthHelper
import com.example.firebasechattingapplication.utils.ProgressIndicator
import com.example.firebasechattingapplication.utils.SpUtils
import com.example.firebasechattingapplication.google.TokenAcquisitionListener
import com.example.firebasechattingapplication.utils.CommonFunctions.showSettingsDialog
import com.example.firebasechattingapplication.utils.getCurrentUtcDateTimeModern
import com.example.firebasechattingapplication.utils.gone
import com.example.firebasechattingapplication.utils.showToast
import com.example.firebasechattingapplication.utils.visible
import com.example.firebasechattingapplication.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), TokenAcquisitionListener {
    lateinit var navController: NavController //nav controller
    private val viewModel: AuthViewModel by viewModels()
    private var pressedTime: Long = 0
    private lateinit var binding: ActivityMainBinding
    private lateinit var oauthHelper: GoogleOAuthHelper

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
//                showToast("Notification Permission Granted!")
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    showSettingsDialog(
                        this@MainActivity,
                        "Notifications permission is permanently denied. Please enable it in App Settings to receive message updates."
                    )
                } else {
                    showToast("Notification Permission Denied. Notifications will not be shown.")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        oauthHelper = GoogleOAuthHelper(this@MainActivity)

//        syncButton.setOnClickListener {
//        oauthHelper.acquireToken(this@MainActivity, this@MainActivity)
//        }

      /*  CoroutineScope(Dispatchers.IO).launch {
            val t = FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()
        }
*/
        setStatusBarColor(window, statusBarBgView = binding.statusBarBackgroundView)
        setUpNavHost()
        binding.root.post { handleNotification() }
        checkUserSession()
        onBackPressedDispatcher.addCallback(this@MainActivity) {
            setupBackNavigation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        updateOnlineStatus(true)
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
            requestNotificationPermission()
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
                    pressedTime = System.currentTimeMillis()
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
                R.id.homeFragment, R.id.chatsListFragment, R.id.settingsFragment-> {
                    binding.bottomNav.visible()
                }

                else -> binding.bottomNav.gone()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateOnlineStatus(isOnline: Boolean) {
        lifecycleScope.launch {
            viewModel.updateOnlineStatusFlow(
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


    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+ (API 33+)
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("Perm", "POST_NOTIFICATIONS already granted.")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showPermissionRationaleDialog()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d("Perm", "POST_NOTIFICATIONS automatically granted below Android 13.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this@MainActivity)
            .setTitle("Notification Permission Required")
            .setMessage("We need access to send you important updates. Please grant the notification permission.")
            .setPositiveButton("OK") { dialog, which ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("Cancel") { dialog, which ->
            }
            .show()
    }

    override fun onTokenAcquired(
        accessToken: String,
        refreshToken: String?,
        idToken: String?
    ) {
        // Now you can call your backend function:
        Log.i("TokenAcquisition", "Access Token acquired: $accessToken")

        // This is the data you need to send to your backend server
        val tokenMap = mapOf(
            "accessToken" to accessToken,
            "refreshToken" to refreshToken,
            "idToken" to idToken
        )
    }

    override fun onError(errorMessage: String) {
        Log.e("TokenAcquisition", "OAuth Error: $errorMessage")
        Toast.makeText(this, "Authentication failed: $errorMessage", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        oauthHelper.dispose()
    }

}