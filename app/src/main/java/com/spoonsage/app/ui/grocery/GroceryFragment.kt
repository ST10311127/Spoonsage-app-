package com.spoonsage.app.ui.grocery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.spoonsage.app.R
import com.spoonsage.app.databinding.FragmentGroceryBinding
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.check
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.text
import com.spoonsage.app.ui.common.toast
import com.spoonsage.app.util.Validators
import kotlinx.coroutines.launch

class GroceryFragment : Fragment() {

    private var _binding: FragmentGroceryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGroceryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val adapter = GroceryAdapter(
            onToggle = { item -> viewLifecycleOwner.lifecycleScope.launch { app.repository.toggleGrocery(item) } },
            onDelete = { item -> viewLifecycleOwner.lifecycleScope.launch { app.repository.deleteGrocery(item) } }
        )
        binding.rvItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvItems.adapter = adapter

        app.repository.observeGroceries().observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.tvEmpty.show(items.isEmpty())
            val left = items.count { !it.isChecked }
            binding.tvCount.text = getString(R.string.items_left, left, items.size)
            binding.btnClearChecked.isEnabled = items.any { it.isChecked }
        }

        binding.btnAdd.setOnClickListener { addItem() }
        binding.tilItem.editText?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) { addItem(); true } else false
        }
        binding.btnClearChecked.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch { app.repository.clearCheckedGroceries() }
        }
    }

    private fun addItem() {
        val name = binding.tilItem.text
        if (!binding.tilItem.check(Validators.required(name))) return
        viewLifecycleOwner.lifecycleScope.launch {
            val added = app.repository.addGroceryItem(name)
            if (added) binding.tilItem.editText?.setText("")
            else toast(getString(R.string.already_on_list, name))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
