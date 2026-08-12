package app.tabit.tracker.feature.table

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableCell(
    done: Boolean,
    score: Float,
    dayLabel: String = "",
    isHeader: Boolean = false,
    isToday: Boolean = false,
    habitColor: Color = MaterialTheme.colorScheme.primary,
    onToggle: () -> Unit = {},
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val safeScore = if (score.isNaN()) 0f else score.coerceIn(0f, 1f)
    val todayHighlight = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

    val targetCellColor = when {
        isHeader -> Color.Transparent
        done -> {
            val baseAlpha = 0.45f + 0.55f * safeScore
            habitColor.copy(alpha = baseAlpha)
        }
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    val cellColor by animateColorAsState(
        targetValue = targetCellColor,
        animationSpec = tween(200)
    )

    val targetTextColor = when {
        isHeader -> MaterialTheme.colorScheme.onSurface
        done -> {
            val lum = (0.299f * habitColor.red + 0.587f * habitColor.green + 0.114f * habitColor.blue)
            if (lum > 0.5f) Color(0xDD000000) else Color.White
        }
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
    }
    val textColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(200)
    )

    Box(
        modifier = modifier
            .size(CELL_SIZE_DP.dp)
            .clip(CircleShape)
            .then(
                if (isToday && !done) {
                    Modifier.drawBehind {
                        drawCircle(
                            color = todayHighlight,
                            radius = size.minDimension / 2f
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
        Text(
            text = dayLabel,
            color = textColor,
            fontSize = if (isHeader) 11.sp else 9.sp,
            fontWeight = when {
                done -> FontWeight.Bold
                isToday -> FontWeight.SemiBold
                else -> FontWeight.Normal
            },
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

private const val CELL_SIZE_DP = 38
