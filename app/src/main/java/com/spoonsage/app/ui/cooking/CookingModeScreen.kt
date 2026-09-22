package com.spoonsage.app.ui.cooking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spoonsage.app.viewmodel.RecipeDetailViewModel

/**
 * Guided, step-by-step cooking screen inspired by SideChef (Section 3,
 * Section 6 point 3): one instruction at a time, with progress tracking.
 */
@Composable
fun CookingModeScreen(
    recipeId: Int,
    viewModel: RecipeDetailViewModel,
    onBack: () -> Unit,
    onFinished: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }

    LaunchedEffect(recipeId) {
        if (viewModel.recipe?.id != recipeId) viewModel.load(recipeId)
    }

    val steps = viewModel.recipe?.steps.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.recipe?.title ?: "Cooking") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                viewModel.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                steps.isEmpty() -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No step-by-step instructions were provided for this recipe.")
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        LinearProgressIndicator(
                            progress = (currentStep + 1) / steps.size.toFloat(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Step ${currentStep + 1} of ${steps.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(Modifier.height(24.dp))
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(
                                steps[currentStep],
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { if (currentStep > 0) currentStep-- },
                                enabled = currentStep > 0,
                                modifier = Modifier.weight(1f)
                            ) { Text("Previous") }
                            Spacer(Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    if (currentStep < steps.size - 1) currentStep++ else onFinished()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                if (currentStep < steps.size - 1) {
                                    Text("Next")
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Done")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
