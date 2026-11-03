package com.spoonsage.app.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.spoonsage.app.R
import com.spoonsage.app.data.firebase.AuthException
import com.spoonsage.app.databinding.FragmentWelcomeBinding
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.toast
import kotlinx.coroutines.launch

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val host = requireActivity() as AuthActivity

        binding.btnGetStarted.setOnClickListener { host.navigateTo(RegisterFragment()) }
        binding.btnLogin.setOnClickListener { host.navigateTo(LoginFragment()) }

        binding.btnGoogle.setOnClickListener {
            setLoading(true)
            viewLifecycleOwner.lifecycleScope.launch {
                val result = app.authRepository.signInWithGoogle(requireActivity())
                result.onSuccess { user ->
                    SignInHelper.completeSignIn(this@WelcomeFragment, user)
                    host.onSignedIn()
                }.onFailure { e ->
                    setLoading(false)
                    toast((e as? AuthException)?.messageRes ?: R.string.error_google_failed)
                }
            }
        }

        binding.btnGuest.setOnClickListener {
            Log.d(TAG, "Continuing as guest (data stays on this phone only)")
            app.prefs.isGuest = true
            app.prefs.isLoggedIn = false
            host.onSignedIn()
        }
    }

    private fun setLoading(loading: Boolean) {
        if (_binding == null) return
        binding.progress.show(loading)
        binding.btnGoogle.isEnabled = !loading
        binding.btnLogin.isEnabled = !loading
        binding.btnGetStarted.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "WelcomeFragment"
    }
}
