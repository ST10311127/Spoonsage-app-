package com.spoonsage.app.ui.recipes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spoonsage.app.R
import com.spoonsage.app.data.local.Recipe
import com.spoonsage.app.databinding.ItemRecipeBinding
import com.spoonsage.app.ui.common.loadRecipeImage
import com.spoonsage.app.ui.common.optionLabel

class RecipeAdapter(private val onClick: (Recipe) -> Unit) :
    ListAdapter<Recipe, RecipeAdapter.Holder>(Diff) {

    class Holder(val binding: ItemRecipeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val recipe = getItem(position)
        val ctx = holder.itemView.context
        with(holder.binding) {
            tvTitle.text = recipe.title

            val parts = mutableListOf<String>()
            if (recipe.readyInMinutes > 0) parts += ctx.getString(R.string.minutes_value, recipe.readyInMinutes)
            if (recipe.calories > 0) parts += ctx.getString(R.string.kcal_value, recipe.calories)
            if (recipe.difficulty.isNotBlank()) parts += ctx.optionLabel(recipe.difficulty)
            tvMeta.text = parts.joinToString(" · ").ifBlank { ctx.getString(R.string.tap_for_details) }
            ivFavorite.setImageResource(if (recipe.isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
            ivImage.loadRecipeImage(recipe.imageUrl)
            root.setOnClickListener { onClick(recipe) }
        }
    }

    object Diff : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(a: Recipe, b: Recipe) = a.id == b.id
        override fun areContentsTheSame(a: Recipe, b: Recipe) = a == b
    }
}
