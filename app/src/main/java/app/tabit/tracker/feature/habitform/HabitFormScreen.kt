package app.tabit.tracker.feature.habitform

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitFormScreen(
    habitId: Long = 0,
    onSaved: () -> Unit,
    onCancelled: () -> Unit,
    viewModel: HabitFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(habitId) { if (habitId > 0) viewModel.loadHabit(habitId) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Habit" else "New Habit", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onCancelled) { Text("Cancel") } },
                actions = { TextButton(onClick = { viewModel.saveHabit(habitId); onSaved() }, enabled = state.name.isNotBlank() && !state.isSaving) { Text("Save") } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = state.name, onValueChange = { viewModel.updateName(it) }, label = { Text("Habit Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Daily Target", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.align(Alignment.CenterVertically))
                Row {
                    IconButton(onClick = { if (state.target > 1) viewModel.updateTarget(state.target - 1) }) { Text("-") }
                    Text("${state.target}", modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { viewModel.updateTarget(state.target + 1) }) { Text("+") }
                }
            }
            Text("Frequency", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("daily", "weekly", "monthly").forEach { freq ->
                    FilterChip(selected = state.frequency == freq, onClick = { viewModel.updateFrequency(freq) }, label = { Text(freq.replaceFirstChar { it.uppercase() }) })
                }
            }
            OutlinedTextField(value = state.note, onValueChange = { viewModel.updateNote(it) }, label = { Text("Note (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Color", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.align(Alignment.CenterVertically))
                    Surface(Modifier.size(32.dp), shape = MaterialTheme.shapes.small, color = Color(state.color)) {}
                }
            }
        }
    }
}
