package com.spoonsage.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spoonsage.app.data.Goals
import com.spoonsage.app.data.PersonalInfo
import com.spoonsage.app.data.Reminder
import com.spoonsage.app.viewmodel.*

private enum class ProfileDialog { NONE, PERSONAL, DIETARY, GOALS, REMINDERS }

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onLoggedOut: () -> Unit) {
    val email by viewModel.email.collectAsState(initial = "")
    val personalInfo by viewModel.personalInfo.collectAsState(initial = PersonalInfo())
    val goals by viewModel.goals.collectAsState(initial = Goals())
    val diet by viewModel.diet.collectAsState(initial = "Classic")
    val dislikes by viewModel.dislikes.collectAsState(initial = "")
    val isGuest by viewModel.isGuest.collectAsState(initial = false)
    var dialog by remember { mutableStateOf(ProfileDialog.NONE) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(personalInfo.displayName.ifBlank { "SpoonSage user" }, style = MaterialTheme.typography.titleLarge)
                Text(if (isGuest) "Guest mode" else email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }
        Spacer(Modifier.height(16.dp))

        ProfileRow("Personal & Health Information") { dialog = ProfileDialog.PERSONAL }
        ProfileRow("Dietary Preferences") { dialog = ProfileDialog.DIETARY }
        ProfileRow("Goals") { dialog = ProfileDialog.GOALS }
        ProfileRow("Reminders") { dialog = ProfileDialog.REMINDERS }

        Spacer(Modifier.height(24.dp))
        OutlinedButton(
            onClick = { viewModel.logOut(onLoggedOut) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (isGuest) "Exit guest mode" else "Log Out")
        }
    }

    when (dialog) {
        ProfileDialog.PERSONAL -> PersonalInfoDialog(personalInfo, onDismiss = { dialog = ProfileDialog.NONE }) {
            viewModel.savePersonalInfo(it); dialog = ProfileDialog.NONE
        }
        ProfileDialog.DIETARY -> DietaryDialog(diet, dislikes, onDismiss = { dialog = ProfileDialog.NONE }) { d, dl ->
            viewModel.saveDietaryPreferences(d, dl); dialog = ProfileDialog.NONE
        }
        ProfileDialog.GOALS -> GoalsDialog(goals, onDismiss = { dialog = ProfileDialog.NONE }) {
            viewModel.saveGoals(it); dialog = ProfileDialog.NONE
        }
        ProfileDialog.REMINDERS -> RemindersDialog(viewModel, onDismiss = { dialog = ProfileDialog.NONE })
        ProfileDialog.NONE -> {}
    }
}

@Composable
private fun ProfileRow(label: String, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
private fun PersonalInfoDialog(current: PersonalInfo, onDismiss: () -> Unit, onSave: (PersonalInfo) -> Unit) {
    var name by remember { mutableStateOf(current.displayName) }
    var age by remember { mutableStateOf(if (current.age > 0) current.age.toString() else "") }
    var gender by remember { mutableStateOf(current.gender) }
    var height by remember { mutableStateOf(if (current.heightCm > 0) current.heightCm.toString() else "") }
    var weight by remember { mutableStateOf(if (current.weightKg > 0) current.weightKg.toString() else "") }
    var activity by remember { mutableStateOf(current.activityLevel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personal & Health Info") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    age, { age = it.filter { c -> c.isDigit() } }, label = { Text("Age") }, singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    height, { height = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Height (cm)") }, singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    weight, { weight = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Weight (kg)") }, singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    PersonalInfo(
                        displayName = name, age = age.toIntOrNull() ?: 0, gender = gender,
                        heightCm = height.toFloatOrNull() ?: 0f, weightKg = weight.toFloatOrNull() ?: 0f,
                        activityLevel = activity
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DietaryDialog(currentDiet: String, currentDislikes: String, onDismiss: () -> Unit, onSave: (String, List<String>) -> Unit) {
    var diet by remember { mutableStateOf(currentDiet) }
    var dislikes by remember { mutableStateOf(currentDislikes.split(",").filter { it.isNotBlank() }.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dietary Preferences") },
        text = {
            Column {
                Text("Diet: $diet", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(120.dp)) {
                    items(DIET_OPTIONS) { option ->
                        FilterChip(
                            selected = option == diet,
                            onClick = { diet = option },
                            label = { Text(option) },
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Avoid:", style = MaterialTheme.typography.titleMedium)
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.height(180.dp)) {
                    items(DISLIKE_OPTIONS) { option ->
                        FilterChip(
                            selected = dislikes.contains(option),
                            onClick = { dislikes = if (dislikes.contains(option)) dislikes - option else dislikes + option },
                            label = { Text(option) },
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(diet, dislikes.toList()) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun GoalsDialog(current: Goals, onDismiss: () -> Unit, onSave: (Goals) -> Unit) {
    var goalType by remember { mutableStateOf(current.goalType) }
    var targetWeight by remember { mutableStateOf(if (current.targetWeightKg > 0) current.targetWeightKg.toString() else "") }
    var calorieTarget by remember { mutableStateOf(current.dailyCalorieTarget.toString()) }
    var proteinTarget by remember { mutableStateOf(current.dailyProteinTargetG.toString()) }
    var waterTarget by remember { mutableStateOf(current.dailyWaterTargetL.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Goals") },
        text = {
            Column {
                GOAL_TYPES.forEach { option ->
                    FilterChip(selected = goalType == option, onClick = { goalType = option }, label = { Text(option) }, modifier = Modifier.padding(2.dp))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(targetWeight, { targetWeight = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Target weight (kg)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(calorieTarget, { calorieTarget = it.filter { c -> c.isDigit() } }, label = { Text("Daily kcal target") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(proteinTarget, { proteinTarget = it.filter { c -> c.isDigit() } }, label = { Text("Daily protein target (g)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(waterTarget, { waterTarget = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Daily water target (L)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    Goals(
                        goalType = goalType,
                        targetWeightKg = targetWeight.toFloatOrNull() ?: 0f,
                        dailyCalorieTarget = calorieTarget.toIntOrNull() ?: 2200,
                        dailyProteinTargetG = proteinTarget.toIntOrNull() ?: 100,
                        dailyWaterTargetL = waterTarget.toFloatOrNull() ?: 2.5f
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RemindersDialog(viewModel: ProfileViewModel, onDismiss: () -> Unit) {
    val reminders by viewModel.reminders.collectAsState(initial = emptyList())
    var newType by remember { mutableStateOf("meal") }
    var newTime by remember { mutableStateOf("08:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reminders") },
        text = {
            Column {
                reminders.forEach { reminder ->
                    ReminderRow(reminder, onToggle = { viewModel.toggleReminder(reminder) }, onDelete = { viewModel.deleteReminder(reminder) })
                }
                Spacer(Modifier.height(12.dp))
                Text("Add a reminder", style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("meal", "water", "weigh-in").forEach { type ->
                        FilterChip(selected = newType == type, onClick = { newType = type }, label = { Text(type) }, modifier = Modifier.padding(2.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(newTime, { newTime = it }, label = { Text("Time (HH:mm)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.addReminder(newType, newTime) }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}

@Composable
private fun ReminderRow(reminder: Reminder, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text("${reminder.type} @ ${reminder.time}", modifier = Modifier.weight(1f))
        Switch(checked = reminder.isEnabled, onCheckedChange = { onToggle() })
        TextButton(onClick = onDelete) { Text("Remove") }
    }
}
