package app.tabit.tracker.feature.habitform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val COLOR_PRESETS = listOf(
    0xFF2196F3, 0xFF4CAF50, 0xFFFF9800, 0xFFE91E63,
    0xFF9C27B0, 0xFF00BCD4, 0xFFFF5722, 0xFF607D8B,
    0xFF8BC34A, 0xFF3F51B5, 0xFFFFEB3B, 0xFF795548,
    0xFF006B5E, 0xFFF44336, 0xFF673AB7, 0xFF03A9F4
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitFormScreen(
    habitId: Long = 0,
    onSaved: () -> Unit,
    onCancelled: () -> Unit,
    viewModel: HabitFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(habitId) { if (habitId > 0) viewModel.loadHabit(habitId) }

    // Fix C3: Navigate only after save completes
    LaunchedEffect(state.saveComplete) {
        if (state.saveComplete) onSaved()
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Habit" else "New Habit", fontWeight = FontWeight.Bold) },
                navigationIcon = { TextButton(onClick = onCancelled) { Text("Cancel") } },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    TextButton(
                        onClick = { viewModel.saveHabit(habitId) },
                        enabled = state.name.isNotBlank() && !state.isSaving
                    ) { Text("Save") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Habit Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Surface(
                        Modifier.size(24.dp),
                        shape = CircleShape,
                        color = Color(state.color)
                    ) {}
                }
            )

            // Color Picker
            Text("Color", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                COLOR_PRESETS.forEach { presetColor ->
                    val isSelected = state.color == presetColor
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(presetColor))
                            .then(
                                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            )
                            .clickable { viewModel.updateColor(presetColor) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Daily Target
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daily Target", style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (state.target > 1) viewModel.updateTarget(state.target - 1) }) {
                        Icon(Icons.Default.Remove, "Decrease")
                    }
                    Text(
                        "${state.target}",
                        modifier = Modifier.width(32.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = { viewModel.updateTarget(state.target + 1) }) {
                        Icon(Icons.Default.Add, "Increase")
                    }
                }
            }

            // Weight
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Weight", style = MaterialTheme.typography.bodyLarge)
                Text("%.1f".format(state.weight), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Slider(
                value = state.weight,
                onValueChange = { viewModel.updateWeight(it) },
                valueRange = 0.1f..2.0f,
                steps = 18,
                modifier = Modifier.fillMaxWidth()
            )

            // Frequency
            Text("Frequency", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("daily", "weekly", "monthly").forEach { freq ->
                    FilterChip(
                        selected = state.frequency == freq,
                        onClick = { viewModel.updateFrequency(freq) },
                        label = { Text(freq.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Note
            OutlinedTextField(
                value = state.note,
                onValueChange = { viewModel.updateNote(it) },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
        }
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Habit?") },
            text = { Text("This will permanently delete \"${state.name}\" and all its records. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteHabit(habitId); showDeleteConfirm = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}
