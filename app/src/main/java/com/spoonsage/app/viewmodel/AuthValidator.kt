package com.spoonsage.app.viewmodel

/**
 * Validates the Sign Up / Login form inputs before they're sent to
 * Firebase Authentication (Section 3.1.1). Kept separate from
 * [AuthViewModel] so the validation rules can be unit tested without
 * needing to mock FirebaseAuth.
 */
object AuthValidator {

    private const val MIN_PASSWORD_LENGTH = 6

    /** Returns a user-facing error message, or null if the input is valid for sign up. */
    fun validateSignUp(email: String, password: String): String? = when {
        email.isBlank() -> "Enter a valid email and a password of at least $MIN_PASSWORD_LENGTH characters."
        !email.contains("@") -> "Enter a valid email and a password of at least $MIN_PASSWORD_LENGTH characters."
        password.length < MIN_PASSWORD_LENGTH ->
            "Enter a valid email and a password of at least $MIN_PASSWORD_LENGTH characters."
        else -> null
    }

    /** Returns a user-facing error message, or null if the input is valid for login. */
    fun validateLogin(email: String, password: String): String? = when {
        email.isBlank() -> "Enter your email and password."
        password.isBlank() -> "Enter your email and password."
        else -> null
    }
}
