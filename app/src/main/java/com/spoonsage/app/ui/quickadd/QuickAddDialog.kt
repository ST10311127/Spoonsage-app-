package com.spoonsage.app.ui.quickadd

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spoonsage.app.viewmodel.GroceryViewModel
import com.spoonsage.app.viewmodel.ProgressViewModel

private enum class QuickAddMode { MENU, LOG_PROGRESS, ADD_GROCERY }

/**
 * The action behind the bottom bar's central "+" button (Section 3.2.2):
 * quick-log today's nutrition, or add a grocery item, in two taps.
 */
@Composable
fun QuickAddDialog(
    progressViewModel: ProgressViewModel,
    groceryViewModel: GroceryViewModel,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf(QuickAddMode.MENU) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Add") },
        text = {
            when (mode) {
                QuickAddMode.MENU -> QuickAddMenu(
                    onLogProgress = { mode = QuickAddMode.LOG_PROGRESS },
                    onAddGrocery = { mode = QuickAddMode.ADD_GROCERY }
                )
                QuickAddMode.LOG_PROGRESS -> LogProgressForm(progressViewModel) { onDismiss() }
                QuickAddMode.ADD_GROCERY -> AddGroceryForm(groceryViewModel) { onDismiss() }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun QuickAddMenu(onLogProgress: () -> Unit, onAddGrocery: () -> Unit) {
    Column {
        Button(onClick = onLogProgress, modifier = Modifier.fillMaxWidth()) { Text("Log today's progress") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onAddGrocery, modifier = Modifier.fillMaxWidth()) { Text("Add a grocery item") }
    }
}

@Composable
private fun LogProgressForm(viewModel: ProgressViewModel, onDone: () -> Unit) {
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var water by remember { mutableStateOf("") }

    Column {
        Text("Add to today's totals:", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            calories, { calories = it.filter { c -> c.isDigit() } }, label = { Text("Calories (kcal)") }, singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            protein, { protein = it.filter { c -> c.isDigit() } }, label = { Text("Protein (g)") }, singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            water, { water = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Water (L)") }, singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                viewModel.logToday(calories.toIntOrNull() ?: 0, protein.toIntOrNull() ?: 0, water.toFloatOrNull() ?: 0f)
                onDone()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Log it") }
    }
}

@Composable
private fun AddGroceryForm(viewModel: GroceryViewModel, onDone: () -> Unit) {
    var item by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(item, { item = it }, label = { Text("Item name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                if (item.isNotBlank()) viewModel.addManualItem(item)
                onDone()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add to grocery list") }
    }
}
