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
    habitColor: Color = MaterialTheme.colorScheme.primary,
    onToggle: () -> Unit = {},
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Fix H1: Use habitColor instead of hardcoded red/green
    val targetCellColor = when {
        isHeader -> Color.Transparent
        done -> {
            // Blend from habitColor (low opacity) to full habitColor based on score
            val baseAlpha = 0.4f + 0.6f * score.coerceIn(0f, 1f)
            habitColor.copy(alpha = baseAlpha)
        }
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val cellColor by animateColorAsState(
        targetValue = targetCellColor,
        animationSpec = tween(300)
    )

    val targetTextColor = when {
        isHeader -> MaterialTheme.colorScheme.onSurface
        done -> {
            val lum = (0.299f * habitColor.red + 0.587f * habitColor.green + 0.114f * habitColor.blue)
            if (lum > 0.5f) Color.Black else Color.White
        }
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    val textColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = tween(300)
    )

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
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
            fontWeight = if (done) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
