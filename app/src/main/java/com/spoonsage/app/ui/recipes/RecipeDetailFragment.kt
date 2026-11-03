package com.spoonsage.app.ui.recipes

import com.spoonsage.app.data.local.ingredients
import com.spoonsage.app.data.local.steps
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.spoonsage.app.R
import com.spoonsage.app.data.local.Recipe
import com.spoonsage.app.databinding.FragmentRecipeDetailBinding
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.loadRecipeImage
import com.spoonsage.app.ui.common.optionLabel
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.showAddToPlanDialog
import com.spoonsage.app.ui.common.toast
import com.spoonsage.app.ui.main.MainActivity
import kotlinx.coroutines.launch

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!
    private var recipe: Recipe? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnFavorite.setOnClickListener { toggleFavorite() }
        binding.btnAddToPlan.setOnClickListener { recipe?.let { showAddToPlanDialog(it) } }
        binding.btnStartCooking.setOnClickListener {
            val r = recipe ?: return@setOnClickListener
            if (r.steps.isEmpty()) toast(R.string.no_steps)
            else (requireActivity() as MainActivity).openScreen(CookingModeFragment.newInstance(r.id))
        }
        load()
    }

    private fun load() {
        val id = requireArguments().getInt(ARG_ID)
        binding.progress.show(true)
        binding.content.show(false)
        viewLifecycleOwner.lifecycleScope.launch {
            val loaded = app.repository.getRecipeDetail(id)
            if (_binding == null) return@launch
            binding.progress.show(false)
            if (loaded == null) {
                binding.tvError.show(true)
                return@launch
            }
            recipe = loaded
            bind(loaded)
        }
    }

    private fun bind(r: Recipe) {
        binding.content.show(true)
        binding.ivImage.loadRecipeImage(r.imageUrl)
        binding.tvTitle.text = r.title
        val meta = mutableListOf<String>()
        if (r.readyInMinutes > 0) meta += getString(R.string.minutes_value, r.readyInMinutes)
        if (r.servings > 0) meta += getString(R.string.serves_value, r.servings)
        if (r.difficulty.isNotBlank()) meta += requireContext().optionLabel(r.difficulty)
        binding.tvMeta.text = meta.joinToString(" · ")
        binding.tvNutrition.text = getString(R.string.nutrition_value, r.calories, r.protein)
        binding.tvNutrition.show(r.calories > 0)
        binding.tvIngredients.text = r.ingredients.joinToString("\n") { "•  $it" }.ifBlank { getString(R.string.no_ingredients) }
        binding.tvSteps.text = r.steps.mapIndexed { i, s -> "${i + 1}.  $s" }.joinToString("\n\n").ifBlank { getString(R.string.no_steps) }
        updateFavoriteIcon(r.isFavorite)
    }

    private fun toggleFavorite() {
        val r = recipe ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val nowFavorite = app.repository.toggleFavorite(r)
            recipe = r.copy(isFavorite = nowFavorite)
            updateFavoriteIcon(nowFavorite)
            toast(if (nowFavorite) R.string.added_to_favourites else R.string.removed_from_favourites)
        }
    }

    private fun updateFavoriteIcon(favorite: Boolean) {
        binding.btnFavorite.setImageResource(if (favorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID = "recipe_id"

        fun newInstance(recipeId: Int) = RecipeDetailFragment().apply { arguments = bundleOf(ARG_ID to recipeId) }
    }
}
