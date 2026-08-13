package app.tabit.tracker.feature.charts

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tabit.tracker.core.theme.TabitAlpha
import app.tabit.tracker.core.theme.TabitSizing
import app.tabit.tracker.core.theme.TabitSpacing
import app.tabit.tracker.ui.components.TabitEmptyState
import app.tabit.tracker.ui.components.StatItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(viewModel: ChartsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Insights",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.habits.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(TabitSpacing.xxl),
                contentAlignment = Alignment.Center
            ) {
                TabitEmptyState(
                    icon = Icons.Default.BarChart,
                    headline = "No habits yet",
                    subtitle = "Add habits and check back here for insights"
                )
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = TabitSpacing.base,
                    vertical = TabitSpacing.sm
                ),
                verticalArrangement = Arrangement.spacedBy(TabitSpacing.base)
            ) {
                // Overview card
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            Modifier.padding(
                                horizontal = TabitSpacing.base,
                                vertical = TabitSpacing.lg
                            )
                        ) {
                            Text(
                                "Overview",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(TabitSpacing.lg))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    "${state.habits.size}",
                                    "Habits"
                                )
                                StatItem(
                                    "${state.streaks.values.maxOrNull() ?: 0}",
                                    "Best Streak"
                                )
                                val avgText = if (state.scores.isEmpty()) "N/A"
                                else "${(state.scores.values.average() * 100).toInt()}%"
                                StatItem(
                                    avgText,
                                    "Avg Score",
                                    valueColor = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Per-habit cards
                items(state.habits, key = { it.id }) { habit ->
                    val score = state.scores[habit.id] ?: 0f
                    val streak = state.streaks[habit.id] ?: 0
                    val bestStreak = state.bestStreaks[habit.id] ?: 0
                    val habitColor = Color(habit.color)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            Modifier.padding(
                                horizontal = TabitSpacing.base,
                                vertical = TabitSpacing.lg
                            )
                        ) {
                            // Habit name row
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    Modifier.size(TabitSizing.colorDotSize),
                                    shape = CircleShape,
                                    color = habitColor
                                ) {}
                                Spacer(Modifier.width(TabitSpacing.sm))
                                Text(
                                    habit.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    "${(score * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = habitColor
                                )
                            }

                            Spacer(Modifier.height(TabitSpacing.md))

                            // Score bar
                            LinearProgressIndicator(
                                progress = { score.coerceIn(0f, 1f) },
                                Modifier
                                    .fillMaxWidth()
                                    .height(TabitSizing.progressHeight),
                                color = habitColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )

                            // Circular stats
                            Spacer(Modifier.height(TabitSpacing.lg))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                CircularStat(
                                    value = streak,
                                    label = "Current",
                                    maxValue = bestStreak.coerceAtLeast(1),
                                    color = habitColor
                                )
                                CircularStat(
                                    value = bestStreak,
                                    label = "Best",
                                    maxValue = bestStreak.coerceAtLeast(1),
                                    color = habitColor.copy(alpha = 0.6f)
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

                // Bottom spacing
                item {
                    Spacer(Modifier.height(TabitSpacing.xl))
                }
            }
        }
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
            Canvas(modifier = Modifier.size(TabitSizing.circularStatSize)) {
                val strokeWidth = TabitSizing.circularStatStroke.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val center = Offset(size.width / 2, size.height / 2)
                // Background track
                drawCircle(
                    color = color.copy(alpha = 0.15f),
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
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}