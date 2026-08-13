package app.tabit.tracker.feature.today

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RemoveCircleOutline
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onHabitClick: (Long) -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Today", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.habits.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No habits yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add habits from the Table tab",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            // Build maps for O(1) lookups
            val recordMap = remember(state.todayRecords) {
                state.todayRecords.associateBy { it.habitId }
            }
            val doneHabitIds = remember(state.todayRecords) {
                state.todayRecords.filter { it.status == "done" }.map { it.habitId }.toSet()
            }
            val skippedHabitIds = remember(state.todayRecords) {
                state.todayRecords.filter { it.status == "skip" }.map { it.habitId }.toSet()
            }

            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val completedCount = state.todayRecords.count { it.status == "done" }
                val totalCount = state.habits.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                // Progress card
                item {
                    Card(
                        Modifier.fillMaxWidth().animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Daily Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "$completedCount of $totalCount completed",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (totalCount > 0) {
                                    Text(
                                        "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Habit items
                items(state.habits, key = { it.id }) { habit ->
                    val record = recordMap[habit.id]
                    val status = record?.status ?: "none"
                    val isDone = status == "done"
                    val isSkipped = status == "skip"
                    val streak = state.streaks[habit.id] ?: 0

                    val cardColor by animateColorAsState(
                        targetValue = when {
                            isDone -> Color(habit.color).copy(alpha = 0.08f)
                            isSkipped -> Color(0xFFFFC107).copy(alpha = 0.06f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                        animationSpec = tween(300)
                    )

                    Card(
                        onClick = { onHabitClick(habit.id) },
                        Modifier.fillMaxWidth().animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 3-state checkbox
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .then(
                                        if (isDone) Modifier.background(Color(habit.color))
                                        else if (isSkipped) Modifier.background(Color(0xFFFFC107).copy(alpha = 0.3f))
                                        else Modifier
                                    )
                                    .clickable { viewModel.toggleHabit(habit.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isDone -> Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Done",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    isSkipped -> Text(
                                        "—",
                                        color = Color(0xFFFFC107),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    else -> Icon(
                                        Icons.Default.RemoveCircleOutline,
                                        contentDescription = "Not done",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    habit.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDone) MaterialTheme.colorScheme.onSurface
                                    else if (isSkipped) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (habit.note.isNotEmpty()) {
                                    Text(
                                        habit.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        maxLines = 1
                                    )
                                }
                                if (isSkipped) {
                                    Text(
                                        "Skipped",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFFC107),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            // Streak badge
                            if (streak > 0) {
                                Surface(
                                    Modifier.padding(end = 4.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Row(
                                        Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.LocalFireDepartment,
                                            contentDescription = "Streak: $streak days",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                        Text(
                                            "$streak",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
