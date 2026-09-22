package com.spoonsage.app.viewmodel

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Verifies the Sign Up / Login validation rules in [AuthValidator]
 * (Section 3.1.1) - the checks that stop obviously-bad input from being
 * sent to Firebase Authentication at all.
 */
class AuthValidatorTest {

    @Test
    fun `sign up rejects a blank email`() {
        val error = AuthValidator.validateSignUp(email = "", password = "password123")

        assertThat(error).isNotNull()
    }

    @Test
    fun `sign up rejects an email with no at-sign`() {
        val error = AuthValidator.validateSignUp(email = "not-an-email", password = "password123")

        assertThat(error).isNotNull()
    }

    @Test
    fun `sign up rejects a password shorter than 6 characters`() {
        val error = AuthValidator.validateSignUp(email = "user@example.com", password = "abc12")

        assertThat(error).isNotNull()
    }

    @Test
    fun `sign up accepts a valid email and a 6-character password`() {
        val error = AuthValidator.validateSignUp(email = "user@example.com", password = "abc123")

        assertThat(error).isNull()
    }

    @Test
    fun `login rejects a blank password even with a valid email`() {
        val error = AuthValidator.validateLogin(email = "user@example.com", password = "")

        assertThat(error).isNotNull()
    }

    @Test
    fun `login accepts any non-blank email and password`() {
        val error = AuthValidator.validateLogin(email = "user@example.com", password = "whatever")

        assertThat(error).isNull()
    }
}
