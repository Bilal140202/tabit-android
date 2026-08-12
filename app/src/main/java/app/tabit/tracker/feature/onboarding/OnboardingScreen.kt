package app.tabit.tracker.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HorizontalPager(state = pagerState, Modifier.weight(1f)) { page ->
                val (icon, title, description, color) = when (page) {
                    0 -> Tuple4(
                        Icons.Default.CalendarMonth, "Table View",
                        "See your entire month at a glance.\nRows = habits, Columns = days.\nTap to toggle, long-press for notes.",
                        primaryColor
                    )
                    1 -> Tuple4(
                        Icons.Default.LocalFireDepartment, "Smart Scoring",
                        "Consistency is rewarded.\nMorning completions get 1.2× weight.\nStreaks of 3 and 7 days earn bonuses.",
                        tertiaryColor
                    )
                    else -> Tuple4(
                        Icons.Default.CloudOff, "100% Offline",
                        "No internet permission. No accounts.\nNo tracking. No ads.\nYour data stays on your device forever.",
                        secondaryColor
                    )
                }
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        Modifier.size(96.dp),
                        shape = CircleShape,
                        color = color.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = title,
                                modifier = Modifier.size(48.dp),
                                tint = color
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    Surface(
                        Modifier.size(if (index == pagerState.currentPage) 10.dp else 8.dp),
                        shape = CircleShape,
                        color = if (index == pagerState.currentPage) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {}
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } }
                    ) { Text("Back") }
                } else {
                    TextButton(onClick = onFinish) { Text("Skip") }
                }
                if (pagerState.currentPage < 2) {
                    Button(
                        onClick = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } }
                    ) { Text("Next") }
                } else {
                    Button(onClick = onFinish) { Text("Get Started") }
                }
            }
        }
    }
}

// Helper since Kotlin doesn't have Tuple4 in stdlib
private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private operator fun <A, B, C, D> Tuple4<A, B, C, D>.component1(): A = first
private operator fun <A, B, C, D> Tuple4<A, B, C, D>.component2(): B = second
private operator fun <A, B, C, D> Tuple4<A, B, C, D>.component3(): C = third
private operator fun <A, B, C, D> Tuple4<A, B, C, D>.component4(): D = fourth
