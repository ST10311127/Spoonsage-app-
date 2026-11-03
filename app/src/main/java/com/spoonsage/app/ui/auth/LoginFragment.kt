package com.spoonsage.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.spoonsage.app.R
import com.spoonsage.app.data.firebase.AuthException
import com.spoonsage.app.databinding.FragmentLoginBinding
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.check
import com.spoonsage.app.ui.common.hideKeyboard
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.text
import com.spoonsage.app.ui.common.toast
import com.spoonsage.app.util.Validators
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val host = requireActivity() as AuthActivity
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnLogin.setOnClickListener { attemptLogin(host) }
        binding.tvGoRegister.setOnClickListener { host.navigateTo(RegisterFragment()) }
        binding.btnGoogle.setOnClickListener { googleSignIn(host) }
    }

    private fun attemptLogin(host: AuthActivity) {
        hideKeyboard()
        val emailOk = binding.tilEmail.check(Validators.email(binding.tilEmail.text))
        val passwordOk = binding.tilPassword.check(Validators.required(binding.tilPassword.text))
        if (!emailOk || !passwordOk) return

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            app.authRepository.login(binding.tilEmail.text, binding.tilPassword.editText?.text.toString())
                .onSuccess { user ->
                    if (user.offline) toast(R.string.offline_login_notice)
                    SignInHelper.completeSignIn(this@LoginFragment, user)
                    host.onSignedIn()
                }
                .onFailure { e ->
                    setLoading(false)
                    toast((e as? AuthException)?.messageRes ?: R.string.error_login_failed)
                }
        }
    }

    private fun googleSignIn(host: AuthActivity) {
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            app.authRepository.signInWithGoogle(requireActivity())
                .onSuccess { user ->
                    SignInHelper.completeSignIn(this@LoginFragment, user)
                    host.onSignedIn()
                }
                .onFailure { e ->
                    setLoading(false)
                    toast((e as? AuthException)?.messageRes ?: R.string.error_google_failed)
                }
        }
    }

    private fun setLoading(loading: Boolean) {
        if (_binding == null) return
        binding.progress.show(loading)
        binding.btnLogin.isEnabled = !loading
        binding.btnGoogle.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
