package com.spoonsage.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.spoonsage.app.data.FirestoreUserRepository
import com.spoonsage.app.data.UserPreferences
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "SpoonSage/Auth"

/**
 * Handles the Welcome / Login / Sign Up / Continue-as-guest flow
 * (Section 3.1.1, Figure 2). Real accounts are backed by Firebase
 * Authentication (email/password sign up+login, anonymous for guest);
 * UserPreferences just mirrors the signed-in state locally so the rest
 * of the app can keep reading it the same way as before.
 *
 * Input validation itself lives in [AuthValidator] so it can be unit
 * tested independently of Firebase.
 */
class AuthViewModel(
    private val auth: FirebaseAuth,
    private val firestoreUserRepository: FirestoreUserRepository,
    private val preferences: UserPreferences
) : ViewModel() {

    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set

    fun onEmailChange(value: String) { email = value; errorMessage = null }
    fun onPasswordChange(value: String) { password = value; errorMessage = null }

    /** Creates a new Firebase Auth account and starts the local session. */
    fun signUp(onSuccess: () -> Unit) {
        val validationError = AuthValidator.validateSignUp(email, password)
        if (validationError != null) {
            errorMessage = validationError
            return
        }
        isLoading = true
        Log.d(TAG, "signUp: attempting account creation")
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email.trim(), password).await()
                preferences.setAccount(email.trim())
                Log.d(TAG, "signUp: success, uid=${auth.currentUser?.uid}")
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "signUp: failed", e)
                errorMessage = e.localizedMessage ?: "Couldn't create that account. Try again."
            } finally {
                isLoading = false
            }
        }
    }

    /** Logs in to an existing Firebase Auth account and pulls the cloud profile down. */
    fun login(onSuccess: () -> Unit) {
        val validationError = AuthValidator.validateLogin(email, password)
        if (validationError != null) {
            errorMessage = validationError
            return
        }
        isLoading = true
        Log.d(TAG, "login: attempting sign-in")
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
                preferences.setAccount(email.trim())
                firestoreUserRepository.pullProfileInto(preferences)
                Log.d(TAG, "login: success, uid=${auth.currentUser?.uid}")
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "login: failed", e)
                errorMessage = "No account matches that email and password. Try Sign Up instead."
            } finally {
                isLoading = false
            }
        }
    }

    /** Signs in anonymously via Firebase Auth so guest data still has a real uid to sync under. */
    fun continueAsGuest(onSuccess: () -> Unit) {
        Log.d(TAG, "continueAsGuest: attempting anonymous sign-in")
        viewModelScope.launch {
            try {
                auth.signInAnonymously().await()
                Log.d(TAG, "continueAsGuest: success, uid=${auth.currentUser?.uid}")
            } catch (e: Exception) {
                // Fall through - the app still works locally even if the anonymous sign-in fails.
                Log.e(TAG, "continueAsGuest: anonymous sign-in failed, continuing offline", e)
            }
            preferences.continueAsGuest()
            onSuccess()
        }
    }
}
