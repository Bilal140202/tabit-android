package app.tabit.tracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.tabit.tracker.core.theme.TabitAlpha
import app.tabit.tracker.core.theme.TabitMotion
import app.tabit.tracker.core.theme.TabitSizing
import app.tabit.tracker.core.theme.TabitSpacing

/**
 * Shared UI components for Tabit.
 *
 * Design principles applied:
 *   - Hairline-first: borders before shadows (impeccable)
 *   - Intentional spacing via TabitSpacing tokens
 *   - Meaningful animation: feedback at 120ms, layout at 350ms (impeccable animate.md)
 *   - Cards only when elevation communicates hierarchy (taste-skill 4.4)
 */

// ── Empty State ──────────────────────────────────────────────

/**
 * Polished empty state with icon, headline, and subtitle.
 * Replaces the bare-text empty states scattered across screens.
 */
@Composable
fun TabitEmptyState(
    icon: ImageVector,
    headline: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TabitAlpha.SUBTLE_TEXT)
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = tint
        )
        Spacer(Modifier.height(TabitSpacing.lg))
        Text(
            headline,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(TabitSpacing.sm))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TabitAlpha.MUTED_TEXT),
            textAlign = TextAlign.Center
        )
    }
}

// ── Streak Badge ─────────────────────────────────────────────

/**
 * Compact fire streak badge. Uses tertiary container for warmth.
 * Extracted from TodayScreen for reuse.
 */
@Composable
fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = streak > 0,
        enter = scaleIn(animationSpec = androidx.compose.animation.core.tween(TabitMotion.IMMEDIATE_MS)) + fadeIn(),
        exit = scaleOut(animationSpec = androidx.compose.animation.core.tween(TabitMotion.IMMEDIATE_MS)) + fadeOut()
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(TabitSpacing.sm),
        color = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = TabitSpacing.sm, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                Icons.Default.LocalFireDepartment,
                contentDescription = "Streak: $streak days",
                modifier = Modifier.size(TabitSizing.streakBadgeIconSize),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Text(
                "$streak",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}
}

// ── Section Header ────────────────────────────────────────────

/**
 * Section header for settings and grouped content.
 * Label-style: uppercase, tracked, muted.
 */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text.uppercase(),
        modifier = modifier.padding(
            start = TabitSpacing.base,
            end = TabitSpacing.base,
            top = TabitSpacing.lg,
            bottom = TabitSpacing.sm
        ),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing
    )
}

// ── Habit Checkbox (3-state) ──────────────────────────────────

/**
 * The 3-state circular checkbox used in TodayScreen.
 * Extracted for consistency and reuse.
 *
 * States: done (filled + check) → skip (amber + dash) → none (outlined)
 */
@Composable
fun HabitCheckbox(
    isDone: Boolean,
    isSkipped: Boolean,
    habitColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateFloatAsState(
        targetValue = when {
            isDone -> 1f
            isSkipped -> TabitAlpha.HABIT_SKIP_BG
            else -> 0f
        },
        animationSpec = androidx.compose.animation.core.tween(TabitMotion.ROUTINE_MS)
    )

    Surface(
        modifier = modifier
            .size(TabitSizing.checkboxSize)
            .clip(CircleShape),
        shape = CircleShape,
        color = when {
            isDone -> habitColor
            isSkipped -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
            else -> Color.Transparent
        },
        contentColor = when {
            isDone -> Color.White.copy(alpha = TabitAlpha.HABIT_CHECK_ICON)
            isSkipped -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TabitAlpha.DISABLED)
        }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier
            .clip(CircleShape)
            .then(
                if (!isDone && !isSkipped) {
                    Modifier.background(
                        color = Color.Transparent,
                        shape = CircleShape
                    )
                } else Modifier
            )
        ) {
            when {
                isDone -> Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Done",
                    modifier = Modifier.size(TabitSizing.checkboxIconSize)
                )
                isSkipped -> Text(
                    "\u2014",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                else -> Icon(
                    Icons.Filled.RemoveCircleOutline,
                    contentDescription = "Not done",
                    modifier = Modifier.size(TabitSizing.checkboxIconSize)
                )
            }
        }
        // Invisible click target meeting 48dp minimum
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(Color.Transparent)
                .then(if (!isDone && !isSkipped) Modifier else Modifier)
        ) {
            if (!isDone && !isSkipped) {
                // Draw outline ring for empty state
                Surface(
                    modifier = Modifier.size(TabitSizing.checkboxSize - 4.dp),
                    shape = CircleShape,
                    color = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TabitAlpha.DISABLED)
                ) {
                    // Empty — just the outline icon
                    Icon(
                        Icons.Filled.RemoveCircleOutline,
                        contentDescription = "Not done",
                        modifier = Modifier.size(TabitSizing.checkboxIconSize)
                    )
                }
            }
        }
    }
}

// ── Stat Item ─────────────────────────────────────────────────

/**
 * A value + label stat display. Used in ChartsScreen overview.
 */
@Composable
fun StatItem(
    value: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
