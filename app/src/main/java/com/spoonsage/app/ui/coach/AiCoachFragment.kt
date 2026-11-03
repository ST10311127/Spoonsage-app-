package com.spoonsage.app.ui.coach

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.spoonsage.app.R
import com.spoonsage.app.databinding.FragmentAiCoachBinding
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.hideKeyboard
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.toast
import com.spoonsage.app.ui.main.MainActivity
import com.spoonsage.app.ui.recipes.RecipeDetailFragment
import kotlinx.coroutines.launch

class AiCoachFragment : Fragment() {

    private var _binding: FragmentAiCoachBinding? = null
    private val binding get() = _binding!!
    private var sending = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAiCoachBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ChatAdapter(onSuggestionClick = { title -> openSuggestion(title) })
        val layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        binding.rvChat.layoutManager = layoutManager
        binding.rvChat.adapter = adapter

        app.repository.observeCoachMessages().observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages) {
                if (messages.isNotEmpty()) binding.rvChat.scrollToPosition(messages.lastIndex)
            }
            binding.emptyState.show(messages.isEmpty())
        }

        listOf(R.string.suggest_breakfast, R.string.suggest_dinner, R.string.suggest_water).forEach { res ->
            val chip = Chip(requireContext()).apply {
                text = getString(res)
                setOnClickListener { send(getString(res)) }
            }
            binding.chipsSuggestions.addView(chip)
        }

        binding.btnSend.setOnClickListener { send(binding.etMessage.text?.toString().orEmpty()) }
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { send(binding.etMessage.text?.toString().orEmpty()); true } else false
        }
        binding.toolbar.setOnMenuItemClickListener {
            viewLifecycleOwner.lifecycleScope.launch { app.repository.clearCoachChat() }
            true
        }
    }

    private fun send(raw: String) {
        val text = raw.trim()
        if (text.isEmpty() || sending) return
        if (text.length > 500) {
            toast(R.string.message_too_long)
            return
        }
        sending = true
        binding.etMessage.setText("")
        hideKeyboard()
        binding.tvTyping.show(true)
        binding.btnSend.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            app.repository.sendCoachMessage(
                text,
                offlineReply = getString(R.string.coach_offline),
                errorReply = getString(R.string.coach_error)
            )
            sending = false
            if (_binding == null) return@launch
            binding.tvTyping.show(false)
            binding.btnSend.isEnabled = true
        }
    }

    private fun openSuggestion(title: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val id = app.repository.findRecipeForSuggestion(title)
            if (id != null) (requireActivity() as MainActivity).openScreen(RecipeDetailFragment.newInstance(id))
            else toast(R.string.no_matching_recipe)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
