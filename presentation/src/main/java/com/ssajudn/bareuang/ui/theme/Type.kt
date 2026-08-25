package com.ssajudn.bareuang.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ssajudn.bareuang.presentation.R

// -----------------------------------------------------------------------------
// Font Families — DESIGN.MD spec
// Headline / Display: Plus Jakarta Sans (friendly, bold, brand)
// Body / Label: Be Vietnam Pro (clean, readable)
// -----------------------------------------------------------------------------

val HeadlineFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

val BodyFontFamily = FontFamily(
    Font(R.font.be_vietnam_pro_regular, FontWeight.Normal),
    Font(R.font.be_vietnam_pro_medium, FontWeight.Medium),
    Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold),
)

// Kept for reference / legacy — prefer HeadlineFontFamily
val AppFontFamily = HeadlineFontFamily

// -----------------------------------------------------------------------------
// Typography scale — M3 roles mapped to DESIGN.MD tokens
// display*   → headline-xl (40px/800) and headline-lg (32px/700)
// headline*  → headline-md (24px/700) and headline-lg-mobile (28px/700)
// title*     → body-lg (18px/500)
// body*      → body-md (16px/400)
// label*     → label-md (14px/600)
// -----------------------------------------------------------------------------

val Typography = Typography(
    // --- Display (hero headers, splash greetings) ---
    displayLarge = TextStyle(              // headline-xl: 40px/800
        fontFamily = HeadlineFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.8).sp,        // -0.02em
    ),
    displayMedium = TextStyle(
        fontFamily = HeadlineFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.36).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = HeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp,
    ),

    // --- Headline (section titles, dashboard totals) ---
    headlineLarge = TextStyle(            // headline-lg: 32px/700
        fontFamily = HeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.32).sp,      // -0.01em
    ),
    headlineMedium = TextStyle(           // headline-lg-mobile: 28px/700
        fontFamily = HeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(            // headline-md: 24px/700
        fontFamily = HeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),

    // --- Title (card headers, modal titles) ---
    titleLarge = TextStyle(
        fontFamily = HeadlineFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.15).sp,
    ),
    titleMedium = TextStyle(              // body-lg: 18px/500
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    ),

    // --- Body (default content, transaction lists) ---
    bodyLarge = TextStyle(                // body-md: 16px/400
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),

    // --- Label (category tags, form labels, timestamps) ---
    labelLarge = TextStyle(               // label-md: 14px/600
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.7.sp,          // +0.05em
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

val SystemTypography = Typography(/* default system fonts, no override needed */)

// -----------------------------------------------------------------------------
// Custom Token: price-display
// For primary balance & currency figures — tabular nums, tight tracking.
// DESIGN.MD spec: Plus Jakarta Sans, 36px, weight 800, lineHeight 44px, -0.03em
// -----------------------------------------------------------------------------

/**
 * Use this style for ALL currency hero figures (balances, totals, price tags).
 * Always renders with tabular figures so digits align in columns.
 */
val PriceDisplayStyle = TextStyle(
    fontFamily = HeadlineFontFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = (-1.08).sp,          // -0.03em × 36sp
    fontFeatureSettings = "\"tnum\"",    // tabular numbers
)

// Convenience alias kept for callers that already use MoneyHeadlineStyle.
val MoneyFontFamily: FontFamily get() = HeadlineFontFamily

val MoneyHeadlineStyle: TextStyle
    @Composable get() = MaterialTheme.typography.headlineLarge.copy(
        fontFamily = MoneyFontFamily,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    )

fun typographyFor(fontFamily: FontFamily): Typography = Typography
