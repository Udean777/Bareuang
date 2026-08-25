package com.ssajudn.barebudget.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.ssajudn.barebudget.domain.model.TransactionCategory

// -----------------------------------------------------------------------------
// Category Color System
// Harmonized with Bareuang's warm amber/honey palette (seed #845400).
// Each category has 3 roles: accent (icon tint), container (bg), onContainer (text/icon on bg).
// -----------------------------------------------------------------------------

@Immutable
class CategoryColors internal constructor(
    private val accentSet: CategoryColorSet,
    private val containerSet: CategoryColorSet,
    private val onContainerSet: CategoryColorSet,
) {
    fun accent(category: TransactionCategory): Color = accentSet.pick(category)
    fun container(category: TransactionCategory): Color = containerSet.pick(category)
    fun onContainer(category: TransactionCategory): Color = onContainerSet.pick(category)
}

private fun CategoryColorSet.pick(category: TransactionCategory): Color = when (category) {
    TransactionCategory.FOOD          -> food
    TransactionCategory.TRANSPORT     -> transport
    TransactionCategory.BILLS         -> bills
    TransactionCategory.SHOPPING      -> shopping
    TransactionCategory.ENTERTAINMENT -> entertainment
    TransactionCategory.SOCIAL        -> social
    TransactionCategory.OTHER         -> other
    else                              -> other
}

internal data class CategoryColorSet(
    val food: Color,
    val transport: Color,
    val bills: Color,
    val shopping: Color,
    val entertainment: Color,
    val social: Color,
    val other: Color,
)

// -----------------------------------------------------------------------------
// Light theme — warm-harmonized category accents
// Designed to complement the amber/honey primary and cream background.
// -----------------------------------------------------------------------------

internal val LightCategoryAccent = CategoryColorSet(
    food          = Color(0xFFB5410C), // warm terracotta
    transport     = Color(0xFF1A6085), // deep teal-blue
    bills         = Color(0xFF845400), // primary honey (on-brand)
    shopping      = Color(0xFF396842), // secondary forest green
    entertainment = Color(0xFF6B4E8A), // soft purple
    social        = Color(0xFF2E7D6B), // muted teal
    other         = Color(0xFF7A5648), // tertiary bear brown
)

internal val LightCategoryContainer = CategoryColorSet(
    food          = Color(0xFFF9DDD4), // warm peach
    transport     = Color(0xFFD4EAF5), // light teal wash
    bills         = Color(0xFFFFDDB5), // primary-fixed honey
    shopping      = Color(0xFFBAF0BF), // secondary-container green
    entertainment = Color(0xFFEADEF5), // lavender wash
    social        = Color(0xFFD1F0E8), // mint wash
    other         = Color(0xFFEDE0D9), // warm sand
)

internal val LightCategoryOnContainer = CategoryColorSet(
    food          = Color(0xFF6E1E00), // deep terracotta
    transport     = Color(0xFF0D3D52), // deep navy
    bills         = Color(0xFF623E00), // on-primary-container
    shopping      = Color(0xFF1B4D28), // deep forest
    entertainment = Color(0xFF3A1F5A), // deep violet
    social        = Color(0xFF0B4035), // deep teal
    other         = Color(0xFF4A2E24), // deep bear brown
)

// -----------------------------------------------------------------------------
// Dark theme — elevated, saturated variants for OLED legibility
// -----------------------------------------------------------------------------

internal val DarkCategoryAccent = CategoryColorSet(
    food          = Color(0xFFFF8A65), // warm coral
    transport     = Color(0xFF4FC3F7), // sky blue
    bills         = Color(0xFFFFB958), // primary-fixed-dim
    shopping      = Color(0xFF9FD3A4), // secondary-fixed-dim
    entertainment = Color(0xFFCE93D8), // light purple
    social        = Color(0xFF80CBC4), // teal
    other         = Color(0xFFD6A998), // tertiary-container
)

internal val DarkCategoryContainer = CategoryColorSet(
    food          = Color(0xFF7A2A0E), // dark terracotta
    transport     = Color(0xFF0D3D52), // dark navy
    bills         = Color(0xFF643F00), // on-primary-fixed-variant
    shopping      = Color(0xFF21502C), // on-secondary-fixed-variant
    entertainment = Color(0xFF4A235A), // dark violet
    social        = Color(0xFF0B4035), // dark teal
    other         = Color(0xFF5E3D31), // on-tertiary-container
)

internal val DarkCategoryOnContainer = CategoryColorSet(
    food          = Color(0xFFF9DDD4), // light peach
    transport     = Color(0xFFD4EAF5), // light teal
    bills         = Color(0xFFFFDDB5), // primary-fixed
    shopping      = Color(0xFFBAF0BF), // secondary-container
    entertainment = Color(0xFFEADEF5), // lavender
    social        = Color(0xFFD1F0E8), // mint
    other         = Color(0xFFFFDBCE), // tertiary-fixed
)

internal val LightCategoryColors = CategoryColors(
    accentSet = LightCategoryAccent,
    containerSet = LightCategoryContainer,
    onContainerSet = LightCategoryOnContainer,
)

internal val DarkCategoryColors = CategoryColors(
    accentSet = DarkCategoryAccent,
    containerSet = DarkCategoryContainer,
    onContainerSet = DarkCategoryOnContainer,
)

// -----------------------------------------------------------------------------
// Semantic income/expense accents — shared by analytics, charts, and lists.
// Aligned with DESIGN.MD color roles:
//   Income   → secondary (Soft Forest Green)
//   Expense  → error (Berry Red)
//   Warning  → primary-container (Honey Yellow)
// -----------------------------------------------------------------------------

/** Positive cashflow, income, savings — secondary Forest Green */
val IncomeAccent = Color(0xFF396842)

/** Negative balance, expense, over-budget — error Berry Red */
val ExpenseAccent = Color(0xFFBA1A1A)

/** Budget warning, approaching limit — primary-container Honey */
val BudgetWarningAccent = Color(0xFFF4A216)
