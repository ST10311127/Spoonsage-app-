package com.spoonsage.app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.spoonsage.app.databinding.ActivitySplashBinding
import com.spoonsage.app.ui.auth.AuthActivity
import com.spoonsage.app.ui.common.spoonApp
import com.spoonsage.app.ui.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(1200)
            val prefs = spoonApp.prefs
            val signedIn = prefs.isLoggedIn || prefs.isGuest
            val target = when {
                signedIn && prefs.profileSetupDone -> MainActivity::class.java
                else -> AuthActivity::class.java
            }
            Log.d("SplashActivity", "Routing to ${target.simpleName} (signedIn=$signedIn, setupDone=${prefs.profileSetupDone})")
            startActivity(Intent(this@SplashActivity, target))
            finish()
        }
    }
}
