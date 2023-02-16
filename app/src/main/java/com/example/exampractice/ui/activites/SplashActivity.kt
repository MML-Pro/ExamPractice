package com.example.exampractice.ui.activites

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.exampractice.R
import com.example.exampractice.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "SplashActivity"
@AndroidEntryPoint
class SplashActivity  : AppCompatActivity() {



    private lateinit var binding: ActivitySplashBinding
    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val typeface = ResourcesCompat.getFont(this, R.font.blacklist)

        val animation = AnimationUtils.loadAnimation(this, R.anim.my_anim)

        binding.apply {
            splashTextView.typeface = typeface
            splashTextView.animation = animation
        }

        Log.d(TAG, "onCreate: ${firebaseAuth.currentUser?.providerData?.get(0)?.email}")



    }

    override fun onStart() {
        super.onStart()

        Handler(Looper.getMainLooper()).postDelayed({ /* Create an Intent that will start the Menu-Activity. */

            if(firebaseAuth.currentUser == null) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
                this@SplashActivity.startActivity(intent)
                this@SplashActivity.finish()
            }else{
                val intent = Intent(this@SplashActivity, HomeActivity::class.java)
                startActivity(intent)
                this@SplashActivity.startActivity(intent)
                this@SplashActivity.finish()
            }
        }, 3000)
    }
}