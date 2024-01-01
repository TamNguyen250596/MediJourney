package com.example.medijourney.modules.base.main

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.medijourney.R
import com.example.medijourney.common.managers.InternationManager
import com.example.medijourney.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // Properties
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val viewModel: MainActivityViewModel by viewModels()

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch {
            InternationManager.config()
        }
        setUpView()
        viewModel.onCreate()
        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHostFragmentContentMain)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Functions
    private fun setUpView() {
        navController = findNavController(R.id.navHostFragmentContentMain)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setSupportActionBar(binding.appBarLayout.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, bundle ->
            binding.appBarLayout.toolbar.setNavigationIcon(R.drawable.ic_back)
            setRootViewColor(R.color.white)

            when (destination.id) {
                R.id.homeDashboardFragment, R.id.healthCenterFragment,
                R.id.mainProfileFragment -> {
                    showBottomNav(hideAppBar = true)
                }
                R.id.mainChatFragment -> {
                    showBottomNav(hideAppBar = false)
                    binding.appBarLayout.toolbar.navigationIcon = null
                }
                R.id.notificationFragment -> {
                    viewModel.updateUnreadNotificationCount(0)
                    showBottomNav(hideAppBar = false)
                    binding.appBarLayout.toolbar.navigationIcon = null
                }
                R.id.selectExerciseLevelFragment -> {
                    val showAppBar = bundle?.getBoolean("showAppBar") ?: false
                    toggleAppBar(showAppBar)
                }
                R.id.videoPlayerFragment -> {
                    showAppBarWithToolbar(
                        textColor = R.color.white,
                        backgroundColor = R.color.black,
                        dividerColor = R.color.black,
                        navigationIconTint = R.color.white
                    )
                    binding.bottomNavView.visibility = View.GONE
                    setRootViewColor(R.color.black)
                }
                else -> {
                    showAppBarWithToolbar(
                        textColor = R.color.disable_grey_color,
                        backgroundColor = R.color.white,
                        dividerColor = R.color.gray_400,
                        navigationIconTint = R.color.disable_grey_color
                    )
                    binding.bottomNavView.visibility = View.GONE
                    setRootViewColor(R.color.white)
                }
            }
        }
    }

    private fun showBottomNav(hideAppBar: Boolean) {
        binding.appBarLayout.root.visibility = if (hideAppBar) View.GONE else View.VISIBLE
        binding.bottomNavView.visibility = View.VISIBLE
    }

    private fun toggleAppBar(isVisible: Boolean) {
        binding.appBarLayout.root.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.bottomNavView.visibility = binding.appBarLayout.root.visibility
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

    private fun observeViewModel() {
        viewModel.unreadNotificationCount.observe(this) {
            if (it == 0) {
                binding.bottomNavView.removeBadge(R.id.notificationFragment)
            } else {
                binding.bottomNavView.getOrCreateBadge(R.id.notificationFragment).number = it
            }
        }
    }

    fun switchToTab(id: Int, data: Map<String, Any>? = null) {
        binding.bottomNavView.selectedItemId = id
        data?.forEach {
            navController.currentBackStackEntry?.savedStateHandle?.set(it.key, it.value)
        }
    }
}