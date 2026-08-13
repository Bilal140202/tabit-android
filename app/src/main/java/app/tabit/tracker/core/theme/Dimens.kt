package app.tabit.tracker.core.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tabit Design Tokens — Spacing, Sizing, Motion.
 * Inspired by impeccable's design system: intentional spacing scale,
 * hairline-first borders, and meaningful animation timing.
 */

object TabitSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val base: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp
}

object TabitSizing {
    val checkboxSize: Dp = 44.dp
    val checkboxIconSize: Dp = 20.dp
    val colorDotSize: Dp = 10.dp
    val streakBadgeIconSize: Dp = 12.dp
    val circularStatSize: Dp = 56.dp
    val circularStatStroke: Dp = 5.dp
    val tableCellSize: Int = 42
    val tableNameColWidth: Int = 80
    val minTouchTarget: Dp = 48.dp
    val cardCornerRadius: Dp = 12.dp
    val progressHeight: Dp = 6.dp
}

object TabitMotion {
    const val IMMEDIATE_MS = 120
    const val ROUTINE_MS = 250
    const val LAYOUT_MS = 350
    const val FOCAL_MS = 500

    // Standard easing — confident arrival (decelerate)
    const val EASING_STANDARD = 0.16f  // cubic-bezier(0.16, 1, 0.3, 1) approximation
}

object TabitAlpha {
    const val HABIT_DONE_BG = 0.10f
    const val HABIT_SKIP_BG = 0.08f
    const val HABIT_CHECK_ICON = 0.92f
    const val MUTED_TEXT = 0.60f
    const val SUBTLE_TEXT = 0.45f
    const val DISABLED = 0.38f
    const val DIVIDER = 0.50f
    const val SURFACE_OVERLAY = 0.95f
    const val CELL_EMPTY = 0.18f
    const val CELL_TODAY_RING = 0.55f
}

/**
 * Semantic status colors — replaces all hardcoded hex values.
 * These are computed from MaterialTheme at use-site, but the alpha/
 * role constants live here to enforce consistency.
 */
object TabitStatusColors {
    // Skip amber: used for skip state across the app
    // Access via MaterialTheme.colorScheme.tertiary or a custom amber token
    const val SKIP_HEX = 0xFFFFC107
    const val SKIP_TEXT_HEX = 0xFFF57F17
    const val SKIP_DARK_TEXT_HEX = 0xFFFFD54F
}
