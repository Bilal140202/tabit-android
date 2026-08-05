package app.tabit.tracker.feature.table

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableScreen(
    onHabitClick: (Long) -> Unit,
    onAddHabit: () -> Unit,
    viewModel: TableViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = 1200, pageCount = { 2400 })
    var showNoteDialog by remember { mutableStateOf<Triple<Long, String, String>?>(null) }

    // Track the last month we told the ViewModel about, to avoid duplicate calls
    var lastNotifiedMonth by remember { mutableStateOf(YearMonth.now()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tabit",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                actions = {
                    IconButton(onClick = onAddHabit) {
                        Icon(Icons.Default.Add, "Add")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddHabit,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, null)
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
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val monthOffset = page - 1200
                    val displayMonth = YearMonth.now().plusMonths(monthOffset.toLong())
                    // Only notify ViewModel when the month actually changes
                    if (displayMonth != lastNotifiedMonth) {
                        LaunchedEffect(displayMonth) {
                            lastNotifiedMonth = displayMonth
                            viewModel.changeMonth(displayMonth)
                        }
                    }
                    TableGrid(
                        habits = state.habits,
                        records = state.records,
                        scores = state.scores,
                        currentMonth = displayMonth,
                        onToggle = { habitId, date, done ->
                            viewModel.toggleRecord(habitId, date, done)
                        },
                        onLongPress = { habitId, date, note ->
                            showNoteDialog = Triple(habitId, date, note)
                        }
                    )
                }
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

@Composable
private fun TableGrid(
    habits: List<HabitEntity>,
    records: Map<Long, List<RecordEntity>>,
    scores: Map<Long, Float>,
    currentMonth: YearMonth,
    onToggle: (Long, String, Boolean) -> Unit,
    onLongPress: (Long, String, String) -> Unit
) {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val daysInMonth = currentMonth.lengthOfMonth()
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(64.dp))
                for (day in 1..daysInMonth) {
                    Text(
                        text = day.toString(),
                        modifier = Modifier.weight(1f),
                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        items(habits.size, key = { index -> habits[index].id }) { index ->
            val habit = habits[index]
            val habitRecords = records[habit.id] ?: emptyList()
            val habitScore = scores[habit.id] ?: 0f
            Row(
                Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = habit.name,
                    modifier = Modifier.width(64.dp),
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                for (day in 1..daysInMonth) {
                    val date = currentMonth.atDay(day).format(formatter)
                    val record = habitRecords.find { it.date == date }
                    TableCell(
                        done = record?.done ?: false,
                        score = habitScore,
                        dayLabel = day.toString(),
                        habitColor = Color(habit.color),
                        onToggle = { onToggle(habit.id, date, record?.done ?: false) },
                        onLongPress = { onLongPress(habit.id, date, record?.note ?: "") },
                        modifier = Modifier.weight(1f)
                    )
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
