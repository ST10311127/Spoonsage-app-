package com.spoonsage.app.ui.grocery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.spoonsage.app.data.GroceryItem
import com.spoonsage.app.viewmodel.GroceryViewModel

@Composable
fun GroceryListScreen(viewModel: GroceryViewModel) {
    val items by viewModel.groceryItems.collectAsState(initial = emptyList())
    var newItemText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Grocery list", style = MaterialTheme.typography.headlineMedium)
            if (items.any { it.isChecked }) {
                TextButton(onClick = { viewModel.clearChecked() }) { Text("Clear checked") }
            }
        }
        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newItemText,
                onValueChange = { newItemText = it },
                placeholder = { Text("Add an item") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (newItemText.isNotBlank()) {
                        viewModel.addManualItem(newItemText)
                        newItemText = ""
                    }
                })
            )
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = {
                if (newItemText.isNotBlank()) {
                    viewModel.addManualItem(newItemText)
                    newItemText = ""
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (items.isEmpty()) {
            Text(
                "Your list is empty. Add recipes to your meal plan and their ingredients will show up here automatically.",
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(items) { item ->
                    GroceryRow(
                        item = item,
                        onToggle = { viewModel.toggleChecked(item) },
                        onDelete = { viewModel.delete(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GroceryRow(item: GroceryItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Checkbox(checked = item.isChecked, onCheckedChange = { onToggle() })
        Text(
            item.name,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
            )
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}
