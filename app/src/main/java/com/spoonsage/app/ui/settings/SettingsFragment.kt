package com.spoonsage.app.ui.settings

import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.spoonsage.app.BuildConfig
import com.spoonsage.app.R
import com.spoonsage.app.SpoonSageApp
import com.spoonsage.app.databinding.FragmentSettingsBinding
import com.spoonsage.app.notifications.NotificationHelper
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.toast
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = app.prefs
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        val currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags().take(2)
        binding.groupLanguage.check(
            when (currentLang) {
                "zu" -> R.id.rb_zulu
                "af" -> R.id.rb_afrikaans
                else -> R.id.rb_english
            }
        )
        binding.groupLanguage.setOnCheckedChangeListener { _, checkedId ->
            val tag = when (checkedId) {
                R.id.rb_zulu -> "zu"
                R.id.rb_afrikaans -> "af"
                else -> "en"
            }
            Log.d(TAG, "Changing app language to $tag")

            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }

        binding.groupTheme.check(
            when (prefs.themeMode) {
                "light" -> R.id.rb_light
                "dark" -> R.id.rb_dark
                else -> R.id.rb_system
            }
        )
        binding.groupTheme.setOnCheckedChangeListener { _, checkedId ->
            prefs.themeMode = when (checkedId) {
                R.id.rb_light -> "light"
                R.id.rb_dark -> "dark"
                else -> "system"
            }
            SpoonSageApp.applyTheme(prefs.themeMode)
        }

        binding.switchNotifications.isChecked = prefs.notificationsEnabled
        binding.switchNotifications.setOnCheckedChangeListener { _, checked -> prefs.notificationsEnabled = checked }
        binding.btnTestNotification.setOnClickListener {
            NotificationHelper.show(requireContext(), R.string.channel_push_id,
                getString(R.string.test_notification_title), getString(R.string.test_notification_body))
            if (!prefs.notificationsEnabled) toast(R.string.notifications_are_off)
        }

        binding.switchAutoSync.isChecked = prefs.autoSync
        binding.switchAutoSync.setOnCheckedChangeListener { _, checked -> prefs.autoSync = checked }
        binding.cardSync.show(!prefs.isGuest)
        binding.tvGuestSync.show(prefs.isGuest)
        binding.btnSyncNow.setOnClickListener { syncNow() }
        app.networkMonitor.isOnline.observe(viewLifecycleOwner) { refreshSyncStatus() }

        binding.tvVersion.text = getString(R.string.version_value, BuildConfig.VERSION_NAME)
    }

    private fun syncNow() {
        if (!app.networkMonitor.isConnected()) {
            toast(R.string.sync_needs_internet)
            return
        }
        binding.btnSyncNow.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            val count = app.syncManager.syncAll()
            if (_binding == null) return@launch
            binding.btnSyncNow.isEnabled = true
            toast(resources.getQuantityString(R.plurals.sync_done_body, count, count))
            refreshSyncStatus()
        }
    }

    private fun refreshSyncStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            val pending = app.syncManager.pendingCount()
            if (_binding == null) return@launch
            val last = app.prefs.lastSyncTime
            val lastText = if (last == 0L) getString(R.string.never)
                           else DateUtils.getRelativeTimeSpanString(last).toString()
            val online = getString(if (app.networkMonitor.isConnected()) R.string.online else R.string.offline)
            binding.tvSyncStatus.text = getString(R.string.sync_status, online, pending, lastText)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
}
