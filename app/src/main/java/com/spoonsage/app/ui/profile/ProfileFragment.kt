package com.spoonsage.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.spoonsage.app.R
import com.spoonsage.app.databinding.DialogDietaryBinding
import com.spoonsage.app.databinding.DialogGoalsBinding
import com.spoonsage.app.databinding.DialogPersonalInfoBinding
import com.spoonsage.app.databinding.FragmentProfileBinding
import com.spoonsage.app.notifications.ReminderScheduler
import com.spoonsage.app.ui.auth.AuthActivity
import com.spoonsage.app.ui.common.Dropdown
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.check
import com.spoonsage.app.ui.common.checkedValues
import com.spoonsage.app.ui.common.optionLabel
import com.spoonsage.app.ui.common.setupFilterChips
import com.spoonsage.app.ui.common.text
import com.spoonsage.app.ui.common.toast
import com.spoonsage.app.ui.main.MainActivity
import com.spoonsage.app.ui.progress.ProgressFragment
import com.spoonsage.app.ui.settings.SettingsFragment
import com.spoonsage.app.util.Options
import com.spoonsage.app.util.TargetCalculator
import com.spoonsage.app.util.Validators
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val main = requireActivity() as MainActivity
        binding.rowPersonal.setOnClickListener { editPersonalInfo() }
        binding.rowDietary.setOnClickListener { editDietary() }
        binding.rowGoals.setOnClickListener { editGoals() }
        binding.rowReminders.setOnClickListener { main.openScreen(RemindersFragment()) }
        binding.rowProgress.setOnClickListener { main.openScreen(ProgressFragment()) }
        binding.rowSettings.setOnClickListener { main.openScreen(SettingsFragment()) }
        binding.btnLogout.setOnClickListener { confirmLogout() }
        app.repository.observeReminders().observe(viewLifecycleOwner) { reminders ->
            binding.tvRemindersSummary.text = resources.getQuantityString(
                R.plurals.reminders_active, reminders.count { it.isEnabled }, reminders.count { it.isEnabled })
        }
        render()
    }

    private fun render() {
        val p = app.prefs
        val name = p.displayName.ifBlank { getString(R.string.default_user_name) }
        binding.tvAvatar.text = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
        binding.tvName.text = name
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val viaGoogle = firebaseUser?.providerData?.any { it.providerId == GoogleAuthProvider.PROVIDER_ID } == true
        binding.tvEmail.text = when {
            p.isGuest -> getString(R.string.guest_mode)
            viaGoogle -> getString(R.string.signed_in_with_google, p.email)
            else -> p.email
        }
        binding.tvPersonalSummary.text = getString(
            R.string.personal_summary, p.age, requireContext().optionLabel(p.gender), p.heightCm.toInt(), p.weightKg
        )
        binding.tvDietarySummary.text = getString(R.string.diet_summary, requireContext().optionLabel(p.diet),
            p.dislikes.size)
        binding.tvGoalsSummary.text = getString(R.string.goals_summary, requireContext().optionLabel(p.goalType),
            p.dailyCalorieTarget, p.dailyProteinTargetG, p.dailyWaterTargetL)
        binding.btnLogout.setText(if (p.isGuest) R.string.exit_guest_mode else R.string.log_out)
    }

    private fun saved() {
        app.repository.requestSync()
        render()
        toast(R.string.saved)
    }

    private fun editPersonalInfo() {
        val p = app.prefs
        val b = DialogPersonalInfoBinding.inflate(layoutInflater)
        b.tilName.editText?.setText(p.displayName)
        b.tilAge.editText?.setText(p.age.toString())
        b.tilHeight.editText?.setText(p.heightCm.toInt().toString())
        b.tilWeight.editText?.setText(p.weightKg.toString())
        val gender = Dropdown(b.tilGender, Options.GENDERS, p.gender)
        val activity = Dropdown(b.tilActivity, Options.ACTIVITY_LEVELS, p.activityLevel)

        showValidatedDialog(R.string.personal_health_info, b.root) {
            val ok = listOf(
                b.tilName.check(Validators.required(b.tilName.text)),
                b.tilAge.check(Validators.number(b.tilAge.text, 10.0, 110.0)),
                b.tilHeight.check(Validators.number(b.tilHeight.text, 100.0, 250.0)),
                b.tilWeight.check(Validators.number(b.tilWeight.text, 25.0, 300.0))
            ).all { it }
            if (ok) {
                p.displayName = b.tilName.text
                p.age = Validators.toInt(b.tilAge.text)
                p.heightCm = Validators.toDouble(b.tilHeight.text).toFloat()
                p.weightKg = Validators.toDouble(b.tilWeight.text).toFloat()
                p.gender = gender.value
                p.activityLevel = activity.value
                saved()
            }
            ok
        }
    }

    private fun editDietary() {
        val p = app.prefs
        val b = DialogDietaryBinding.inflate(layoutInflater)
        val diet = Dropdown(b.tilDiet, Options.DIETS, p.diet)
        b.chipsDislikes.setupFilterChips(Options.DISLIKES, p.dislikes)
        showValidatedDialog(R.string.dietary_preferences, b.root) {
            p.diet = diet.value
            p.dislikes = b.chipsDislikes.checkedValues
            saved()
            true
        }
    }

    private fun editGoals() {
        val p = app.prefs
        val b = DialogGoalsBinding.inflate(layoutInflater)
        val goal = Dropdown(b.tilGoal, Options.GOALS, p.goalType)
        b.tilTargetWeight.editText?.setText(if (p.targetWeightKg > 0) p.targetWeightKg.toString() else "")
        b.tilCalories.editText?.setText(p.dailyCalorieTarget.toString())
        b.tilProtein.editText?.setText(p.dailyProteinTargetG.toString())
        b.tilWater.editText?.setText(p.dailyWaterTargetL.toString())

        b.btnRecalculate.setOnClickListener {
            val t = TargetCalculator.calculate(p.age, p.gender, p.heightCm.toDouble(), p.weightKg.toDouble(), p.activityLevel, goal.value)
            b.tilCalories.editText?.setText(t.calories.toString())
            b.tilProtein.editText?.setText(t.proteinG.toString())
            b.tilWater.editText?.setText(t.waterL.toString())
        }
        showValidatedDialog(R.string.your_goals, b.root) {
            val ok = listOf(
                b.tilTargetWeight.check(Validators.number(b.tilTargetWeight.text, 25.0, 300.0, optional = true)),
                b.tilCalories.check(Validators.number(b.tilCalories.text, 800.0, 6000.0)),
                b.tilProtein.check(Validators.number(b.tilProtein.text, 10.0, 400.0)),
                b.tilWater.check(Validators.number(b.tilWater.text, 0.5, 8.0))
            ).all { it }
            if (ok) {
                p.goalType = goal.value
                p.targetWeightKg = Validators.toDouble(b.tilTargetWeight.text, 0.0).toFloat()
                p.dailyCalorieTarget = Validators.toInt(b.tilCalories.text)
                p.dailyProteinTargetG = Validators.toInt(b.tilProtein.text)
                p.dailyWaterTargetL = Validators.toDouble(b.tilWater.text).toFloat()
                saved()
            }
            ok
        }
    }

    private fun showValidatedDialog(titleRes: Int, content: View, onSave: () -> Boolean) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(titleRes)
            .setView(content)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { if (onSave()) dialog.dismiss() }
        }
        dialog.show()
    }

    private fun confirmLogout() {
        val guest = app.prefs.isGuest
        viewLifecycleOwner.lifecycleScope.launch {

            if (!guest && app.networkMonitor.isConnected()) app.syncManager.syncAll()
            val pending = if (guest) 0 else app.syncManager.pendingCount()
            if (_binding == null) return@launch
            val message = when {
                guest -> getString(R.string.exit_guest_warning)
                pending > 0 -> resources.getQuantityString(R.plurals.logout_unsynced_warning, pending, pending)
                else -> getString(R.string.logout_confirm)
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(if (guest) R.string.exit_guest_mode else R.string.log_out)
                .setMessage(message)
                .setPositiveButton(if (guest) R.string.exit_guest_mode else R.string.log_out) { _, _ -> logout() }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun logout() {
        val activity = requireActivity()
        val application = app
        application.appScope.launch {
            application.repository.allReminders().forEach { ReminderScheduler.cancel(activity, it) }
            application.authRepository.logout(activity)
            application.repository.clearUserData()
            application.prefs.clearSession()
            Log.d("ProfileFragment", "Session cleared")
            activity.runOnUiThread {
                activity.startActivity(Intent(activity, AuthActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                activity.finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
