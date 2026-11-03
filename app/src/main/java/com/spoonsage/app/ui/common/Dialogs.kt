package com.spoonsage.app.ui.common

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.spoonsage.app.R
import com.spoonsage.app.data.local.Recipe
import com.spoonsage.app.databinding.DialogAddToPlanBinding
import com.spoonsage.app.databinding.DialogLogProgressBinding
import com.spoonsage.app.databinding.DialogSingleInputBinding
import com.spoonsage.app.util.DateUtils
import com.spoonsage.app.util.Options
import com.spoonsage.app.util.Validators
import kotlinx.coroutines.launch

fun Fragment.showQuickAddMenu() {
    val items = arrayOf(getString(R.string.log_todays_progress), getString(R.string.add_grocery_item))
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.quick_add)
        .setItems(items) { _, which -> if (which == 0) showLogProgressDialog() else showAddGroceryDialog() }
        .show()
}

fun Fragment.showLogProgressDialog() {
    val b = DialogLogProgressBinding.inflate(LayoutInflater.from(requireContext()))
    val dialog = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.log_todays_progress)
        .setView(b.root)
        .setPositiveButton(R.string.log_it, null)
        .setNegativeButton(R.string.cancel, null)
        .create()
    dialog.setOnShowListener {
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val calOk = b.tilCalories.check(Validators.number(b.tilCalories.text, 0.0, 10000.0, optional = true))
            val proOk = b.tilProtein.check(Validators.number(b.tilProtein.text, 0.0, 500.0, optional = true))
            val watOk = b.tilWater.check(Validators.number(b.tilWater.text, 0.0, 10.0, optional = true))
            if (!(calOk && proOk && watOk)) return@setOnClickListener
            val calories = Validators.toInt(b.tilCalories.text)
            val protein = Validators.toInt(b.tilProtein.text)
            val water = Validators.toDouble(b.tilWater.text)
            if (calories == 0 && protein == 0 && water == 0.0) {
                b.tilCalories.error = getString(R.string.error_enter_something)
                return@setOnClickListener
            }
            viewLifecycleOwner.lifecycleScope.launch {
                app.repository.logToday(calories, protein, water)
                toast(R.string.progress_logged)
            }
            dialog.dismiss()
        }
    }
    dialog.show()
}

fun Fragment.showAddGroceryDialog() {
    val b = DialogSingleInputBinding.inflate(LayoutInflater.from(requireContext()))
    b.til.hint = getString(R.string.item_name)
    val dialog = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.add_grocery_item)
        .setView(b.root)
        .setPositiveButton(R.string.add, null)
        .setNegativeButton(R.string.cancel, null)
        .create()
    dialog.setOnShowListener {
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (!b.til.check(Validators.required(b.til.text))) return@setOnClickListener
            val name = b.til.text
            viewLifecycleOwner.lifecycleScope.launch {
                val added = app.repository.addGroceryItem(name)
                toast(if (added) getString(R.string.added_to_list, name) else getString(R.string.already_on_list, name))
            }
            dialog.dismiss()
        }
    }
    dialog.show()
}

fun Fragment.showAddToPlanDialog(recipe: Recipe) {
    val b = DialogAddToPlanBinding.inflate(LayoutInflater.from(requireContext()))
    val day = Dropdown(b.tilDay, Options.DAYS, DateUtils.todayDayName())
    val meal = Dropdown(b.tilMeal, Options.MEAL_TYPES, "Dinner")
    MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.add_to_which_day)
        .setView(b.root)
        .setPositiveButton(R.string.add_to_plan) { _, _ ->
            viewLifecycleOwner.lifecycleScope.launch {
                app.repository.addToMealPlan(recipe, day.value, meal.value)
                snack(getString(R.string.added_to_plan_message, recipe.title))
            }
        }
        .setNegativeButton(R.string.cancel, null)
        .show()
}
