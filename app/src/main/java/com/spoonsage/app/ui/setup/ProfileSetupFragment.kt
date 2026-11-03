package com.spoonsage.app.ui.setup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.spoonsage.app.R
import com.spoonsage.app.databinding.FragmentProfileSetupBinding
import com.spoonsage.app.ui.auth.AuthActivity
import com.spoonsage.app.ui.common.Dropdown
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.check
import com.spoonsage.app.ui.common.checkedValues
import com.spoonsage.app.ui.common.hideKeyboard
import com.spoonsage.app.ui.common.setupFilterChips
import com.spoonsage.app.ui.common.text
import com.spoonsage.app.ui.common.toast
import com.spoonsage.app.util.Options
import com.spoonsage.app.util.TargetCalculator
import com.spoonsage.app.util.Validators

class ProfileSetupFragment : Fragment() {

    private var _binding: FragmentProfileSetupBinding? = null
    private val binding get() = _binding!!

    private lateinit var gender: Dropdown
    private lateinit var activity: Dropdown
    private lateinit var goal: Dropdown
    private lateinit var diet: Dropdown

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = app.prefs
        binding.tilName.editText?.setText(prefs.displayName)
        gender = Dropdown(binding.tilGender, Options.GENDERS, prefs.gender)
        activity = Dropdown(binding.tilActivity, Options.ACTIVITY_LEVELS, prefs.activityLevel)
        goal = Dropdown(binding.tilGoal, Options.GOALS, prefs.goalType)
        diet = Dropdown(binding.tilDiet, Options.DIETS, prefs.diet)
        binding.chipsDislikes.setupFilterChips(Options.DISLIKES, prefs.dislikes)

        binding.btnFinish.setOnClickListener { finishSetup() }
    }

    private fun finishSetup() {
        hideKeyboard()
        val nameOk = binding.tilName.check(Validators.required(binding.tilName.text))
        val ageOk = binding.tilAge.check(Validators.number(binding.tilAge.text, 10.0, 110.0))
        val heightOk = binding.tilHeight.check(Validators.number(binding.tilHeight.text, 100.0, 250.0))
        val weightOk = binding.tilWeight.check(Validators.number(binding.tilWeight.text, 25.0, 300.0))
        val targetOk = binding.tilTargetWeight.check(Validators.number(binding.tilTargetWeight.text, 25.0, 300.0, optional = true))
        if (!(nameOk && ageOk && heightOk && weightOk && targetOk)) {
            toast(R.string.fix_errors)
            return
        }

        val prefs = app.prefs
        prefs.displayName = binding.tilName.text
        prefs.age = Validators.toInt(binding.tilAge.text)
        prefs.gender = gender.value
        prefs.heightCm = Validators.toDouble(binding.tilHeight.text).toFloat()
        prefs.weightKg = Validators.toDouble(binding.tilWeight.text).toFloat()
        prefs.activityLevel = activity.value
        prefs.goalType = goal.value
        prefs.targetWeightKg = Validators.toDouble(binding.tilTargetWeight.text, 0.0).toFloat()
        prefs.diet = diet.value
        prefs.dislikes = binding.chipsDislikes.checkedValues

        val targets = TargetCalculator.calculate(
            prefs.age, prefs.gender, prefs.heightCm.toDouble(), prefs.weightKg.toDouble(), prefs.activityLevel, prefs.goalType
        )
        prefs.dailyCalorieTarget = targets.calories
        prefs.dailyProteinTargetG = targets.proteinG
        prefs.dailyWaterTargetL = targets.waterL.toFloat()
        prefs.profileSetupDone = true
        Log.d("ProfileSetup", "Setup done. Targets: $targets")

        app.repository.requestSync()
        toast(getString(R.string.targets_set, targets.calories, targets.proteinG, targets.waterL))
        (requireActivity() as AuthActivity).openMain()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
