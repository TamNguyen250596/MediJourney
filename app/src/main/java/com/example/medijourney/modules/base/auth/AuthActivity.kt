package com.example.medijourney.modules.base.auth

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.common.extensions.disposeBy
import com.example.medijourney.common.managers.firebase_auth.AuthenticationResult
import com.example.medijourney.common.managers.firebase_auth.FirebaseAuthManager
import com.example.medijourney.databinding.ActivityAuthBinding
import com.example.medijourney.modules.base.main.MainActivity
import io.reactivex.rxjava3.disposables.CompositeDisposable

class AuthActivity : AppCompatActivity() {

    // Properties
    private lateinit var binding: ActivityAuthBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val disposables = CompositeDisposable()

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpView()
        observeAuthResult()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHostFragmentContentAuth)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Functions
    private fun setUpView() {
        if (FirebaseAuthManager.getCurrentFirebaseUser() != null) {
            FirebaseAuthManager.userAuthenticationResult.onNext(AuthenticationResult.SIGN_IN_SUCCESS)
            openMainApp()
        } else {
            setupAuthView()
        }
    }

    private fun setupAuthView() {
        navController = findNavController(R.id.navHostFragmentContentAuth)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setSupportActionBar(binding.appBarLayout.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val signOut = intent.getBooleanExtra(Constants.SIGN_OUT, false)
        if (signOut) {
            navController.navigate(R.id.action_launchFragment_to_signInFragment)
        } else {
            Handler(Looper.getMainLooper()).postDelayed({
                navController.navigate(R.id.action_launchFragment_to_signInFragment)
            }, 1000)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.appBarLayout.toolbar.setNavigationIcon(R.drawable.ic_left_arrow)

            when (destination.id) {
                R.id.launchFragment, R.id.signInFragment,
                R.id.signUpFragment -> {
                    binding.appBarLayout.root.visibility = View.GONE
                    binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.deep_turquoise_blue_color))
                }
                else -> {
                    binding.appBarLayout.root.visibility = View.VISIBLE
                    binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                }
            }
        }
    }

    private fun openMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun observeAuthResult() {
        FirebaseAuthManager.userAuthenticationResult
            .subscribe(
                { result ->
                    when (result) {
                        AuthenticationResult.SIGN_IN_SUCCESS,
                        AuthenticationResult.SIGN_UP_SUCCESS -> {
                            openMainApp()
                        }
                        AuthenticationResult.ACCOUNT_DEACTIVATED -> {
                            showDialog(getString(R.string.error),
                                getString(R.string.account_deactivated_message))
                        }
                        else -> {}
                    }
                },
                { error ->
                    error.printStackTrace()
                }
            ).disposeBy(disposables)
    }

    private fun showDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder
            .setMessage(message)
            .setTitle(title)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}