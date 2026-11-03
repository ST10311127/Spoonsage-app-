package com.spoonsage.app.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spoonsage.app.R
import com.spoonsage.app.data.local.ProgressLog
import com.spoonsage.app.databinding.FragmentProgressBinding
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.optionLabel
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.showLogProgressDialog
import com.spoonsage.app.util.DateUtils
import com.spoonsage.app.util.StreakCalculator
import java.util.Locale
import kotlin.math.roundToInt

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnLogToday.setOnClickListener { showLogProgressDialog() }

        app.repository.observeRecentLogs(30).observe(viewLifecycleOwner) { logs -> render(logs) }
    }

    private fun render(logs: List<ProgressLog>) {
        val prefs = app.prefs
        binding.tvNoData.show(logs.isEmpty())

        val latest = logs.firstOrNull()
        val streak = if (latest != null && (latest.date == DateUtils.today() || latest.date == DateUtils.daysAgo(1))) latest.streakCount else 0
        binding.tvStreak.text = resources.getQuantityString(R.plurals.streak_days, streak, streak)
        val badges = StreakCalculator.badgesFor(streak)
        binding.badge3.isEnabled = 3 in badges
        binding.badge7.isEnabled = 7 in badges
        binding.badge14.isEnabled = 14 in badges
        binding.badge3.alpha = if (3 in badges) 1f else 0.4f
        binding.badge7.alpha = if (7 in badges) 1f else 0.4f
        binding.badge14.alpha = if (14 in badges) 1f else 0.4f

        val byDate = logs.associateBy { it.date }
        val days = (6 downTo 0).map { DateUtils.daysAgo(it) }
        val week = days.mapNotNull { byDate[it] }
        val loggedDays = week.size.coerceAtLeast(1)
        binding.tvAvgCalories.text = (week.sumOf { it.caloriesConsumed } / loggedDays).toString()
        binding.tvAvgProtein.text = getString(R.string.grams_value, week.sumOf { it.proteinConsumedG } / loggedDays)
        binding.tvAvgWater.text = getString(R.string.litres_value, week.sumOf { it.waterConsumedL } / loggedDays)

        binding.chartCalories.setData(
            values = days.map { (byDate[it]?.caloriesConsumed ?: 0).toFloat() },
            labels = days.map { DateUtils.shortDay(it, Locale.getDefault()) },
            target = prefs.dailyCalorieTarget.toFloat()
        )

        val today = byDate[DateUtils.today()]
        binding.tvGoal.text = getString(R.string.goal_label, requireContext().optionLabel(prefs.goalType))
        binding.tvTargetWeight.show(prefs.targetWeightKg > 0)
        binding.tvTargetWeight.text = getString(R.string.target_weight_label, prefs.targetWeightKg, prefs.weightKg)
        setGoal(binding.tvGoalCalories, binding.progressGoalCalories, R.string.of_target_kcal,
            (today?.caloriesConsumed ?: 0).toDouble(), prefs.dailyCalorieTarget.toDouble(), isInt = true)
        setGoal(binding.tvGoalProtein, binding.progressGoalProtein, R.string.of_target_g,
            (today?.proteinConsumedG ?: 0).toDouble(), prefs.dailyProteinTargetG.toDouble(), isInt = true)
        setGoal(binding.tvGoalWater, binding.progressGoalWater, R.string.of_target_l,
            today?.waterConsumedL ?: 0.0, prefs.dailyWaterTargetL.toDouble(), isInt = false)
    }

    private fun setGoal(
        label: android.widget.TextView,
        bar: com.google.android.material.progressindicator.LinearProgressIndicator,
        format: Int, value: Double, target: Double, isInt: Boolean
    ) {
        label.text = if (isInt) getString(format, value.toInt(), target.toInt()) else getString(format, value, target.toFloat())
        bar.progress = if (target <= 0) 0 else ((value / target) * 100).roundToInt().coerceIn(0, 100)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
