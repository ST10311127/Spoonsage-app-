package com.spoonsage.app.ui.auth

import android.util.Log
import androidx.fragment.app.Fragment
import com.spoonsage.app.data.firebase.AuthUser
import com.spoonsage.app.ui.common.app

object SignInHelper {

    suspend fun completeSignIn(fragment: Fragment, user: AuthUser) {
        val app = fragment.app
        val prefs = app.prefs
        val previousUid = prefs.uid
        if (previousUid != null && previousUid != user.uid) {

            app.repository.clearUserData()
            prefs.clearSession()
        }
        prefs.isGuest = false
        prefs.isLoggedIn = true
        prefs.uid = user.uid
        prefs.email = user.email
        if (prefs.displayName.isBlank()) prefs.displayName = user.displayName

        if (!user.offline) {

            app.syncManager.pullAll()
        }
        Log.d("SignInHelper", "Signed in ${user.uid} (offline=${user.offline}, setupDone=${prefs.profileSetupDone})")
    }
}
