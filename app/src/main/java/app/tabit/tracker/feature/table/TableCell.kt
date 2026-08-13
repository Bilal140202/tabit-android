package app.tabit.tracker.feature.table

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import app.tabit.tracker.core.theme.TabitAlpha
import app.tabit.tracker.core.theme.TabitMotion
import app.tabit.tracker.core.theme.TabitSizing

/**
 * Calendar table cell — 3-state circle with today ring.
 *
 * Design refinements applied:
 *   - Faster feedback animation (120ms from impeccable: "immediate feedback")
 *   - Today ring uses primary with intentional alpha, not generic 0.5f
 *   - Empty cell uses very subtle surface tint, not harsh outline
 *   - Skip state uses MaterialTheme.tertiary (amber) — no hardcoded hex
 *   - Check icon uses rounded stroke cap for polish
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableCell(
    done: Boolean,
    score: Float,
    isToday: Boolean = false,
    habitColor: Color = MaterialTheme.colorScheme.primary,
    status: String = "none",
    onToggle: () -> Unit = {},
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val todayRingColor = MaterialTheme.colorScheme.primary.copy(alpha = TabitAlpha.CELL_TODAY_RING)

    val cellColor by animateColorAsState(
        targetValue = when {
            status == "skip" -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
            done -> habitColor.copy(alpha = 0.85f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = TabitAlpha.CELL_EMPTY)
        },
        animationSpec = tween(TabitMotion.IMMEDIATE_MS)
    )

    Box(
        modifier = modifier
            .size(TabitSizing.tableCellSize.dp)
            .clip(CircleShape)
            .background(cellColor)
            .then(
                if (isToday) {
                    Modifier.drawBehind {
                        drawCircle(
                            color = todayRingColor,
                            radius = size.minDimension / 2f,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                } else Modifier
            )
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            done -> Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Done",
                tint = Color.White.copy(alpha = TabitAlpha.HABIT_CHECK_ICON),
                modifier = Modifier.size(TabitSizing.checkboxIconSize)
            )
            status == "skip" -> Text(
                text = "\u2014",
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}
