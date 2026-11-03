package com.spoonsage.app.ui.mealplan

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spoonsage.app.R
import com.spoonsage.app.data.local.MealPlanEntry
import com.spoonsage.app.databinding.ItemMealPlanBinding
import com.spoonsage.app.ui.common.loadRecipeImage
import com.spoonsage.app.ui.common.optionLabel
import com.spoonsage.app.ui.common.show

class MealPlanAdapter(
    private val onClick: (MealPlanEntry) -> Unit,
    private val onDelete: ((MealPlanEntry) -> Unit)? = null
) : ListAdapter<MealPlanEntry, MealPlanAdapter.Holder>(Diff) {

    class Holder(val binding: ItemMealPlanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemMealPlanBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val entry = getItem(position)
        val ctx = holder.itemView.context
        with(holder.binding) {
            tvMealType.text = ctx.optionLabel(entry.mealType)
            tvTitle.text = entry.recipeTitle
            tvCalories.text = if (entry.calories > 0) ctx.getString(R.string.kcal_value, entry.calories) else ""
            ivImage.loadRecipeImage(entry.recipeImageUrl)
            root.setOnClickListener { onClick(entry) }
            btnDelete.show(onDelete != null)
            btnDelete.setOnClickListener { onDelete?.invoke(entry) }
        }
    }

    object Diff : DiffUtil.ItemCallback<MealPlanEntry>() {
        override fun areItemsTheSame(a: MealPlanEntry, b: MealPlanEntry) = a.id == b.id
        override fun areContentsTheSame(a: MealPlanEntry, b: MealPlanEntry) = a == b
    }
}
