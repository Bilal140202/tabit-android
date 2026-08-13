package app.tabit.tracker.feature.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tabit.tracker.core.theme.TabitAlpha
import app.tabit.tracker.core.theme.TabitMotion
import app.tabit.tracker.core.theme.TabitSizing
import app.tabit.tracker.core.theme.TabitSpacing
import app.tabit.tracker.ui.components.StreakBadge
import app.tabit.tracker.ui.components.TabitEmptyState

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
                    Text(
                        "Today",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            )
        },
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
                    icon = Icons.Default.CheckCircle,
                    headline = "No habits yet",
                    subtitle = "Add habits from the Table tab to start tracking"
                )
            }
        } else {
            val recordMap = remember(state.todayRecords) {
                state.todayRecords.associateBy { it.habitId }
            }

            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    horizontal = TabitSpacing.base,
                    vertical = TabitSpacing.sm
                ),
                verticalArrangement = Arrangement.spacedBy(TabitSpacing.sm)
            ) {
                val completedCount = state.habits.count { habit ->
                    val record = recordMap[habit.id]
                    val status = record?.status ?: "none"
                    if (habit.habitType == "negative") {
                        status != "done" && status != "skip"
                    } else {
                        status == "done"
                    }
                }
                val totalCount = state.habits.size
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                // Progress card — refined with hairline border
                item {
                    val progressColor = MaterialTheme.colorScheme.primary
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.6f
                            )
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            Modifier.padding(
                                horizontal = TabitSpacing.base,
                                vertical = TabitSpacing.lg
                            )
                        ) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Daily Progress",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (totalCount > 0) {
                                    Text(
                                        "${(progress * 100).toInt()}%",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = progressColor
                                    )
                                }
                            }
                            Spacer(Modifier.height(TabitSpacing.sm))
                            LinearProgressIndicator(
                                progress = { progress },
                                Modifier
                                    .fillMaxWidth()
                                    .height(TabitSizing.progressHeight),
                                color = progressColor,
                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                    alpha = 0.15f
                                ),
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(Modifier.height(TabitSpacing.xs))
                            Text(
                                "$completedCount of $totalCount completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = TabitAlpha.MUTED_TEXT
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(TabitSpacing.xs))
                }

                // Habit items — polished cards with intentional spacing
                items(state.habits, key = { it.id }) { habit ->
                    val record = recordMap[habit.id]
                    val status = record?.status ?: "none"
                    val isDone = status == "done"
                    val isSkipped = status == "skip"
                    val streak = state.streaks[habit.id] ?: 0
                    val habitColor = Color(habit.color)

                    val cardColor by animateColorAsState(
                        targetValue = when {
                            isDone -> habitColor.copy(alpha = TabitAlpha.HABIT_DONE_BG)
                            isSkipped -> MaterialTheme.colorScheme.tertiary.copy(alpha = TabitAlpha.HABIT_SKIP_BG)
                            else -> MaterialTheme.colorScheme.surface
                        },
                        animationSpec = tween(TabitMotion.ROUTINE_MS)
                    )

                    Card(
                        onClick = { onHabitClick(habit.id) },
                        Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            Modifier.padding(
                                horizontal = TabitSpacing.md,
                                vertical = TabitSpacing.md
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 3-state checkbox circle
                            Surface(
                                modifier = Modifier
                                    .size(TabitSizing.checkboxSize)
                                    .clip(CircleShape)
                                    .clickable { viewModel.toggleHabit(habit.id) },
                                shape = CircleShape,
                                color = when {
                                    isDone -> habitColor
                                    isSkipped -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                                    else -> Color.Transparent
                                }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    AnimatedVisibility(
                                        visible = isDone,
                                        enter = fadeIn(tween(TabitMotion.IMMEDIATE_MS)),
                                        exit = fadeOut(tween(TabitMotion.IMMEDIATE_MS))
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Done",
                                            tint = Color.White.copy(alpha = TabitAlpha.HABIT_CHECK_ICON),
                                            modifier = Modifier.size(TabitSizing.checkboxIconSize)
                                        )
                                    }
                                    AnimatedVisibility(
                                        visible = isSkipped,
                                        enter = fadeIn(tween(TabitMotion.IMMEDIATE_MS)),
                                        exit = fadeOut(tween(TabitMotion.IMMEDIATE_MS))
                                    ) {
                                        Text(
                                            "\u2014",
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                    }
                                    AnimatedVisibility(
                                        visible = !isDone && !isSkipped,
                                        enter = fadeIn(tween(TabitMotion.IMMEDIATE_MS)),
                                        exit = fadeOut(tween(TabitMotion.IMMEDIATE_MS))
                                    ) {
                                        Icon(
                                            Icons.Default.RemoveCircleOutline,
                                            contentDescription = "Not done",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TabitAlpha.DISABLED),
                                            modifier = Modifier.size(TabitSizing.checkboxIconSize)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.width(TabitSpacing.md))

                            // Habit info
                            Column(Modifier.weight(1f)) {
                                Text(
                                    habit.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isDone) FontWeight.SemiBold else FontWeight.Normal,
                                    color = when {
                                        isDone -> MaterialTheme.colorScheme.onSurface
                                        isSkipped -> MaterialTheme.colorScheme.onSurfaceVariant
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    textDecoration = if (isDone) TextDecoration.LineThrough else null
                                )
                                if (habit.note.isNotEmpty()) {
                                    Text(
                                        habit.note,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = TabitAlpha.SUBTLE_TEXT
                                        ),
                                        maxLines = 1
                                    )
                                }
                                AnimatedVisibility(
                                    visible = isSkipped,
                                    enter = slideInVertically { it } + fadeIn(tween(TabitMotion.IMMEDIATE_MS)),
                                    exit = fadeOut(tween(TabitMotion.IMMEDIATE_MS))
                                ) {
                                    Text(
                                        "Skipped",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Streak badge
                            StreakBadge(
                                streak = streak,
                                modifier = Modifier.padding(end = TabitSpacing.xs)
                            )
                        }
                    }
                }

                // Bottom spacing for nav bar
                item {
                    Spacer(Modifier.height(TabitSpacing.xl))
                }
            }
        }
    }
}