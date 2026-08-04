package app.tabit.tracker.feature.today

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tabit.tracker.core.db.HabitEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onHabitClick: (Long) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Today", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (state.habits.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No habits yet", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Add habits from the Table tab", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val completedCount = state.todayRecords.count { it.done }
                val totalCount = state.habits.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
                item {
                    Card(Modifier.fillMaxWidth().animateContentSize(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Daily Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { progress }, Modifier.fillMaxWidth())
                            Spacer(Modifier.height(4.dp))
                            Text("$completedCount of $totalCount completed", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                items(state.habits, key = { it.id }) { habit ->
                    val isDone = state.todayRecords.any { it.habitId == habit.id && it.done }
                    Card(onClick = { onHabitClick(habit.id) }, Modifier.fillMaxWidth().animateContentSize()) {
                        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isDone, onCheckedChange = { viewModel.toggleHabit(habit.id) }, colors = CheckboxDefaults.colors(checkedColor = Color(habit.color)))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(habit.name, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal, color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                                if (habit.note.isNotEmpty()) {
                                    Text(habit.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                }
                            }
                            if (isDone) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(habit.color))
                            }
                        }
                    }
                }
            }
        }
    }
}
