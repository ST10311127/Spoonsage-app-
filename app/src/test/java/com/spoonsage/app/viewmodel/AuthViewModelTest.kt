package com.spoonsage.app.viewmodel

import com.google.firebase.auth.FirebaseAuth
import com.spoonsage.app.data.FirestoreUserRepository
import com.spoonsage.app.data.UserPreferences
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

/**
 * Verifies [AuthViewModel]'s validation guard: invalid input must be
 * rejected locally, with a visible error message, WITHOUT ever calling
 * Firebase Authentication. This is the "fail fast on bad input" behaviour
 * the assessment brief asks for so the app never crashes on invalid input.
 *
 * Firebase/Firestore are mocked with MockK - this test never touches the
 * network, so it runs the same on any machine (and in GitHub Actions).
 */
class AuthViewModelTest {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreUserRepository: FirestoreUserRepository
    private lateinit var preferences: UserPreferences
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        auth = mockk(relaxed = true)
        firestoreUserRepository = mockk(relaxed = true)
        preferences = mockk(relaxed = true)
        viewModel = AuthViewModel(auth, firestoreUserRepository, preferences)
    }

    @Test
    fun `signUp with an invalid password shows an error and never calls Firebase`() {
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("123") // too short

        viewModel.signUp(onSuccess = { /* should not be called */ })

        assertThat(viewModel.errorMessage).isNotNull()
        verify(exactly = 0) { auth.createUserWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `login with a blank password shows an error and never calls Firebase`() {
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("")

        viewModel.login(onSuccess = { /* should not be called */ })

        assertThat(viewModel.errorMessage).isNotNull()
        verify(exactly = 0) { auth.signInWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `typing into the email field clears any previous error message`() {
        viewModel.onEmailChange("user@example.com")
        viewModel.onPasswordChange("")
        viewModel.login(onSuccess = {})
        assertThat(viewModel.errorMessage).isNotNull()

        viewModel.onEmailChange("user@example.com")

        assertThat(viewModel.errorMessage).isNull()
    }
}
