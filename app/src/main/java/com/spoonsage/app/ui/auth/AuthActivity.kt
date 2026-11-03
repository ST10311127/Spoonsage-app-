package com.spoonsage.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.spoonsage.app.R
import com.spoonsage.app.databinding.ActivityAuthBinding
import com.spoonsage.app.ui.common.spoonApp
import com.spoonsage.app.ui.main.MainActivity
import com.spoonsage.app.ui.setup.ProfileSetupFragment

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val prefs = spoonApp.prefs

            val start: Fragment = if ((prefs.isLoggedIn || prefs.isGuest) && !prefs.profileSetupDone) {
                ProfileSetupFragment()
            } else {
                WelcomeFragment()
            }
            supportFragmentManager.beginTransaction().replace(R.id.auth_container, start).commit()
        }
    }

    fun navigateTo(fragment: Fragment, addToBackStack: Boolean = true) {
        val tx = supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.auth_container, fragment)
        if (addToBackStack) tx.addToBackStack(null)
        tx.commit()
    }

    fun onSignedIn() {
        if (spoonApp.prefs.profileSetupDone) openMain()
        else {
            supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            navigateTo(ProfileSetupFragment(), addToBackStack = false)
        }
    }

    fun openMain() {
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }
}
