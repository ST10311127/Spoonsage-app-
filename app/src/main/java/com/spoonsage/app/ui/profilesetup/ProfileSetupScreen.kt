package com.spoonsage.app.ui.profilesetup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spoonsage.app.viewmodel.*

private const val STEP_COUNT = 4

@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel,
    onFinished: () -> Unit
) {
    var step by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            LinearProgressIndicator(
                progress = (step + 1) / STEP_COUNT.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                if (step > 0) {
                    OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f)) { Text("Back") }
                    Spacer(Modifier.width(12.dp))
                }
                Button(
                    onClick = {
                        if (step < STEP_COUNT - 1) step++ else viewModel.finish(onFinished)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (step < STEP_COUNT - 1) "Continue" else "Finish setup")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 24.dp).fillMaxSize()) {
            Spacer(Modifier.height(16.dp))
            when (step) {
                0 -> PersonalInfoStep(viewModel)
                1 -> HealthInfoStep(viewModel)
                2 -> DietaryStep(viewModel)
                3 -> GoalsStep(viewModel)
            }
        }
    }
}

@Composable
private fun PersonalInfoStep(vm: ProfileSetupViewModel) {
    Text("Tell us about yourself", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = vm.displayName, onValueChange = vm::onDisplayNameChange,
        label = { Text("What should we call you?") }, singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = vm.age, onValueChange = vm::onAgeChange,
        label = { Text("Age") }, singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(16.dp))
    Text("Gender", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    ChoiceRow(GENDER_OPTIONS, vm.gender, vm::onGenderChange)
}

@Composable
private fun HealthInfoStep(vm: ProfileSetupViewModel) {
    Text("A little about your health", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = vm.heightCm, onValueChange = vm::onHeightChange,
        label = { Text("Height (cm)") }, singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = vm.weightKg, onValueChange = vm::onWeightChange,
        label = { Text("Weight (kg)") }, singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(16.dp))
    Text("Activity level", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    ChoiceRow(ACTIVITY_LEVELS, vm.activityLevel, vm::onActivityChange)
}

@Composable
private fun DietaryStep(vm: ProfileSetupViewModel) {
    Text("Dietary preferences", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(8.dp))
    Text("Pick your diet and anything you'd like recipes to avoid.", style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(16.dp))
    Text("Diet", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    LazyColumn(modifier = Modifier.height(160.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(DIET_OPTIONS) { diet ->
            val selected = diet == vm.selectedDiet
            OutlinedButton(
                onClick = { vm.selectDiet(diet) },
                modifier = Modifier.fillMaxWidth(),
                colors = if (selected) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                else ButtonDefaults.outlinedButtonColors()
            ) { Text(diet, modifier = Modifier.fillMaxWidth()) }
        }
    }
    Spacer(Modifier.height(16.dp))
    Text("Avoid these ingredients", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.height(220.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(DISLIKE_OPTIONS) { item ->
            val selected = vm.selectedDislikes.contains(item)
            FilterChip(selected = selected, onClick = { vm.toggleDislike(item) }, label = { Text(item) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun GoalsStep(vm: ProfileSetupViewModel) {
    Text("What's your goal?", style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(16.dp))
    ChoiceRow(GOAL_TYPES, vm.goalType, vm::onGoalTypeChange)
    Spacer(Modifier.height(20.dp))
    OutlinedTextField(
        value = vm.targetWeightKg, onValueChange = vm::onTargetWeightChange,
        label = { Text("Target weight (kg, optional)") }, singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = vm.dailyCalorieTarget, onValueChange = vm::onCalorieTargetChange,
            label = { Text("Kcal/day") }, singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = vm.dailyProteinTargetG, onValueChange = vm::onProteinTargetChange,
            label = { Text("Protein g/day") }, singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(12.dp))
    OutlinedTextField(
        value = vm.dailyWaterTargetL, onValueChange = vm::onWaterTargetChange,
        label = { Text("Water L/day") }, singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ChoiceRow(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { option ->
            val isSelected = option == selected
            OutlinedButton(
                onClick = { onSelect(option) },
                modifier = Modifier.fillMaxWidth(),
                colors = if (isSelected) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                else ButtonDefaults.outlinedButtonColors()
            ) { Text(option, modifier = Modifier.fillMaxWidth()) }
        }
    }
}
