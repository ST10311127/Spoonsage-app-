package com.spoonsage.app.ui.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.spoonsage.app.viewmodel.AuthViewModel

private enum class WelcomeMode { INTRO, LOGIN, SIGN_UP }

/**
 * First screen for a new device (Section 4, screen 2 / Figure 2):
 * routes to Get Started -> Sign Up, Login, or Continue as guest.
 */
@Composable
fun WelcomeScreen(
    viewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
    onSignedUp: () -> Unit,
    onGuest: () -> Unit
) {
    var mode by remember { mutableStateOf(WelcomeMode.INTRO) }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        when (mode) {
            WelcomeMode.INTRO -> IntroContent(
                onGetStarted = { mode = WelcomeMode.SIGN_UP },
                onLogin = { mode = WelcomeMode.LOGIN },
                onSignUp = { mode = WelcomeMode.SIGN_UP },
                onGuest = onGuest
            )
            WelcomeMode.LOGIN -> AuthForm(
                title = "Log in",
                actionLabel = "Log in",
                viewModel = viewModel,
                onAction = { viewModel.login(onLoggedIn) },
                onBack = { mode = WelcomeMode.INTRO }
            )
            WelcomeMode.SIGN_UP -> AuthForm(
                title = "Create your account",
                actionLabel = "Sign up",
                viewModel = viewModel,
                onAction = { viewModel.signUp(onSignedUp) },
                onBack = { mode = WelcomeMode.INTRO }
            )
        }
    }
}

@Composable
private fun IntroContent(
    onGetStarted: () -> Unit,
    onLogin: () -> Unit,
    onSignUp: () -> Unit,
    onGuest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Text("Welcome to", style = MaterialTheme.typography.titleMedium)
        Text("SpoonSage", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text(
            "Your smart companion for healthy meal planning, personalised nutrition and better lifestyle choices.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onGetStarted, modifier = Modifier.fillMaxWidth()) { Text("Get Started") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Login") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onSignUp, modifier = Modifier.fillMaxWidth()) { Text("Sign Up") }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onGuest, modifier = Modifier.fillMaxWidth()) { Text("Continue as guest") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AuthForm(
    title: String,
    actionLabel: String,
    viewModel: AuthViewModel,
    onAction: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = viewModel.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        viewModel.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onAction,
            enabled = !viewModel.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(actionLabel)
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
