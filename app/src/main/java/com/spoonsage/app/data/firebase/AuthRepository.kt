package com.spoonsage.app.data.firebase

import android.app.Activity
import android.util.Log
import androidx.annotation.StringRes
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.spoonsage.app.R
import com.spoonsage.app.data.local.LocalUser
import com.spoonsage.app.data.local.LocalUserDao
import com.spoonsage.app.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class AuthUser(val uid: String, val email: String, val displayName: String, val offline: Boolean = false)

class AuthException(@StringRes val messageRes: Int, cause: Throwable? = null) : Exception(cause)

class AuthRepository(private val localUserDao: LocalUserDao) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentFirebaseUser get() = auth.currentUser

    suspend fun register(name: String, email: String, password: String): Result<AuthUser> = try {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: throw AuthException(R.string.error_register_failed)
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
        saveLocalCopy(user.uid, email, name, password)
        Log.d(TAG, "Registered new user ${user.uid}")
        Result.success(AuthUser(user.uid, email.trim(), name))
    } catch (e: Exception) {
        Log.e(TAG, "Register failed", e)
        Result.failure(mapError(e, isRegister = true))
    }

    suspend fun login(email: String, password: String): Result<AuthUser> = try {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        val user = result.user ?: throw AuthException(R.string.error_login_failed)
        val name = user.displayName ?: email.substringBefore("@")
        saveLocalCopy(user.uid, email, name, password)
        Log.d(TAG, "Online login OK for ${user.uid}")
        Result.success(AuthUser(user.uid, email.trim(), name))
    } catch (e: Exception) {
        if (e is FirebaseNetworkException) {
            Log.w(TAG, "No internet - trying offline login")
            offlineLogin(email, password)
        } else {
            Log.e(TAG, "Login failed", e)
            Result.failure(mapError(e, isRegister = false))
        }
    }

    private suspend fun offlineLogin(email: String, password: String): Result<AuthUser> {
        val local = localUserDao.findByEmail(email.trim())
            ?: return Result.failure(AuthException(R.string.error_offline_first_login))
        val ok = withContext(Dispatchers.Default) { PasswordHasher.verify(password, local.salt, local.passwordHash) }
        return if (ok) {
            Log.d(TAG, "Offline login OK using stored password hash")
            Result.success(AuthUser(local.uid, local.email, local.displayName, offline = true))
        } else {
            Result.failure(AuthException(R.string.error_wrong_credentials))
        }
    }

    suspend fun signInWithGoogle(activity: Activity): Result<AuthUser> = try {

        val clientIdRes = activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)
        if (clientIdRes == 0) {
            Log.e(TAG, "default_web_client_id missing: enable Google sign-in in Firebase and re-download google-services.json")
            throw AuthException(R.string.error_google_not_setup)
        }
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(activity.getString(clientIdRes))
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
        val response = CredentialManager.create(activity).getCredential(activity, request)
        val credential = response.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
            val result = auth.signInWithCredential(firebaseCredential).await()
            val user = result.user ?: throw AuthException(R.string.error_google_failed)
            Log.d(TAG, "Google SSO OK for ${user.uid}")
            Result.success(
                AuthUser(
                    uid = user.uid,
                    email = user.email ?: googleCredential.id,
                    displayName = user.displayName ?: googleCredential.displayName ?: ""
                )
            )
        } else {
            Result.failure(AuthException(R.string.error_google_failed))
        }
    } catch (e: GetCredentialCancellationException) {
        Log.d(TAG, "User cancelled Google sign-in")
        Result.failure(AuthException(R.string.error_google_cancelled, e))
    } catch (e: NoCredentialException) {
        Log.w(TAG, "No Google account on device", e)
        Result.failure(AuthException(R.string.error_google_no_account, e))
    } catch (e: AuthException) {
        Result.failure(e)
    } catch (e: Exception) {
        Log.e(TAG, "Google sign-in failed", e)
        Result.failure(if (e is FirebaseNetworkException) AuthException(R.string.error_no_internet, e) else AuthException(R.string.error_google_failed, e))
    }

    suspend fun logout(activity: Activity) {
        auth.signOut()
        try {
            CredentialManager.create(activity).clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w(TAG, "clearCredentialState failed (safe to ignore)", e)
        }
        Log.d(TAG, "Logged out")
    }

    private suspend fun saveLocalCopy(uid: String, email: String, name: String, password: String) =
        withContext(Dispatchers.Default) {
            val salt = PasswordHasher.newSalt()
            val hash = PasswordHasher.hash(password, salt)
            localUserDao.upsert(LocalUser(email.trim().lowercase(), uid, name, hash, salt))
        }

    private fun mapError(e: Exception, isRegister: Boolean): AuthException = when (e) {
        is AuthException -> e
        is FirebaseNetworkException -> AuthException(R.string.error_no_internet, e)
        is FirebaseAuthUserCollisionException -> AuthException(R.string.error_email_in_use, e)
        is FirebaseAuthInvalidUserException -> AuthException(R.string.error_wrong_credentials, e)
        is FirebaseAuthInvalidCredentialsException ->
            AuthException(if (isRegister) R.string.error_invalid_email else R.string.error_wrong_credentials, e)
        else -> AuthException(if (isRegister) R.string.error_register_failed else R.string.error_login_failed, e)
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
