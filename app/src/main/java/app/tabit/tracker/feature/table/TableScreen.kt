package app.tabit.tracker.feature.table

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tabit.tracker.core.db.HabitEntity
import app.tabit.tracker.core.db.RecordEntity
import app.tabit.tracker.core.theme.TabitAlpha
import app.tabit.tracker.core.theme.TabitSizing
import app.tabit.tracker.core.theme.TabitSpacing
import app.tabit.tracker.ui.components.TabitEmptyState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableScreen(
    onHabitClick: (Long) -> Unit,
    onAddHabit: () -> Unit,
    viewModel: TableViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showNoteDialog by remember { mutableStateOf<Triple<Long, String, String>?>(null) }

    val currentMonth = state.currentMonth
    val monthLabel = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(TabitSpacing.xs)
                    ) {
                        IconButton(
                            onClick = { viewModel.changeMonth(currentMonth.minusMonths(1)) }
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Previous month",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            monthLabel,
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(
                            onClick = { viewModel.changeMonth(currentMonth.plusMonths(1)) }
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Next month",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = TabitAlpha.SURFACE_OVERLAY)
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddHabit,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new habit")
                Spacer(Modifier.width(TabitSpacing.sm))
                Text("New Habit")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.habits.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(TabitSpacing.xxl),
                    contentAlignment = Alignment.Center
                ) {
                    TabitEmptyState(
                        icon = Icons.Default.Add,
                        headline = "No habits yet",
                        subtitle = "Tap + to start building your routine"
                    )
                }
            } else {
                TableGrid(
                    habits = state.habits,
                    records = state.records,
                    scores = state.scores,
                    currentMonth = currentMonth,
                    onToggle = { habitId, date, done, status ->
                        viewModel.toggleRecord(habitId, date, done, status)
                    },
                    onLongPress = { habitId, date, note ->
                        showNoteDialog = Triple(habitId, date, note)
                    }
                )
            }
        }
    }
    showNoteDialog?.let { (habitId, date, note) ->
        NoteDialog(
            initialNote = note,
            onConfirm = {
                viewModel.updateRecordNote(habitId, date, it)
                showNoteDialog = null
            },
            onDismiss = { showNoteDialog = null }
        )
    }
}

private val DAY_LABELS = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
private fun TableGrid(
    habits: List<HabitEntity>,
    records: Map<Long, List<RecordEntity>>,
    scores: Map<Long, Float>,
    currentMonth: YearMonth,
    onToggle: (Long, String, Boolean, String) -> Unit,
    onLongPress: (Long, String, String) -> Unit
) {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val daysInMonth = currentMonth.lengthOfMonth()
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    val today = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(verticalScrollState)
            .padding(
                start = TabitSpacing.sm,
                end = TabitSpacing.sm,
                top = TabitSpacing.sm,
                bottom = 80.dp
            )
    ) {
        // Day-of-week header row
        Row(
            modifier = Modifier.horizontalScroll(horizontalScrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(TabitSizing.tableNameColWidth.dp))
            for (day in 1..daysInMonth) {
                val dayOfWeek = currentMonth.atDay(day).dayOfWeek
                val label = DAY_LABELS[dayOfWeek.value - 1]
                val isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
                Box(
                    modifier = Modifier.width(TabitSizing.tableCellSize.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isWeekend)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TabitAlpha.MUTED_TEXT),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Day number header row
        Row(
            modifier = Modifier.horizontalScroll(horizontalScrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(TabitSizing.tableNameColWidth.dp))
            for (day in 1..daysInMonth) {
                val isToday = currentMonth.year == today.year && currentMonth.month == today.month && day == today.dayOfMonth
                Box(
                    modifier = Modifier.width(TabitSizing.tableCellSize.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Hairline divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = TabitSpacing.xs),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = TabitAlpha.DIVIDER)
        )

        // Habit rows
        habits.forEach { habit ->
            key(habit.id) {
                val habitRecords = records[habit.id] ?: emptyList()
                val recordMap = remember(habitRecords) { habitRecords.associateBy { it.date } }
                val habitScore = scores[habit.id] ?: 0f
                val habitColor = Color(habit.color)

                Row(
                    modifier = Modifier
                        .horizontalScroll(horizontalScrollState)
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = habit.name,
                        modifier = Modifier
                            .width(TabitSizing.tableNameColWidth.dp)
                            .padding(end = TabitSpacing.xs),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    for (day in 1..daysInMonth) {
                        val date = currentMonth.atDay(day).format(formatter)
                        val record = recordMap[date]
                        val isToday = currentMonth.year == today.year && currentMonth.month == today.month && day == today.dayOfMonth
                        TableCell(
                            done = record?.status == "done" || (record?.done == true && record.status == "none"),
                            score = habitScore,
                            habitColor = habitColor,
                            isToday = isToday,
                            status = record?.status ?: "none",
                            onToggle = {
                                val currentStatus = record?.status ?: "none"
                                onToggle(habit.id, date, record?.done == true, currentStatus)
                            },
                            onLongPress = { onLongPress(habit.id, date, record?.note ?: "") },
                            modifier = Modifier.width(TabitSizing.tableCellSize.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteDialog(
    initialNote: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var note by remember(initialNote) { mutableStateOf(initialNote) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Note", style = MaterialTheme.typography.titleMedium) },
        text = {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Add a note") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(note) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}