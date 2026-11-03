package com.spoonsage.app.ui.recipes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.spoonsage.app.R
import com.spoonsage.app.databinding.FragmentRecipeSearchBinding
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.hideKeyboard
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.text
import com.spoonsage.app.ui.main.MainActivity
import kotlinx.coroutines.launch

class RecipeSearchFragment : Fragment() {

    private var _binding: FragmentRecipeSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RecipeAdapter
    private var byIngredients = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecipeSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = RecipeAdapter { recipe ->
            (requireActivity() as MainActivity).openScreen(RecipeDetailFragment.newInstance(recipe.id))
        }
        binding.rvRecipes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecipes.adapter = adapter

        binding.toggleMode.check(R.id.btn_by_name)
        binding.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            byIngredients = checkedId == R.id.btn_by_ingredients
            binding.tilSearch.hint = getString(if (byIngredients) R.string.search_ingredients_hint else R.string.search_recipes_hint)
        }

        binding.btnSearch.setOnClickListener { search() }
        binding.tilSearch.editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { search(); true } else false
        }

        showSavedRecipes()
    }

    private fun showSavedRecipes() {
        app.repository.observeFavorites().observe(viewLifecycleOwner) { favourites ->
            if (binding.tvSectionTitle.tag == "results") return@observe
            if (favourites.isNotEmpty()) {
                binding.tvSectionTitle.setText(R.string.your_favourites)
                adapter.submitList(favourites)
                binding.tvEmpty.show(false)
            } else {
                viewLifecycleOwner.lifecycleScope.launch {
                    binding.tvSectionTitle.setText(R.string.suggested_recipes)
                    adapter.submitList(app.repository.suggestedRecipes())
                }
            }
        }
    }

    private fun search() {
        val query = binding.tilSearch.text
        if (query.isBlank()) {
            binding.tilSearch.error = getString(R.string.error_required)
            return
        }
        binding.tilSearch.error = null
        hideKeyboard()
        binding.progress.show(true)
        binding.tvInfo.show(false)
        Log.d(TAG, "Searching (byIngredients=$byIngredients): $query")

        viewLifecycleOwner.lifecycleScope.launch {
            val result = if (byIngredients) app.repository.searchByIngredients(query)
                         else app.repository.searchByName(query)
            if (_binding == null) return@launch
            binding.progress.show(false)
            binding.tvSectionTitle.tag = "results"
            binding.tvSectionTitle.setText(R.string.results)
            adapter.submitList(result.recipes)
            binding.tvEmpty.show(result.recipes.isEmpty())
            if (result.fromCache) {
                binding.tvInfo.show(true)
                binding.tvInfo.setText(R.string.showing_saved_results)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "RecipeSearch"
    }
}
