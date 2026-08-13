package app.tabit.tracker.core.theme

import androidx.compose.ui.graphics.Color

/**
 * Tabit Color Tokens — light and dark fallback palettes.
 *
 * Design principles applied:
 *   - Warm teal primary (distinctive, not generic blue/purple — taste-skill anti-default)
 *   - High-contrast text on dark surfaces (impeccable: dark type needs air)
 *   - Tonal elevation via surface variants, not arbitrary shadows
 *   - Skip amber as a semantic state color, not a random accent
 */

// ── Light scheme ──────────────────────────────────────────────
val md_primary_light = Color(0xFF006B5E)
val md_on_primary_light = Color(0xFFFFFFFF)
val md_primary_container_light = Color(0xFF7AF8DE)
val md_on_primary_container_light = Color(0xFF00201B)
val md_secondary_light = Color(0xFF4A635C)
val md_on_secondary_light = Color(0xFFFFFFFF)
val md_tertiary_light = Color(0xFF9B6A00)  // Warm amber for skip states
val md_on_tertiary_light = Color(0xFFFFFFFF)
val md_error_light = Color(0xFFBA1A1A)
val md_on_error_light = Color(0xFFFFFFFF)
val md_background_light = Color(0xFFF8FAF8)
val md_on_background_light = Color(0xFF191C1B)
val md_surface_light = Color(0xFFF8FAF8)
val md_on_surface_light = Color(0xFF191C1B)
val md_surface_variant_light = Color(0xFFDAE5E0)
val md_on_surface_variant_light = Color(0xFF3F4945)
val md_outline_light = Color(0xFF6F7975)
val md_outline_variant_light = Color(0xFFBEC9C4)

// ── Dark scheme ───────────────────────────────────────────────
val md_primary_dark = Color(0xFF5CDBC2)
val md_on_primary_dark = Color(0xFF003730)
val md_primary_container_dark = Color(0xFF005045)
val md_on_primary_container_dark = Color(0xFF7AF8DE)
val md_secondary_dark = Color(0xFFB0CFC6)
val md_on_secondary_dark = Color(0xFF1C352E)
val md_tertiary_dark = Color(0xFFFFD54F)  // Visible amber for dark skip states
val md_on_tertiary_dark = Color(0xFF3B2E00)
val md_error_dark = Color(0xFFFFB4AB)
val md_on_error_dark = Color(0xFF690005)
val md_background_dark = Color(0xFF191C1B)
val md_on_background_dark = Color(0xFFE0E3E0)
val md_surface_dark = Color(0xFF191C1B)
val md_on_surface_dark = Color(0xFFE0E3E0)
val md_surface_variant_dark = Color(0xFF3F4945)
val md_on_surface_variant_dark = Color(0xFFBEC9C4)
val md_outline_dark = Color(0xFF899390)
val md_outline_variant_dark = Color(0xFF3F4945)

// ── Semantic convenience accessors ─────────────────────────────
// These are NOT used inside composables (use MaterialTheme.colorScheme there).
// They exist for non-composable contexts (e.g., Canvas drawing, Room defaults).
object TabitColors {
    val SkipAmber = Color(0xFFFFC107)
    val SkipAmberText = Color(0xFFF57F17)
    val SkipAmberDarkText = Color(0xFFFFD54F)
}