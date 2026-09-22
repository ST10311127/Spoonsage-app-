package com.spoonsage.app.ui.recipedetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spoonsage.app.viewmodel.DAYS_OF_WEEK
import com.spoonsage.app.viewmodel.RecipeDetailViewModel

@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    viewModel: RecipeDetailViewModel,
    onBack: () -> Unit,
    onStartCooking: (Int) -> Unit
) {
    var showDayPicker by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) { viewModel.load(recipeId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.recipe?.title ?: "Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.recipe != null) {
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                if (viewModel.recipe?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite"
                            )
                        }
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
                viewModel.errorMessage != null -> {
                    Text(
                        viewModel.errorMessage ?: "",
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                viewModel.recipe != null -> {
                    val recipe = viewModel.recipe!!
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        AsyncImage(
                            model = recipe.imageUrl,
                            contentDescription = recipe.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("${recipe.readyInMinutes} min · Serves ${recipe.servings} · ${recipe.difficulty}" + if (recipe.calories > 0) " · ${recipe.calories} kcal" else "", style = MaterialTheme.typography.bodyMedium)

                        Spacer(Modifier.height(20.dp))
                        Text("Ingredients", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        recipe.ingredients.forEach { ingredient ->
                            Text("•  $ingredient", modifier = Modifier.padding(vertical = 2.dp))
                        }

                        Spacer(Modifier.height(24.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { showDayPicker = true },
                                modifier = Modifier.weight(1f)
                            ) { Text("Add to plan") }
                            Spacer(Modifier.width(12.dp))
                            OutlinedButton(
                                onClick = { onStartCooking(recipe.id) },
                                modifier = Modifier.weight(1f)
                            ) { Text("Start cooking") }
                        }

                        viewModel.addedToPlanMessage?.let { message ->
                            Spacer(Modifier.height(12.dp))
                            Text(message, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showDayPicker) {
        AlertDialog(
            onDismissRequest = { showDayPicker = false },
            title = { Text("Add to which day?") },
            text = {
                Column {
                    DAYS_OF_WEEK.forEach { day ->
                        TextButton(
                            onClick = {
                                viewModel.addToMealPlan(day)
                                showDayPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(day) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDayPicker = false }) { Text("Cancel") }
            }
        )
    }
}
