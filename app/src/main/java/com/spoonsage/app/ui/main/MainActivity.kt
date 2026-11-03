package com.spoonsage.app.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.spoonsage.app.R
import com.spoonsage.app.databinding.ActivityMainBinding
import com.spoonsage.app.notifications.ReminderScheduler
import com.spoonsage.app.notifications.SpoonSageMessagingService
import com.spoonsage.app.ui.coach.AiCoachFragment
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.spoonApp
import com.spoonsage.app.ui.home.DashboardFragment
import com.spoonsage.app.ui.mealplan.MealPlanFragment
import com.spoonsage.app.ui.profile.ProfileFragment
import com.spoonsage.app.ui.recipes.RecipeSearchFragment
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d(TAG, "Notification permission granted=$granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_recipes -> RecipeSearchFragment()
                R.id.nav_meal_plan -> MealPlanFragment()
                R.id.nav_coach -> AiCoachFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> DashboardFragment()
            }
            showTab(fragment)
            true
        }
        if (savedInstanceState == null) showTab(DashboardFragment())

        spoonApp.networkMonitor.isOnline.observe(this) { online -> binding.offlineBanner.show(!online) }

        askForNotificationPermission()
        setUpPushNotifications()
        rescheduleReminders()
    }

    private fun showTab(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun openScreen(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun selectTab(itemId: Int) {
        binding.bottomNav.selectedItemId = itemId
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun setUpPushNotifications() {
        lifecycleScope.launch {
            try {
                FirebaseMessaging.getInstance().subscribeToTopic(SpoonSageMessagingService.TOPIC_TIPS).await()
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d(TAG, "FCM token: $token")
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null && !spoonApp.prefs.isGuest) spoonApp.firestore.saveFcmToken(uid, token)
            } catch (e: Exception) {
                Log.w(TAG, "FCM setup failed (probably offline) - will retry next launch", e)
            }
        }
    }

    private fun rescheduleReminders() {
        lifecycleScope.launch {
            spoonApp.repository.allReminders().forEach { ReminderScheduler.schedule(this@MainActivity, it) }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
