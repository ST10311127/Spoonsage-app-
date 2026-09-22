package com.spoonsage.app.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spoonsage.app.data.ProgressLog
import com.spoonsage.app.viewmodel.ProgressViewModel

private val TABS = listOf("Overview", "Nutrition", "Activity", "Goals")

@Composable
fun ProgressScreen(viewModel: ProgressViewModel) {
    val logs by viewModel.recentLogs.collectAsState(initial = emptyList())
    val today by viewModel.todayProgress.collectAsState(initial = null)
    val goals by viewModel.goals.collectAsState(initial = com.spoonsage.app.data.Goals())
    var tabIndex by remember { mutableStateOf(0) }
    val averages = remember(logs) { viewModel.weeklyAverages(logs) }
    val streak = today?.streakCount ?: (logs.firstOrNull()?.streakCount ?: 0)
    val badges = remember(streak) { viewModel.badgesFor(streak) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Progress", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        TabRow(selectedTabIndex = tabIndex) {
            TABS.forEachIndexed { index, label ->
                Tab(selected = tabIndex == index, onClick = { tabIndex = index }, text = { Text(label) })
            }
        }
        Spacer(Modifier.height(16.dp))

        when (tabIndex) {
            0 -> OverviewTab(averages, streak, badges)
            1 -> NutritionTab(logs, averages)
            2 -> ActivityTab(streak)
            3 -> GoalsTab(goals, today)
        }
    }
}

@Composable
private fun OverviewTab(averages: com.spoonsage.app.viewmodel.WeeklyAverages, streak: Int, badges: List<String>) {
    Text("This week", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        StatCard("Calories", "Avg ${averages.avgCalories} kcal", Modifier.weight(1f))
        StatCard("Protein", "Avg ${averages.avgProtein} g", Modifier.weight(1f))
        StatCard("Water", "Avg %.1fL".format(averages.avgWater), Modifier.weight(1f))
    }
    Spacer(Modifier.height(20.dp))
    Text("Streak", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(8.dp))
    Text("🔥 $streak day${if (streak == 1) "" else "s"} in a row", style = MaterialTheme.typography.bodyLarge)
    if (badges.isNotEmpty()) {
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            badges.forEach { badge -> AssistChip(onClick = {}, label = { Text("🏅 $badge") }) }
        }
    }
}

@Composable
private fun NutritionTab(logs: List<ProgressLog>, averages: com.spoonsage.app.viewmodel.WeeklyAverages) {
    Text("Weekly nutrition", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp))
    if (logs.isEmpty()) {
        Text("No progress logged yet. Use the + button to log today's meals and water.", color = MaterialTheme.colorScheme.secondary)
    } else {
        BarChart(values = logs.reversed().map { it.caloriesConsumed.toFloat() }, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Text("Calories per day (last ${logs.size})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(16.dp))
        Text("Averages: ${averages.avgCalories} kcal · ${averages.avgProtein} g protein · %.1fL water".format(averages.avgWater))
    }
}

@Composable
private fun ActivityTab(streak: Int) {
    Text("Activity", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp))
    Text(
        "SpoonSage currently tracks healthy-eating activity through your daily log streak: $streak day(s).",
        color = MaterialTheme.colorScheme.secondary
    )
}

@Composable
private fun GoalsTab(goals: com.spoonsage.app.data.Goals, today: ProgressLog?) {
    Text("Your goals", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp))
    GoalProgressRow("Calories", today?.caloriesConsumed ?: 0, goals.dailyCalorieTarget)
    Spacer(Modifier.height(10.dp))
    GoalProgressRow("Protein (g)", today?.proteinConsumedG ?: 0, goals.dailyProteinTargetG)
    Spacer(Modifier.height(10.dp))
    GoalProgressRowFloat("Water (L)", today?.waterConsumedL ?: 0f, goals.dailyWaterTargetL)
    if (goals.targetWeightKg > 0f) {
        Spacer(Modifier.height(16.dp))
        Text("Goal type: ${goals.goalType}", style = MaterialTheme.typography.bodyMedium)
        Text("Target weight: ${goals.targetWeightKg} kg", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun GoalProgressRow(label: String, value: Int, target: Int) {
    val progress = if (target > 0) (value / target.toFloat()).coerceIn(0f, 1f) else 0f
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
        Text("$value of $target", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun GoalProgressRowFloat(label: String, value: Float, target: Float) {
    val progress = if (target > 0f) (value / target).coerceIn(0f, 1f) else 0f
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
        Text("%.1f of %.1f".format(value, target), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleMedium)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

/** A minimal hand-drawn bar chart - no external charting library needed for this simple version. */
@Composable
private fun BarChart(values: List<Float>, color: Color, modifier: Modifier = Modifier) {
    val maxValue = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    Canvas(modifier = modifier.fillMaxWidth().height(140.dp)) {
        if (values.isEmpty()) return@Canvas
        val barWidth = size.width / (values.size * 1.5f)
        val gap = barWidth * 0.5f
        values.forEachIndexed { index, value ->
            val barHeight = (value / maxValue) * size.height
            val x = index * (barWidth + gap)
            drawRect(
                color = color,
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
}
