package com.spoonsage.app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spoonsage.app.data.MealPlanEntry
import com.spoonsage.app.viewmodel.HomeDashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: HomeDashboardViewModel,
    onOpenMealPlan: () -> Unit,
    onOpenRecipes: () -> Unit,
    onOpenGrocery: () -> Unit,
    onOpenAiCoach: () -> Unit,
    onRecipeClick: (Int) -> Unit
) {
    val personalInfo by viewModel.personalInfo.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val progress by viewModel.todayProgress.collectAsState()
    val todayMeals by viewModel.todayMeals.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val name = personalInfo.displayName.ifBlank { "there" }
        Text("Hi, $name \uD83D\uDC4B", style = MaterialTheme.typography.headlineMedium)
        Text("Let's make today healthy!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)

        if (viewModel.isGuest) {
            Spacer(Modifier.height(8.dp))
            Text(
                "You're browsing as a guest - sign up to save plans and track progress.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(20.dp))
        Text("Today's Summary", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryCard("Calories", "${progress?.caloriesConsumed ?: 0}", "of ${goals.dailyCalorieTarget} kcal", Modifier.weight(1f))
            SummaryCard("Protein", "${progress?.proteinConsumedG ?: 0}g", "of ${goals.dailyProteinTargetG}g", Modifier.weight(1f))
            SummaryCard("Water", "${progress?.waterConsumedL ?: 0f}L", "of ${goals.dailyWaterTargetL}L", Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("AI Recommendation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Based on your goal (\"${goals.goalType}\"), ask the AI Coach for a plan that fits.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(10.dp))
                Button(onClick = onOpenAiCoach) { Text("Ask AI Coach") }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Quick Actions", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickAction("Meal Plan", Icons.Default.CalendarMonth, Modifier.weight(1f), onOpenMealPlan)
            QuickAction("Recipes", Icons.Default.Restaurant, Modifier.weight(1f), onOpenRecipes)
            QuickAction("Grocery List", Icons.Default.ShoppingCart, Modifier.weight(1f), onOpenGrocery)
            QuickAction("AI Coach", Icons.Default.Chat, Modifier.weight(1f), onOpenAiCoach)
        }

        Spacer(Modifier.height(20.dp))
        Text("Today's Meals", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        if (todayMeals.isEmpty()) {
            Text(
                "Nothing planned for today yet. Add a meal from Recipes or the Meal Plan.",
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            todayMeals.forEach { entry ->
                TodayMealRow(entry, onClick = { onRecipeClick(entry.recipeId) })
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SummaryCard(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(14.dp)) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun TodayMealRow(entry: MealPlanEntry, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = entry.recipeImageUrl,
                contentDescription = entry.recipeTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp))
            )
            Spacer(Modifier.width(12.dp))
            Text(entry.recipeTitle, style = MaterialTheme.typography.titleMedium)
        }
    }
}
