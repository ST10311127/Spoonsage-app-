package com.spoonsage.app.ui.grocery

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spoonsage.app.data.local.GroceryItem
import com.spoonsage.app.databinding.ItemGroceryBinding

class GroceryAdapter(
    private val onToggle: (GroceryItem) -> Unit,
    private val onDelete: (GroceryItem) -> Unit
) : ListAdapter<GroceryItem, GroceryAdapter.Holder>(Diff) {

    class Holder(val binding: ItemGroceryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemGroceryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            cbChecked.setOnCheckedChangeListener(null)
            cbChecked.isChecked = item.isChecked
            tvName.text = item.name
            tvName.paintFlags = if (item.isChecked) tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                else tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            tvName.alpha = if (item.isChecked) 0.5f else 1f
            cbChecked.setOnCheckedChangeListener { _, _ -> onToggle(item) }
            root.setOnClickListener { onToggle(item) }
            btnDelete.setOnClickListener { onDelete(item) }
        }
    }

    object Diff : DiffUtil.ItemCallback<GroceryItem>() {
        override fun areItemsTheSame(a: GroceryItem, b: GroceryItem) = a.id == b.id
        override fun areContentsTheSame(a: GroceryItem, b: GroceryItem) = a == b
    }
}
