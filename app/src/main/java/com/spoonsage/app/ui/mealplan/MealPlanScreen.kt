package com.spoonsage.app.ui.mealplan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spoonsage.app.data.MealPlanEntry
import com.spoonsage.app.viewmodel.DAYS_OF_WEEK
import com.spoonsage.app.viewmodel.MealPlanViewModel

@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel,
    onRecipeClick: (Int) -> Unit
) {
    val entries by viewModel.weekPlan.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("This week", style = MaterialTheme.typography.headlineMedium)
            if (entries.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearWeek() }) { Text("Clear all") }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (entries.isEmpty()) {
            Text(
                "No meals planned yet. Find a recipe on Home and add it to a day.",
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                for (day in DAYS_OF_WEEK) {
                    val dayEntries = entries.filter { it.dayOfWeek == day }
                    if (dayEntries.isNotEmpty()) {
                        item {
                            Text(day, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        }
                        items(dayEntries) { entry ->
                            MealPlanRow(
                                entry = entry,
                                onClick = { onRecipeClick(entry.recipeId) },
                                onRemove = { viewModel.remove(entry) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPlanRow(entry: MealPlanEntry, onClick: () -> Unit, onRemove: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = entry.recipeImageUrl,
                contentDescription = entry.recipeTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(12.dp))
            Text(entry.recipeTitle, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove")
            }
        }
    }
}
