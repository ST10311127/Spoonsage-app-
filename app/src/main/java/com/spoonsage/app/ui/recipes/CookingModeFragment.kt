package com.spoonsage.app.ui.recipes

import com.spoonsage.app.data.local.steps
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.spoonsage.app.R
import com.spoonsage.app.databinding.FragmentCookingModeBinding
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.toast
import kotlinx.coroutines.launch

class CookingModeFragment : Fragment() {

    private var _binding: FragmentCookingModeBinding? = null
    private val binding get() = _binding!!
    private var steps: List<String> = emptyList()
    private var index = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCookingModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        index = savedInstanceState?.getInt(STATE_INDEX) ?: 0
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnPrev.setOnClickListener { if (index > 0) { index--; showStep() } }
        binding.btnNext.setOnClickListener {
            if (index < steps.lastIndex) { index++; showStep() }
            else { toast(R.string.enjoy_your_meal); parentFragmentManager.popBackStack() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val recipe = app.repository.getRecipeDetail(requireArguments().getInt(ARG_ID))
            if (_binding == null) return@launch
            binding.toolbar.title = recipe?.title ?: getString(R.string.cooking_mode)
            steps = recipe?.steps.orEmpty()
            if (steps.isEmpty()) {
                toast(R.string.no_steps)
                parentFragmentManager.popBackStack()
                return@launch
            }
            index = index.coerceIn(0, steps.lastIndex)
            showStep()
        }
    }

    private fun showStep() {
        binding.tvStepCounter.text = getString(R.string.step_of, index + 1, steps.size)
        binding.tvStep.text = steps[index]
        binding.progressSteps.max = steps.size
        binding.progressSteps.progress = index + 1
        binding.btnPrev.isEnabled = index > 0
        binding.btnNext.setText(if (index == steps.lastIndex) R.string.done else R.string.next)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_INDEX, index)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID = "recipe_id"
        private const val STATE_INDEX = "step_index"
        fun newInstance(recipeId: Int) = CookingModeFragment().apply { arguments = bundleOf(ARG_ID to recipeId) }
    }
}
