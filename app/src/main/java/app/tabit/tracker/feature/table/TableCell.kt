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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableCell(
    done: Boolean,
    score: Float,
    isToday: Boolean = false,
    habitColor: Color = MaterialTheme.colorScheme.primary,
    onToggle: () -> Unit = {},
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val safeScore = if (score.isNaN()) 0f else score.coerceIn(0f, 1f)
    val todayRingColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

    // Cell background: filled when done, subtle when not
    val cellColor by animateColorAsState(
        targetValue = when {
            done -> habitColor.copy(alpha = 0.85f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        },
        animationSpec = tween(200)
    )

    Box(
        modifier = modifier
            .size(CELL_SIZE_DP.dp)
            .clip(CircleShape)
            .background(cellColor)
            .then(
                if (isToday) {
                    Modifier.drawBehind {
                        // Draw a ring around today's cell
                        drawCircle(
                            color = todayRingColor,
                            radius = size.minDimension / 2f,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
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
        if (done) {
            // Show a small checkmark when completed
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Done",
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(CHECK_ICON_SIZE.dp)
            )
        }
    }
}

private const val CELL_SIZE_DP = 38
private const val CHECK_ICON_SIZE = 16
