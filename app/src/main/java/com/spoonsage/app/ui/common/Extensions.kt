package com.spoonsage.app.ui.common

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.spoonsage.app.R
import com.spoonsage.app.SpoonSageApp
import com.spoonsage.app.util.Options
import com.spoonsage.app.util.Validators

val Fragment.app: SpoonSageApp get() = requireActivity().application as SpoonSageApp
val Activity.spoonApp: SpoonSageApp get() = application as SpoonSageApp

fun Fragment.toast(message: String) = Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
fun Fragment.toast(resId: Int) = toast(getString(resId))

fun Fragment.snack(message: String) {
    view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG).show() }
}

fun Fragment.hideKeyboard() {
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
}

fun View.show(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

val TextInputLayout.text: String get() = editText?.text?.toString()?.trim().orEmpty()

fun TextInputLayout.check(error: Validators.Error?): Boolean {
    this.error = error?.let { context.getString(errorMessage(it)) }
    return error == null
}

fun errorMessage(error: Validators.Error): Int = when (error) {
    Validators.Error.EMPTY -> R.string.error_required
    Validators.Error.INVALID_EMAIL -> R.string.error_invalid_email
    Validators.Error.SHORT_PASSWORD -> R.string.error_short_password
    Validators.Error.PASSWORD_MISMATCH -> R.string.error_password_mismatch
    Validators.Error.NOT_A_NUMBER -> R.string.error_not_a_number
    Validators.Error.OUT_OF_RANGE -> R.string.error_out_of_range
    Validators.Error.INVALID_TIME -> R.string.error_invalid_time
}

fun ImageView.loadRecipeImage(url: String?) {
    Glide.with(this)
        .load(url)
        .placeholder(R.drawable.bg_placeholder)
        .error(R.drawable.bg_placeholder)
        .centerCrop()
        .into(this)
}

fun Context.optionLabel(value: String): String {
    val res = when (value) {
        Options.GENDER_MALE -> R.string.opt_male
        Options.GENDER_FEMALE -> R.string.opt_female
        Options.GENDER_NONE -> R.string.opt_prefer_not
        Options.ACTIVITY_LOW -> R.string.opt_low
        Options.ACTIVITY_MEDIUM -> R.string.opt_medium
        Options.ACTIVITY_HIGH -> R.string.opt_high
        Options.GOAL_LOSE_WEIGHT -> R.string.opt_lose_weight
        Options.GOAL_BUILD_MUSCLE -> R.string.opt_build_muscle
        Options.GOAL_EAT_HEALTHIER -> R.string.opt_eat_healthier
        Options.GOAL_DRINK_WATER -> R.string.opt_drink_water
        "Classic" -> R.string.diet_classic
        "Vegetarian" -> R.string.diet_vegetarian
        "Vegan" -> R.string.diet_vegan
        "Pescatarian" -> R.string.diet_pescatarian
        "Keto" -> R.string.diet_keto
        "Paleo" -> R.string.diet_paleo
        "Halal" -> R.string.diet_halal
        "Kosher" -> R.string.diet_kosher
        "Monday" -> R.string.day_monday
        "Tuesday" -> R.string.day_tuesday
        "Wednesday" -> R.string.day_wednesday
        "Thursday" -> R.string.day_thursday
        "Friday" -> R.string.day_friday
        "Saturday" -> R.string.day_saturday
        "Sunday" -> R.string.day_sunday
        "Breakfast" -> R.string.meal_breakfast
        "Lunch" -> R.string.meal_lunch
        "Dinner" -> R.string.meal_dinner
        "Snack" -> R.string.meal_snack
        Options.REMINDER_WATER -> R.string.reminder_type_water
        Options.REMINDER_MEAL -> R.string.reminder_type_meal
        Options.REMINDER_LOG -> R.string.reminder_type_log
        "Easy" -> R.string.difficulty_easy
        "Hard" -> R.string.difficulty_hard
        else -> 0
    }
    return if (res == 0) value else getString(res)
}
