package com.example.exampractice.ui.activites

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.exampractice.R
import com.example.exampractice.databinding.ActivityHomeBinding
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "HomeActivity"

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {


    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

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
            setOf(R.id.categoryFragment,R.id.leaderBoardFragment,R.id.profileFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        binding.bottomNavigationView.apply {
            setupWithNavController(navController)


//            setOnItemSelectedListener {
//                when (it.itemId) {
//                    R.id.categoryFragment -> {
//                        navController.navigate(R.id.categoryFragment)
//                        return@setOnItemSelectedListener true
//                    }
//                    R.id.leaderBoardFragment -> {
//                        navController.navigate(R.id.leaderBoardFragment)
//                        return@setOnItemSelectedListener true
//                    }
//                    R.id.profileFragment -> {
//
//                        navController.navigate(R.id.profileFragment)
//                        return@setOnItemSelectedListener true
//                    }
//                }
//                false
//            }
        }

//        binding.bottomNavigationView.setupWithNavController(navController)
    }
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}