package com.spoonsage.app.util

object Validators {

    enum class Error { EMPTY, INVALID_EMAIL, SHORT_PASSWORD, PASSWORD_MISMATCH, NOT_A_NUMBER, OUT_OF_RANGE, INVALID_TIME }

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val TIME_REGEX = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")
    const val MIN_PASSWORD_LENGTH = 6

    fun email(value: String): Error? = when {
        value.isBlank() -> Error.EMPTY
        !EMAIL_REGEX.matches(value.trim()) -> Error.INVALID_EMAIL
        else -> null
    }

    fun password(value: String): Error? = when {
        value.isEmpty() -> Error.EMPTY
        value.length < MIN_PASSWORD_LENGTH -> Error.SHORT_PASSWORD
        else -> null
    }

    fun confirmPassword(password: String, confirm: String): Error? =
        if (password != confirm) Error.PASSWORD_MISMATCH else null

    fun required(value: String): Error? = if (value.isBlank()) Error.EMPTY else null

    fun number(value: String, min: Double, max: Double, optional: Boolean = false): Error? {
        if (value.isBlank()) return if (optional) null else Error.EMPTY
        val n = value.trim().replace(',', '.').toDoubleOrNull() ?: return Error.NOT_A_NUMBER
        return if (n < min || n > max) Error.OUT_OF_RANGE else null
    }

    fun time(value: String): Error? = when {
        value.isBlank() -> Error.EMPTY
        !TIME_REGEX.matches(value.trim()) -> Error.INVALID_TIME
        else -> null
    }

    fun toDouble(value: String, default: Double = 0.0): Double =
        value.trim().replace(',', '.').toDoubleOrNull() ?: default

    fun toInt(value: String, default: Int = 0): Int =
        value.trim().replace(',', '.').toDoubleOrNull()?.toInt() ?: default
}
