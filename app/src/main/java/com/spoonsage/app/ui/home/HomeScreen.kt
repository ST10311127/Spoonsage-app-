package com.spoonsage.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spoonsage.app.data.ComplexSearchResult
import com.spoonsage.app.viewmodel.HomeViewModel
import com.spoonsage.app.viewmodel.SearchMode

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onRecipeClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Find something to cook", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        SingleChoiceSegmented(viewModel)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.queryText,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    if (viewModel.searchMode == SearchMode.BY_INGREDIENTS)
                        "e.g. chicken, rice, spinach"
                    else "e.g. pasta, tacos, curry"
                )
            },
            trailingIcon = {
                IconButton(onClick = { viewModel.search() }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        when {
            viewModel.isLoading -> {
                Box(Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            viewModel.errorMessage != null -> {
                Text(
                    viewModel.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error
                )
            }
            viewModel.results.isEmpty() -> {
                Text(
                    "Search by a dish name, or switch to \"By ingredients\" to use up what's already in your kitchen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(viewModel.results) { recipe ->
                        RecipeResultCard(recipe, onClick = { onRecipeClick(recipe.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleChoiceSegmented(viewModel: HomeViewModel) {
    Row(modifier = Modifier.fillMaxWidth()) {
        SegmentButton(
            text = "By name",
            selected = viewModel.searchMode == SearchMode.BY_NAME,
            modifier = Modifier.weight(1f)
        ) { viewModel.setSearchMode(SearchMode.BY_NAME) }
        Spacer(Modifier.width(8.dp))
        SegmentButton(
            text = "By ingredients",
            selected = viewModel.searchMode == SearchMode.BY_INGREDIENTS,
            modifier = Modifier.weight(1f)
        ) { viewModel.setSearchMode(SearchMode.BY_INGREDIENTS) }
    }
}

@Composable
private fun SegmentButton(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) { Text(text) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(text) }
    }
}

@Composable
private fun RecipeResultCard(recipe: ComplexSearchResult, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = recipe.image,
                contentDescription = recipe.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(recipe.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                if (recipe.readyInMinutes != null || recipe.calories != null) {
                    Spacer(Modifier.height(4.dp))
                    val parts = buildList {
                        recipe.readyInMinutes?.let { add("$it min") }
                        add(recipe.difficulty)
                        recipe.calories?.let { add("$it kcal") }
                    }
                    Text(
                        parts.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
