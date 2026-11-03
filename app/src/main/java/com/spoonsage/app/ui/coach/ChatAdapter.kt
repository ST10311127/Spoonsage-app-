package com.spoonsage.app.ui.coach

import com.spoonsage.app.data.local.isFromUser
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.spoonsage.app.R
import com.spoonsage.app.data.local.AiCoachMessage
import com.spoonsage.app.databinding.ItemChatCoachBinding
import com.spoonsage.app.databinding.ItemChatUserBinding
import com.spoonsage.app.ui.common.show

class ChatAdapter(private val onSuggestionClick: (String) -> Unit) :
    ListAdapter<AiCoachMessage, RecyclerView.ViewHolder>(Diff) {

    class UserHolder(val binding: ItemChatUserBinding) : RecyclerView.ViewHolder(binding.root)
    class CoachHolder(val binding: ItemChatCoachBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int) = if (getItem(position).isFromUser) TYPE_USER else TYPE_COACH

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) UserHolder(ItemChatUserBinding.inflate(inflater, parent, false))
               else CoachHolder(ItemChatCoachBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        when (holder) {
            is UserHolder -> holder.binding.tvMessage.text = msg.text
            is CoachHolder -> {
                holder.binding.tvMessage.text = msg.text
                val suggestion = msg.suggestedRecipeTitle
                holder.binding.btnSuggestion.show(suggestion != null)
                if (suggestion != null) {
                    holder.binding.btnSuggestion.text =
                        holder.itemView.context.getString(R.string.see_recipe, suggestion)
                    holder.binding.btnSuggestion.setOnClickListener { onSuggestionClick(suggestion) }
                }
            }
        }
    }

    object Diff : DiffUtil.ItemCallback<AiCoachMessage>() {
        override fun areItemsTheSame(a: AiCoachMessage, b: AiCoachMessage) = a.id == b.id
        override fun areContentsTheSame(a: AiCoachMessage, b: AiCoachMessage) = a == b
    }

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_COACH = 2
    }
}
