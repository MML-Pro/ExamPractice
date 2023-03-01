package com.example.exampractice.ui.activites


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavArgument
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.exampractice.R
import com.example.exampractice.databinding.ActivityHomeBinding
import com.example.exampractice.databinding.NavHeaderMainBinding
import com.example.exampractice.util.Resource
import com.example.exampractice.viewmodels.CredentialsViewModel
import dagger.hilt.android.AndroidEntryPoint


private const val TAG = "HomeActivity"

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {


    private lateinit var binding: ActivityHomeBinding
    private lateinit var navHeaderMainBinding: NavHeaderMainBinding

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val credentialsViewModel by viewModels<CredentialsViewModel>()

    var userName = "UserName"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

//        val drawerLayout: DrawerLayout = binding.drawerLayout
//        val navView: NavigationView = binding.navView
        navHeaderMainBinding = NavHeaderMainBinding.bind(binding.navView.getHeaderView(0))

        credentialsViewModel.getUserData()


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_bottom_bar) as NavHostFragment
        navController = navHostFragment.navController

        val extras = intent.extras
        if (extras != null) {
            val email = extras.getString("email")
            val name = extras.getString("name")

            Log.d(TAG, "onCreate: email is $email")
            Log.d(TAG, "onCreate: name is $name")

            //The key argument here must match that used in the other activity
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.categoryFragment, R.id.profileFragment, R.id.leaderBoardFragment
            ), binding.drawerLayout
        )
        setupActionBarWithNavController(this, navController, appBarConfiguration)
        setupWithNavController(binding.navView, navController)

        binding.bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.categoryFragment ->{
                    if (!binding.bottomNavigationView.isVisible){
                        binding.bottomNavigationView.visibility = View.VISIBLE
                    }
                }

                R.id.questionsFragment -> {
                    binding.apply {
                        toolbar.visibility = View.GONE
                        bottomNavigationView.visibility = View.GONE

                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START)

                        }
                    }


                }
                R.id.testFragment,R.id.resultFragment -> {
                    //                binding.bottomNavigationView.removeAllViews()
                    binding.apply {

                        bottomNavigationView.visibility = View.GONE
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START)

                        }
                    }
                }


            }
        }





    }

    override fun onStart() {
        super.onStart()

        lifecycleScope.launchWhenStarted {
            credentialsViewModel.user.collect { result ->
                when (result) {
                    is Resource.Success -> {

                        result.data?.let {
                            userName = it.userName
                            navHeaderMainBinding.navDrawerName.text = it.userName
                            navHeaderMainBinding.navDrawerTextImage.text =
                                it.userName.uppercase().substring(0, 1)
                        }


                    }
                    is Resource.Error -> {
                        navHeaderMainBinding.navDrawerName.error = result.message.toString()
                    }
                    else -> {}
                }
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}