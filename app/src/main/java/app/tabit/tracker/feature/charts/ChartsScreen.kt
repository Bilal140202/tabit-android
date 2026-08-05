package app.tabit.tracker.feature.charts

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tabit.tracker.core.db.HabitEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(viewModel: ChartsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Charts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.habits.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No habits yet", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Add habits and check back here", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Overview card
                item {
                    Card(
                        Modifier.fillMaxWidth().animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Overview", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                StatItem("${state.habits.size}", "Habits")
                                StatItem("${state.streaks.values.maxOrNull() ?: 0}", "Best Streak")
                                // Fix M4: Handle empty scores
                                val avgText = if (state.scores.isEmpty()) "N/A"
                                    else "${(state.scores.values.average() * 100).toInt()}%"
                                StatItem(avgText, "Avg Score")
                            }
                        }
                    }
                }

                // Per-habit cards
                items(state.habits, key = { it.id }) { habit ->
                    val score = state.scores[habit.id] ?: 0f
                    val streak = state.streaks[habit.id] ?: 0
                    val bestStreak = state.bestStreaks[habit.id] ?: 0
                    Card(
                        modifier = Modifier.fillMaxWidth().animateContentSize(),
                        onClick = { viewModel.selectHabit(habit.id) }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    Modifier.size(12.dp),
                                    shape = CircleShape,
                                    color = Color(habit.color)
                                ) {}
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    habit.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(12.dp))

                            // Mini score bar chart
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Score", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text(
                                    "${(score * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { score },
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                color = Color(habit.color),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            // Mini circular progress for visual appeal
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                CircularStat(
                                    value = streak,
                                    label = "Current",
                                    maxValue = bestStreak.coerceAtLeast(1),
                                    color = Color(habit.color)
                                )
                                CircularStat(
                                    value = bestStreak,
                                    label = "Best",
                                    maxValue = bestStreak.coerceAtLeast(1),
                                    color = Color(habit.color).copy(alpha = 0.6f)
                                )
                                CircularStat(
                                    value = (score * 100).toInt(),
                                    label = "Score",
                                    maxValue = 100,
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

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CircularStat(
    value: Int,
    label: String,
    maxValue: Int,
    color: Color
) {
    val fraction = if (maxValue > 0) (value.toFloat() / maxValue).coerceIn(0f, 1f) else 0f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(52.dp)) {
                val strokeWidth = 6.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)
                // Background circle
                drawCircle(
                    color = color.copy(alpha = 0.2f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )
                // Progress arc
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                "$value",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
