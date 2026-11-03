package com.spoonsage.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.spoonsage.app.R
import com.spoonsage.app.databinding.FragmentDashboardBinding
import com.spoonsage.app.ui.auth.AuthActivity
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.showLogProgressDialog
import com.spoonsage.app.ui.common.showQuickAddMenu
import com.spoonsage.app.ui.grocery.GroceryFragment
import com.spoonsage.app.ui.main.MainActivity
import com.spoonsage.app.ui.mealplan.MealPlanAdapter
import com.spoonsage.app.ui.progress.ProgressFragment
import com.spoonsage.app.ui.recipes.RecipeDetailFragment
import com.spoonsage.app.util.DateUtils
import com.spoonsage.app.util.Options
import java.util.Locale
import kotlin.math.roundToInt

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = app.prefs
        val main = requireActivity() as MainActivity

        binding.tvGreeting.text = getString(R.string.greeting, prefs.displayName.ifBlank { getString(R.string.default_user_name) })
        binding.tvDate.text = DateUtils.prettyToday(Locale.getDefault())

        binding.cardGuest.show(prefs.isGuest)
        binding.btnGuestSignup.setOnClickListener {
            prefs.isGuest = false
            startActivity(android.content.Intent(requireContext(), AuthActivity::class.java))
            requireActivity().finish()
        }

        app.repository.observeToday().observe(viewLifecycleOwner) { log ->
            val calories = log?.caloriesConsumed ?: 0
            val protein = log?.proteinConsumedG ?: 0
            val water = log?.waterConsumedL ?: 0.0
            binding.tvCalories.text = getString(R.string.of_target_kcal, calories, prefs.dailyCalorieTarget)
            binding.progressCalories.progress = percent(calories.toDouble(), prefs.dailyCalorieTarget.toDouble())
            binding.tvProtein.text = getString(R.string.of_target_g, protein, prefs.dailyProteinTargetG)
            binding.progressProtein.progress = percent(protein.toDouble(), prefs.dailyProteinTargetG.toDouble())
            binding.tvWater.text = getString(R.string.of_target_l, water, prefs.dailyWaterTargetL)
            binding.progressWater.progress = percent(water, prefs.dailyWaterTargetL.toDouble())
            binding.tvStreak.text = resources.getQuantityString(R.plurals.streak_days, log?.streakCount ?: 0, log?.streakCount ?: 0)
        }

        binding.btnLogProgress.setOnClickListener { showLogProgressDialog() }
        binding.btnGrocery.setOnClickListener { main.openScreen(GroceryFragment()) }
        binding.btnProgress.setOnClickListener { main.openScreen(ProgressFragment()) }
        binding.btnFindRecipes.setOnClickListener { main.selectTab(R.id.nav_recipes) }
        binding.fabQuickAdd.setOnClickListener { showQuickAddMenu() }

        val adapter = MealPlanAdapter(onClick = { main.openScreen(RecipeDetailFragment.newInstance(it.recipeId)) })
        binding.rvTodayMeals.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTodayMeals.adapter = adapter
        binding.btnViewPlan.setOnClickListener { main.selectTab(R.id.nav_meal_plan) }
        app.repository.observeMealsForDay(DateUtils.todayDayName()).observe(viewLifecycleOwner) { meals ->
            val sorted = meals.sortedBy { Options.MEAL_TYPES.indexOf(it.mealType) }
            adapter.submitList(sorted)
            binding.tvNoMeals.show(sorted.isEmpty())
        }

        val plan = when (prefs.goalType) {
            Options.GOAL_LOSE_WEIGHT -> getString(R.string.plan_low_carb)
            Options.GOAL_BUILD_MUSCLE -> getString(R.string.plan_high_protein)
            Options.GOAL_DRINK_WATER -> getString(R.string.plan_hydration)
            else -> getString(R.string.plan_balanced)
        }
        binding.tvRecommendation.text = getString(R.string.recommendation_text, plan)
        binding.btnAskCoach.setOnClickListener { main.selectTab(R.id.nav_coach) }
    }

    private fun percent(value: Double, target: Double): Int =
        if (target <= 0) 0 else ((value / target) * 100).roundToInt().coerceIn(0, 100)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
