package com.example.medijourney.modules.base.main

ˆ
class MainActivity : AppCompatActivity() {

    // Properties
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val viewModel: MainActivityViewModel by viewModels()
    private var didUpdateBottomNav = false

    // Life cycle
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpView()
        observeViewModel()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navHostFragmentContentMain)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateShowSplashAd(true)
    }

    // Functions
    private fun setUpView() {
        navController = findNavController(R.id.navHostFragmentContentMain)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setSupportActionBar(binding.appBarLayout.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavView.setupWithNavController(navController)
        observeWindowInsets()
        observeDestinationChanged()
    }

    private fun observeWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            updateViewTopMargin(v, insets)
            updateBottomNavView(insets)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun updateViewTopMargin(view: View, insets: Insets) {
        val destination = navController.currentDestination ?: return
        val top = when(destination.id) {
            R.id.homeDashboardFragment, R.id.mainProfileFragment -> {
                0
            }
            else -> insets.top
        }
        view.updateLayoutParams<MarginLayoutParams> {
            topMargin = top
        }
    }

    private fun updateBottomNavView(insets: Insets) {
        if (!didUpdateBottomNav && insets.bottom > 0) {
            val additionalHeight = insets.bottom / 3
            binding.bottomNavView.updateLayoutParams {
                height += additionalHeight
            }
            binding.bottomNavView.setPadding(0, 0, 0, additionalHeight)
            didUpdateBottomNav = true
        }
    }

    private fun observeDestinationChanged() {
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
                    val showAppBar = bundle?.getBoolean("showAppBar") == true
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
        viewModel.showPlashAd.observe(this) {
            if (!it) return@observe
            showFullScreenAd()
        }
    }

    fun switchToTab(id: Int, data: Map<String, Any>? = null) {
        val currentBackStackEntry = navController.currentBackStackEntry ?: return

        binding.bottomNavView.selectedItemId = id
        data?.forEach {
            currentBackStackEntry.savedStateHandle[it.key] = it.value
        }
    }

    private fun showFullScreenAd() {
        val ad = viewModel.getSplashAd() ?: return
        if (!ad.isValid()) return
        val fragment = FullScreenAdFragment()
        fragment.imageUrlString = "images/advertisements/${ad.imageName}.png"
        fragment.actionUrlString = ad.actionUrl
        fragment.show(supportFragmentManager, FullScreenAdFragment::class.java.simpleName)
    }
}