package com.spoonsage.app.ui.aicoach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spoonsage.app.data.AiCoachMessage
import com.spoonsage.app.viewmodel.AiCoachViewModel

private val QUICK_PROMPTS = listOf(
    "Suggest a high protein dinner under 600 kcal.",
    "What's a healthy quick breakfast?",
    "Give me a tip to hit my water goal."
)

@Composable
fun AiCoachScreen(
    viewModel: AiCoachViewModel,
    onBack: () -> Unit,
    onViewRecipe: (Int) -> Unit
) {
    val messages by viewModel.messages.collectAsState(initial = emptyList())
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Coach") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        bottomBar = {
            Column {
                viewModel.pendingSuggestion?.let { suggestion ->
                    SuggestionCard(
                        title = suggestion.title,
                        onView = { onViewRecipe(suggestion.id) },
                        onDismiss = { viewModel.dismissSuggestion() }
                    )
                }
                viewModel.suggestionError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.draftText,
                        onValueChange = viewModel::onDraftChange,
                        placeholder = { Text("Ask me anything...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { viewModel.send() }, enabled = !viewModel.isSending) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (messages.isEmpty()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Ask about nutrition, or tap a suggestion below to get started.",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(12.dp))
                    QUICK_PROMPTS.forEach { prompt ->
                        AssistChip(
                            onClick = { viewModel.sendQuickPrompt(prompt) },
                            label = { Text(prompt) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message) { title -> viewModel.prepareSuggestion(title) }
                }
                if (viewModel.isSending) {
                    item { TypingIndicator() }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: AiCoachMessage, onSuggestionTap: (String) -> Unit) {
    val isUser = message.sender == "user"
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    message.text,
                    modifier = Modifier.padding(12.dp),
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            message.suggestedRecipeTitle?.let { title ->
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = { onSuggestionTap(title) }) { Text("See recipe: $title") }
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    Text("AI Coach is typing…", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(start = 4.dp))
}

@Composable
private fun SuggestionCard(title: String, onView: () -> Unit, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Recipe suggestion", style = MaterialTheme.typography.labelLarge)
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row {
                Button(onClick = onView) { Text("View & add to plan") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDismiss) { Text("Dismiss") }
            }
        }
    }
}
