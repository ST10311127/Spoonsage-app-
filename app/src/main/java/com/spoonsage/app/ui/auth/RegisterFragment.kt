package com.spoonsage.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.spoonsage.app.R
import com.spoonsage.app.data.firebase.AuthException
import com.spoonsage.app.databinding.FragmentRegisterBinding
import com.spoonsage.app.ui.common.app
import com.spoonsage.app.ui.common.check
import com.spoonsage.app.ui.common.hideKeyboard
import com.spoonsage.app.ui.common.show
import com.spoonsage.app.ui.common.text
import com.spoonsage.app.ui.common.toast
import com.spoonsage.app.util.Validators
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val host = requireActivity() as AuthActivity
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.tvGoLogin.setOnClickListener { host.navigateTo(LoginFragment()) }
        binding.btnSignUp.setOnClickListener { attemptRegister(host) }
        binding.btnGoogle.setOnClickListener {
            setLoading(true)
            viewLifecycleOwner.lifecycleScope.launch {
                app.authRepository.signInWithGoogle(requireActivity())
                    .onSuccess { user ->
                        SignInHelper.completeSignIn(this@RegisterFragment, user)
                        host.onSignedIn()
                    }
                    .onFailure { e ->
                        setLoading(false)
                        toast((e as? AuthException)?.messageRes ?: R.string.error_google_failed)
                    }
            }
        }
    }

    private fun attemptRegister(host: AuthActivity) {
        hideKeyboard()
        val name = binding.tilName.text
        val email = binding.tilEmail.text
        val password = binding.tilPassword.editText?.text.toString()
        val confirm = binding.tilConfirm.editText?.text.toString()

        val nameOk = binding.tilName.check(Validators.required(name))
        val emailOk = binding.tilEmail.check(Validators.email(email))
        val passwordOk = binding.tilPassword.check(Validators.password(password))
        val confirmOk = binding.tilConfirm.check(Validators.confirmPassword(password, confirm))
        if (!(nameOk && emailOk && passwordOk && confirmOk)) return

        if (!app.networkMonitor.isConnected()) {
            toast(R.string.error_register_offline)
            return
        }

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            app.authRepository.register(name, email, password)
                .onSuccess { user ->
                    app.prefs.displayName = name
                    SignInHelper.completeSignIn(this@RegisterFragment, user)
                    toast(R.string.account_created)
                    host.onSignedIn()
                }
                .onFailure { e ->
                    setLoading(false)
                    toast((e as? AuthException)?.messageRes ?: R.string.error_register_failed)
                }
        }
    }

    private fun setLoading(loading: Boolean) {
        if (_binding == null) return
        binding.progress.show(loading)
        binding.btnSignUp.isEnabled = !loading
        binding.btnGoogle.isEnabled = !loading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
