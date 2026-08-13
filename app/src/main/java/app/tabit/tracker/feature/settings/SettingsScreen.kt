package app.tabit.tracker.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.tabit.tracker.core.theme.TabitAlpha
import app.tabit.tracker.core.theme.TabitSpacing
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                if (inputStream != null) {
                    val file = File(context.cacheDir, "import_temp.json")
                    file.outputStream().use { out -> inputStream.copyTo(out) }
                    viewModel.importJson(file)
                    file.delete()
                }
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", style = MaterialTheme.typography.headlineMedium)
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = TabitSpacing.base)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(TabitSpacing.sm)
        ) {
            // ── Appearance ──
            SectionHeader("Appearance")

            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(Modifier.padding(TabitSpacing.base)) {
                    Text("Theme", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(TabitSpacing.sm))
                    Row(horizontalArrangement = Arrangement.spacedBy(TabitSpacing.sm)) {
                        listOf("light", "dark", "system").forEach { mode ->
                            FilterChip(
                                selected = state.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(mode.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }
            }

            // ── Archived Habits ──
            SectionHeader("Archived Habits")

            if (state.archivedHabits.isEmpty()) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(Modifier.padding(TabitSpacing.base)) {
                        Text(
                            "No archived habits",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        Modifier.padding(TabitSpacing.base),
                        verticalArrangement = Arrangement.spacedBy(TabitSpacing.sm)
                    ) {
                        Text(
                            "${state.archivedHabits.size} archived habit(s)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        state.archivedHabits.forEach { habit ->
                            Row(
                                Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        Modifier.size(12.dp),
                                        shape = CircleShape,
                                        color = Color(habit.color)
                                    ) {}
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(habit.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        if (habit.description.isNotEmpty()) {
                                            Text(
                                                habit.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                TextButton(onClick = { viewModel.restoreHabit(habit.id) }) {
                                    Text("Restore")
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            // ── Data Management ──
            SectionHeader("Data")

            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(Modifier.padding(TabitSpacing.base)) {
                    Text("Export Data", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(TabitSpacing.xs))
                    Text(
                        "CSV is compatible with Loop Habit Tracker. JSON is a full backup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(TabitSpacing.md))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TabitSpacing.sm)) {
                        OutlinedButton(
                            onClick = { viewModel.exportCsv() },
                            Modifier.weight(1f),
                            enabled = !state.isExporting
                        ) { Text("CSV") }
                        OutlinedButton(
                            onClick = { viewModel.exportJson() },
                            Modifier.weight(1f),
                            enabled = !state.isExporting
                        ) { Text("JSON") }
                    }
                }
            }

            // Import section
            Card(
                Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(Modifier.padding(TabitSpacing.base)) {
                    Text("Import Data", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(TabitSpacing.xs))
                    Text(
                        "Import from a Tabit JSON backup file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(TabitSpacing.md))
                    OutlinedButton(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json"))
                        },
                        Modifier.fillMaxWidth(),
                        enabled = !state.isImporting
                    ) { Text("Import JSON") }
                }
            }

            // Import result
            state.importResult?.let { result ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.success)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            result.message,
                            color = if (result.success)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.dismissImport() }) { Text("OK") }
                    }
                }
            }

            // Export success
            if (state.exportSuccess == true && state.lastExportPath != null) {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Export Complete!",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    try {
                                        val file = File(state.lastExportPath!!)
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val ext = if (file.name.endsWith(".csv")) "text/csv" else "application/json"
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = ext
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share export"))
                                    } catch (_: Exception) { }
                                },
                                Modifier.weight(1f)
                            ) { Text("Share") }
                            OutlinedButton(
                                onClick = { viewModel.dismissExport() },
                                Modifier.weight(1f)
                            ) { Text("Dismiss") }
                        }
                    }
                }
            }

            // Export failed
            if (state.exportSuccess == false) {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Export failed. Try again.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.dismissExport() }) { Text("OK") }
                    }
                }
            }

            // ── General ──
            SectionHeader("General")

            SettingsClickableCard(
                icon = Icons.Default.Info,
                title = "About Tabit",
                subtitle = "Version, features, and credits",
                onClick = { showAboutDialog = true }
            )

            SettingsClickableCard(
                icon = Icons.Default.Security,
                title = "Privacy Policy",
                subtitle = "How your data is handled",
                onClick = { showPrivacyDialog = true }
            )

            Spacer(Modifier.height(TabitSpacing.xxxl))
        }
    }

    // ── About Dialog ──
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    "Tabit",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRow("Version", "1.3.0")
                    InfoRow("Package", "app.tabit.tracker")

                    HorizontalDivider()

                    Text(
                        "Tabit is a minimalist, offline-first habit tracker built around a table/calendar view. Track daily habits, visualize streaks, and review progress with charts — all stored privately on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    Text(
                        "Features",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    FeatureBullet("Table View — Monthly calendar grid with per-day completion")
                    FeatureBullet("Today View — Quick daily check-in with 3-state toggle (done/skip/none)")
                    FeatureBullet("Charts — Weekly & monthly completion stats")
                    FeatureBullet("Streak Tracking — Current & best streak per habit")
                    FeatureBullet("Scoring System — Weighted completion rate + streak bonus")
                    FeatureBullet("Negative Habits — Track 'do less' habits with inverted scoring")
                    FeatureBullet("Notes — Attach notes to any daily record")
                    FeatureBullet("Theme — Light, Dark, or System theme")
                    FeatureBullet("Archive — Archive and restore habits")
                    FeatureBullet("Widget — Home screen widget via Glance")
                    FeatureBullet("Export/Import — CSV & JSON backup with import support")
                    FeatureBullet("Haptic Feedback — Tap & long-press feedback")

                    HorizontalDivider()

                    Text(
                        "Built With",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Kotlin, Jetpack Compose, Room, Hilt, Navigation Compose, Material 3, DataStore, Glance for Widgets",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    Text(
                        "License",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "MIT License. Inspired by mHabit (GPL-3.0) but written from scratch as 100% new code.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Close") }
            }
        )
    }

    // ── Privacy Policy Dialog ──
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    "Privacy Policy",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrivacyHeading("1. Overview")
                    PrivacyBody(
                        "Tabit is designed with a privacy-first philosophy. The app does not require or request any internet permissions. All your data is stored exclusively on your device and never leaves it."
                    )

                    PrivacyHeading("2. Data Collection")
                    PrivacyBody("Tabit does NOT collect, transmit, or share any personal data. Specifically:")
                    PrivacyBullet("No user accounts, sign-ups, or authentication")
                    PrivacyBullet("No analytics, telemetry, or crash reporting")
                    PrivacyBullet("No advertising SDKs or trackers")
                    PrivacyBullet("No network requests of any kind — the app has no INTERNET permission")
                    PrivacyBullet("No location, contacts, camera, or microphone access")

                    PrivacyHeading("3. Data Stored Locally")
                    PrivacyBody("Tabit stores the following data on your device using an encrypted Room database:")
                    PrivacyBullet("Habit names, descriptions, colors, and frequency settings")
                    PrivacyBullet("Daily completion records (date, done/not done/skip status)")
                    PrivacyBullet("Notes attached to individual daily records")
                    PrivacyBullet("App preferences (theme, onboarding state) via DataStore")
                    PrivacyBody(
                        "This data is stored in the app's private internal storage and is not accessible to other apps without root access."
                    )

                    PrivacyHeading("4. Data Export & Deletion")
                    PrivacyBody(
                        "You can export your data at any time via CSV or JSON from the Settings screen. The exported file is saved to your device's shared storage and can be shared or backed up manually."
                    )
                    PrivacyBody(
                        "To delete all data, you can clear the app's storage from Android Settings > Apps > Tabit > Storage > Clear Data, or uninstall the app entirely."
                    )

                    PrivacyHeading("5. Third-Party Services")
                    PrivacyBody(
                        "Tabit does not use any third-party services, SDKs, or libraries that transmit data externally. All dependencies are offline-only (UI framework, database, dependency injection)."
                    )

                    PrivacyHeading("6. Open Source")
                    PrivacyBody(
                        "Tabit is open source under the MIT License. You can review the full source code on GitHub."
                    )
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = MaterialTheme.colorScheme.primary)) {
                                append("github.com/Bilal140202/tabit-android")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 12.dp)
                    )

                    PrivacyHeading("7. Children's Privacy")
                    PrivacyBody(
                        "Tabit does not knowingly collect information from children. Since no data is collected or transmitted at all, the app is safe for users of any age."
                    )

                    PrivacyHeading("8. Changes to This Policy")
                    PrivacyBody(
                        "If Tabit ever adds features that involve data collection or network access, this policy will be updated accordingly and the change will be noted in the app version changelog."
                    )

                    PrivacyHeading("9. Contact")
                    PrivacyBody(
                        "For questions about this privacy policy, open an issue on the GitHub repository: github.com/Bilal140202/tabit-android"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Close") }
            }
        )
    }
}

// ── Reusable Components ──

@Composable
private fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = TabitSpacing.sm, bottom = TabitSpacing.xs)
    )
}

@Composable
private fun SettingsClickableCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier
                .padding(TabitSpacing.base)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(TabitSpacing.base))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TabitAlpha.DISABLED),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FeatureBullet(text: String) {
    Row(
        Modifier.fillMaxWidth().padding(start = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PrivacyHeading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun PrivacyBody(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3f
    )
}

@Composable
private fun PrivacyBullet(text: String) {
    Row(
        Modifier.fillMaxWidth().padding(start = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "•",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}