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
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.changeMonth(currentMonth.minusMonths(1)) }
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Previous month",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            monthLabel,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { viewModel.changeMonth(currentMonth.plusMonths(1)) }
                        ) {
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Next month",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddHabit,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new habit")
                Spacer(Modifier.width(8.dp))
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No habits yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap + to start tracking",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
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
private const val NAME_COL_WIDTH = 84
private const val CELL_SIZE = 38

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
            .padding(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 80.dp)
    ) {
        // Day-of-week header row
        Row(
            modifier = Modifier.horizontalScroll(horizontalScrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(NAME_COL_WIDTH.dp))
            for (day in 1..daysInMonth) {
                val dayOfWeek = currentMonth.atDay(day).dayOfWeek
                val label = DAY_LABELS[dayOfWeek.value - 1]
                val isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
                Box(
                    modifier = Modifier.width(CELL_SIZE.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isWeekend)
                            MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium,
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
            Spacer(Modifier.width(NAME_COL_WIDTH.dp))
            for (day in 1..daysInMonth) {
                val isToday = currentMonth.year == today.year && currentMonth.month == today.month && day == today.dayOfMonth
                Box(
                    modifier = Modifier.width(CELL_SIZE.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold
                        ),
                        color = if (isToday)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Horizontal divider
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
                        modifier = Modifier.width(NAME_COL_WIDTH.dp).padding(end = 4.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
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
                            modifier = Modifier.width(CELL_SIZE.dp)
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
        title = { Text("Note") },
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
