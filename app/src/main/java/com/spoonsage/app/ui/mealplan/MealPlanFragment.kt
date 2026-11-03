package com.spoonsage.app.ui.mealplan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.spoonsage.app.R
import com.spoonsage.app.data.local.MealPlanEntry
import com.spoonsage.app.databinding.FragmentMealPlanBinding
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.optionLabel
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.snack
import com.spoonsage.app.ui.grocery.GroceryFragment
import com.spoonsage.app.ui.main.MainActivity
import com.spoonsage.app.ui.recipes.RecipeDetailFragment
import com.spoonsage.app.util.DateUtils
import com.spoonsage.app.util.Options
import kotlinx.coroutines.launch

class MealPlanFragment : Fragment() {

    private var _binding: FragmentMealPlanBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MealPlanAdapter
    private var selectedDay = DateUtils.todayDayName()
    private var allEntries: List<MealPlanEntry> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMealPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val main = requireActivity() as MainActivity
        selectedDay = savedInstanceState?.getString(STATE_DAY) ?: selectedDay

        Options.DAYS.forEach { day ->
            val chip = Chip(requireContext()).apply {
                text = requireContext().optionLabel(day).take(3)
                tag = day
                isCheckable = true
                isChecked = day == selectedDay
                setOnClickListener { selectedDay = day; render() }
            }
            binding.chipsDays.addView(chip)
        }

        adapter = MealPlanAdapter(
            onClick = { main.openScreen(RecipeDetailFragment.newInstance(it.recipeId)) },
            onDelete = { entry ->
                viewLifecycleOwner.lifecycleScope.launch {
                    app.repository.removeFromMealPlan(entry)
                    snack(getString(R.string.removed_from_plan, entry.recipeTitle))
                }
            }
        )
        binding.rvMeals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMeals.adapter = adapter

        binding.btnGrocery.setOnClickListener { main.openScreen(GroceryFragment()) }
        binding.fabAdd.setOnClickListener { main.selectTab(R.id.nav_recipes) }
        binding.btnClearWeek.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_week)
                .setMessage(R.string.clear_week_confirm)
                .setPositiveButton(R.string.clear) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch { app.repository.clearWeek() }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        app.repository.observeMealPlan().observe(viewLifecycleOwner) { entries ->
            allEntries = entries
            render()
        }
    }

    private fun render() {
        val forDay = allEntries.filter { it.dayOfWeek == selectedDay }.sortedBy { Options.MEAL_TYPES.indexOf(it.mealType) }
        adapter.submitList(forDay)
        binding.tvEmpty.show(forDay.isEmpty())
        binding.tvDayTitle.text = requireContext().optionLabel(selectedDay)
        binding.tvDayTotal.text = getString(R.string.day_total, forDay.sumOf { it.calories }, forDay.size)
        binding.tvWeekTotal.text = getString(R.string.week_total, allEntries.size)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_DAY, selectedDay)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val STATE_DAY = "selected_day"
    }
}
