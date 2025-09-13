package com.example.medijourney.modules.base.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.medijourney.R
import com.example.medijourney.common.constants.Constants
import com.example.medijourney.databinding.ActivityAuthBinding
import com.example.medijourney.modules.base.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    // Properties
    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthActivityViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpView()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHostFragmentContentAuth)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Functions
    private fun setUpView() {
        if (viewModel.checkUserLoggedIn()) {
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
        observeWindowInsets()
        observeDestinationChanged()

        val signOut = intent.getBooleanExtra(Constants.SIGN_OUT, false)
        if (signOut) {
            navController.navigate(R.id.action_launchFragment_to_signInFragment)
        } else {
            lifecycleScope.launch {
                delay(1000)
                navController.navigate(R.id.action_launchFragment_to_signInFragment)
            }
        }
    }

    private fun observeWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            updateViewTopMargin(v, insets)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun updateViewTopMargin(view: View, insets: Insets) {
        val destination = navController.currentDestination ?: return
        val top = when(destination.id) {
            R.id.launchFragment,
            R.id.signInFragment,
            R.id.signUpFragment  -> {
                0
            }
            else -> insets.top
        }
        view.updateLayoutParams<MarginLayoutParams> {
            topMargin = top
        }
    }

    private fun observeDestinationChanged() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.appBarLayout.toolbar.setNavigationIcon(R.drawable.ic_back)

            when (destination.id) {
                R.id.launchFragment,
                R.id.signInFragment,
                R.id.signUpFragment -> {
                    binding.appBarLayout.root.visibility = View.GONE
                    setRootViewColor(R.color.deep_turquoise_blue_color)
                }
                else -> {
                    showAppBarWithToolbar(
                        textColor = R.color.disable_grey_color,
                        backgroundColor = R.color.white,
                        dividerColor = R.color.gray_400,
                        navigationIconTint = R.color.disable_grey_color
                    )
                    setRootViewColor(R.color.white)
                }
            }
        }
    }

    private fun showAppBarWithToolbar(textColor: Int, backgroundColor: Int, dividerColor: Int, navigationIconTint: Int) {
        binding.appBarLayout.root.visibility = View.VISIBLE
        binding.appBarLayout.toolbar.setTitleTextColor(ContextCompat.getColor(this, textColor))
        binding.appBarLayout.toolbar.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
        binding.appBarLayout.viewDivider.setBackgroundColor(ContextCompat.getColor(this, dividerColor))
        binding.appBarLayout.toolbar.setNavigationIconTint(ContextCompat.getColor(this, navigationIconTint))
    }

    private fun setRootViewColor(backgroundColor: Int) {
        binding.root.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
    }

    fun openMainApp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}