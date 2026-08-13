package app.tabit.tracker.feature.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.tabit.tracker.core.theme.TabitAlpha
import app.tabit.tracker.core.theme.TabitMotion
import app.tabit.tracker.core.theme.TabitSpacing
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
            Modifier
                .fillMaxSize()
                .padding(TabitSpacing.xxl),
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
                        "Consistency is rewarded.\nMorning completions get 1.2x weight.\nStreaks of 3 and 7 days earn bonuses.",
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
                        color = color.copy(alpha = 0.10f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = title,
                                modifier = Modifier.size(44.dp),
                                tint = color
                            )
                        }
                    }
                    Spacer(Modifier.height(TabitSpacing.xxl))
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(TabitSpacing.base))
                    Text(
                        description,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TabitAlpha.MUTED_TEXT),
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4f
                    )
                }
            }

            // Page indicators with animated sizing
            Row(
                Modifier.padding(TabitSpacing.base),
                horizontalArrangement = Arrangement.spacedBy(TabitSpacing.sm)
            ) {
                repeat(3) { index ->
                    val isActive = index == pagerState.currentPage
                    val size by animateDpAsState(
                        targetValue = if (isActive) 24.dp else 8.dp,
                        animationSpec = tween(TabitMotion.ROUTINE_MS)
                    )
                    Surface(
                        Modifier.size(width = size, height = 8.dp),
                        shape = CircleShape,
                        color = if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {}
                }
            }

            // Navigation buttons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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

private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private operator fun <A, B, C, D> Tuple4<A, B, C, D>.component1(): A = first
private operator fun <A, B, C, D> Tuple4<A, B, C, D>.component2(): B = second
private operator fun <A, B, C, D> Tuple4<A, B, C, D>.component3(): C = third
private operator fun <A, B, C, D> Tuple4<A, B, C, D>.component4(): D = fourth
