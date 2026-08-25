package com.ssajudn.barebudget.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing scale anchored by an 8px base unit — per DESIGN.MD §4 "Spacing & Layout Grid".
 *
 * Exists because spacing was previously invented per screen: horizontal screen
 * padding was 20dp in some places and 24dp in others, and the gap left for the
 * floating navigation bar was 88dp on the dashboard but 100dp on three other
 * screens — for the same bar.
 *
 * A plain object, not a CompositionLocal: these values never vary by theme, and a
 * local would add indirection for nothing.
 *
 * DESIGN.MD token → Kotlin name mapping:
 *   unit            → ExtraSmall (4dp) / Small (8dp)
 *   stack-sm        → StackSm   (8dp)
 *   gutter / stack-md → Medium  (16dp)
 *   container-margin → ScreenHorizontal (24dp)
 *   stack-lg        → StackLg   (32dp)
 *   section-padding → SectionPadding (40dp)
 */
object Spacing {
    /** 4dp — between tightly related items, e.g. a label and its value. */
    val ExtraSmall = 4.dp

    /** 8dp — icon-to-text gap, chip gaps. DESIGN.MD: stack-sm */
    val Small = 8.dp

    /** 8dp — alias for DESIGN.MD `stack-sm` token */
    val StackSm = 8.dp

    /** 12dp — inside compact containers. */
    val MediumSmall = 12.dp

    /** 16dp — default. Card padding, list row insets, grid gutters. DESIGN.MD: gutter / stack-md */
    val Medium = 16.dp

    /** 20dp — generous card interiors. */
    val MediumLarge = 20.dp

    /** 24dp — between major sections. DESIGN.MD: container-margin */
    val Large = 24.dp

    /**
     * 24dp — horizontal inset from the screen edge.
     * DESIGN.MD: container-margin = 24px (Mobile: 4-column layout with 24px horizontal margins).
     *
     * One value for every screen. Vertical list padding should come from the
     * Scaffold's inner padding, not a constant.
     */
    val ScreenHorizontal = 24.dp

    /** 32dp — around hero content. DESIGN.MD: stack-lg */
    val ExtraLarge = 32.dp

    /** 32dp — alias for DESIGN.MD `stack-lg` token */
    val StackLg = 32.dp

    /** 40dp — major view and layout section gutters. DESIGN.MD: section-padding */
    val SectionPadding = 40.dp

    /**
     * 88dp — bottom padding so scrollable content can clear the FAB.
     *
     * A floating action button overlaps content by design, so the navigation bar
     * inset alone is not enough: the last list item would sit underneath it.
     * 56dp FAB + 16dp margin + 16dp breathing room.
     *
     * This is the *only* place a FAB/bar clearance number should appear. The nav
     * bar itself is handled by the Scaffold inset, not by a constant — screens
     * previously hardcoded 88dp and 100dp for that, which is what this replaces.
     */
    val FabClearance = 88.dp
}

/**
 * 48dp — the minimum touch-target size for any interactive element.
 *
 * From the Material accessibility guidance and WCAG 2.1 target-size guidance.
 * Anything tappable should be at least this large even when its icon is smaller;
 * shrink the icon, not the target.
 */
val MinTouchTarget = 48.dp
