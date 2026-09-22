package com.spoonsage.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.spoonsage.app.data.UserPreferences
import com.spoonsage.app.ui.navigation.SpoonSageNavHost
import com.spoonsage.app.ui.theme.SpoonSageTheme
import com.spoonsage.app.viewmodel.AppViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as SpoonSageApp
        val preferences = UserPreferences(applicationContext)
        val factory = AppViewModelFactory(app.repository, preferences, app.auth, app.firestoreUserRepository)

        setContent {
            SpoonSageTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SpoonSageNavHost(factory = factory, preferences = preferences)
                }
            }
        }
    }
}
