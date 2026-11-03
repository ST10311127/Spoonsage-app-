package com.spoonsage.app.ui.profile

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.spoonsage.app.R
import com.spoonsage.app.data.local.Reminder
import com.spoonsage.app.databinding.FragmentRemindersBinding
import com.spoonsage.app.databinding.ItemReminderBinding
import com.spoonsage.app.notifications.ReminderScheduler
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.optionLabel
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.toast
import com.spoonsage.app.util.Options
import kotlinx.coroutines.launch
import java.util.Locale

class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        val adapter = ReminderAdapter(
            onToggle = { r, enabled ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val updated = r.copy(isEnabled = enabled)
                    app.repository.updateReminder(updated)
                    ReminderScheduler.schedule(requireContext(), updated)
                }
            },
            onDelete = { r ->
                viewLifecycleOwner.lifecycleScope.launch {
                    ReminderScheduler.cancel(requireContext(), r)
                    app.repository.deleteReminder(r)
                }
            }
        )
        binding.rvReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReminders.adapter = adapter
        app.repository.observeReminders().observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.tvEmpty.show(it.isEmpty())
        }
        binding.fabAdd.setOnClickListener { chooseType() }
    }

    private fun chooseType() {
        val labels = Options.REMINDER_TYPES.map { requireContext().optionLabel(it) }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.add_a_reminder)
            .setItems(labels) { _, which -> chooseTime(Options.REMINDER_TYPES[which]) }
            .show()
    }

    private fun chooseTime(type: String) {
        TimePickerDialog(requireContext(), { _, hour, minute ->
            val time = String.format(Locale.US, "%02d:%02d", hour, minute)
            viewLifecycleOwner.lifecycleScope.launch {
                val reminder = app.repository.addReminder(type, time)
                ReminderScheduler.schedule(requireContext(), reminder)
                toast(getString(R.string.reminder_set, requireContext().optionLabel(type), time))
            }
        }, 8, 0, DateFormat.is24HourFormat(requireContext())).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class ReminderAdapter(
        val onToggle: (Reminder, Boolean) -> Unit,
        val onDelete: (Reminder) -> Unit
    ) : ListAdapter<Reminder, ReminderAdapter.Holder>(object : DiffUtil.ItemCallback<Reminder>() {
        override fun areItemsTheSame(a: Reminder, b: Reminder) = a.id == b.id
        override fun areContentsTheSame(a: Reminder, b: Reminder) = a == b
    }) {
        class Holder(val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            Holder(ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val r = getItem(position)
            with(holder.binding) {
                tvType.text = root.context.optionLabel(r.type)
                tvTime.text = r.time
                switchEnabled.setOnCheckedChangeListener(null)
                switchEnabled.isChecked = r.isEnabled
                switchEnabled.setOnCheckedChangeListener { _, checked -> onToggle(r, checked) }
                btnDelete.setOnClickListener { onDelete(r) }
            }
        }
    }
}
