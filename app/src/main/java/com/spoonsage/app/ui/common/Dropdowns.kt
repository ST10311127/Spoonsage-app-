package com.spoonsage.app.ui.common

import android.widget.ArrayAdapter
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout

class Dropdown(private val layout: TextInputLayout, private val values: List<String>, selected: String?) {

    private val field = layout.editText as MaterialAutoCompleteTextView
    private val labels = values.map { layout.context.optionLabel(it) }
    private var selectedIndex = values.indexOf(selected).coerceAtLeast(0)

    init {
        field.setAdapter(ArrayAdapter(layout.context, android.R.layout.simple_list_item_1, labels))
        field.setText(labels.getOrNull(selectedIndex) ?: "", false)
        field.setOnItemClickListener { _, _, position, _ -> selectedIndex = position }
    }

    val value: String get() = values.getOrElse(selectedIndex) { values.first() }
}

fun com.google.android.material.chip.ChipGroup.setupFilterChips(options: List<String>, selected: Set<String>) {
    removeAllViews()
    options.forEach { option ->
        val chip = com.google.android.material.chip.Chip(context).apply {
            text = option
            tag = option
            isCheckable = true
            isChecked = option in selected
        }
        addView(chip)
    }
}

val com.google.android.material.chip.ChipGroup.checkedValues: Set<String>
    get() = (0 until childCount)
        .map { getChildAt(it) as com.google.android.material.chip.Chip }
        .filter { it.isChecked }
        .map { it.tag as String }
        .toSet()
