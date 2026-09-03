package com.ssajudn.bareuang.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.PriceDisplayStyle
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.utils.CurrencyFormatter
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@Composable
fun FinancialRunwayCard(
    remainingBudget: Long,
    netWorth: Long,
    totalBudget: Long,
    estimatedDeathDay: Int,
    daysInMonth: Int,
    message: String,
    modifier: Modifier = Modifier,
    onSetBudgetClick: () -> Unit = {},
    dailyAllowance: Long = 0L,
    todaySpent: Long = 0L,
    remainingToday: Long = 0L,
    remainingDays: Int = 0,
) {
    val isDanger = remainingBudget <= 0 || estimatedDeathDay < daysInMonth
    val containerColor = if (isDanger) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (isDanger) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    val accentColor = if (isDanger) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    val targetProgress = if (totalBudget > 0) {
        (remainingBudget.toFloat() / totalBudget.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "runwayProgress",
    )

    // Count-up money display — animates from the previous value to the new one
    val countUp = remember { Animatable(0f) }
    LaunchedEffect(remainingBudget) {
        countUp.animateTo(remainingBudget.toFloat(), tween(900, easing = FastOutSlowInEasing))
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .crispBorder(
                shape = MaterialTheme.shapes.extraLarge,
                color = accentColor.copy(alpha = 0.25f)
            ),
        shape = MaterialTheme.shapes.extraLarge,      // rounded-xl = 48dp per DESIGN.MD
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        // DESIGN.MD §6: soft ambient shadow, bear-brown tint, 5-8% opacity
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        // Aurora sheen — a soft light band sweeping across the hero card forever
        val aurora = rememberInfiniteTransition(label = "aurora")
        val sweep by aurora.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(7000, easing = androidx.compose.animation.core.LinearEasing)),
            label = "auroraSweep",
        )
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        val angle = sweep * 2f * PI.toFloat()
                        val radius = max(size.width, size.height)
                        val dir = Offset(cos(angle), sin(angle))
                        drawRect(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    accentColor.copy(alpha = 0.16f),
                                    Color.Transparent,
                                ),
                                start = size.center - dir * radius,
                                end = size.center + dir * radius,
                            )
                        )
                    }
            )
            Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(AppShapes.Pill)
                        .background(contentColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    // Pulsing status dot indicator with infinite alpha animation
                    val pulse by rememberInfiniteTransition(label = "runwayDot").animateFloat(
                        initialValue = 0.6f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                        label = "dotAlpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = pulse)),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.runway_label),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp
                        ),
                        fontWeight = FontWeight.ExtraBold,
                    )
                }

                AppFilledTonalButton(
                    onClick = onSetBudgetClick,
                    shape = AppShapes.Pill,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 6.dp,
                    ),
                ) {
                    Text(
                        text = stringResource(if (totalBudget > 0) R.string.runway_edit else R.string.runway_set),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Main remaining runway budget display using dedicated money typography
            Text(
                text = CurrencyFormatter.formatRupiah(countUp.value.toLong()),
                style = PriceDisplayStyle,
            )

            Spacer(Modifier.height(10.dp))

            // Signature Expressive Gauge Bar with Bear Mascot
            BearProgressIndicator(
                progress = animatedProgress,
                color = accentColor,
                trackColor = contentColor.copy(alpha = 0.15f),
                trackHeight = 10.dp,
                bearSize = 22.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            // Total Net Worth & Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.runway_total_wealth),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.8.sp),
                    fontWeight = FontWeight.Bold,
                    color = contentColor.copy(alpha = 0.8f)
                )
                Text(
                    text = CurrencyFormatter.formatRupiah(netWorth),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = contentColor
                )
            }

            Spacer(Modifier.height(12.dp))

            // Runway Insights Pill
            Surface(
                shape = AppShapes.Squircle,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isDanger) Icons.Default.Warning else Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            }
        }
    }
}

@Composable
fun DailyPacingCard(
    dailyAllowance: Long,
    todaySpent: Long,
    remainingToday: Long,
    remainingDays: Int,
    modifier: Modifier = Modifier,
) {
    val dailyExceeded = remainingToday < 0
    val dailyProgress = if (dailyAllowance > 0) (todaySpent.toFloat() / dailyAllowance).coerceIn(0f, 1f) else 0f
    val accent = if (dailyExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
    val darkTheme = isSystemInDarkTheme()
    val cardContent = when {
        dailyExceeded -> MaterialTheme.colorScheme.onErrorContainer
        darkTheme -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .crispBorder(shape = AppShapes.Squircle, color = accent.copy(alpha = 0.35f)),
        shape = AppShapes.Squircle,
        color = if (dailyExceeded) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.dashboard_daily_title), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = cardContent)
                    Text(stringResource(R.string.dashboard_daily_auto_desc), style = MaterialTheme.typography.bodySmall, color = cardContent.copy(alpha = 0.75f))
                }
                if (dailyExceeded) Icon(Icons.Default.Warning, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val metricColor = when {
                    dailyExceeded -> MaterialTheme.colorScheme.onErrorContainer
                    darkTheme -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
                DailyPacingMetric(stringResource(R.string.dashboard_daily_target), CurrencyFormatter.formatRupiah(dailyAllowance), Modifier.weight(1f), metricColor)
                DailyPacingMetric(stringResource(R.string.dashboard_daily_spent), CurrencyFormatter.formatRupiah(todaySpent), Modifier.weight(1f), metricColor)
                DailyPacingMetric(stringResource(R.string.dashboard_daily_remaining_label), CurrencyFormatter.formatRupiah(remainingToday.coerceAtLeast(0L)), Modifier.weight(1f), if (dailyExceeded) accent else metricColor)
            }
            Spacer(Modifier.height(12.dp))
            BearProgressIndicator(
                progress = dailyProgress,
                modifier = Modifier.fillMaxWidth(),
                color = accent,
                trackColor = if (dailyExceeded) MaterialTheme.colorScheme.error.copy(alpha = 0.18f) else (if (darkTheme) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.18f),
                trackHeight = 8.dp,
                bearSize = 28.dp,
                indicatorRes = R.drawable.ic_app_logo,
            )
            if (remainingDays > 0) {
                Spacer(Modifier.height(6.dp))
                Text("${stringResource(R.string.dashboard_daily_days_left, remainingDays)} • ${CurrencyFormatter.formatRupiah(dailyAllowance)}/hari", style = MaterialTheme.typography.labelSmall, color = cardContent.copy(alpha = 0.75f))
            }
            if (dailyExceeded) {
                Spacer(Modifier.height(6.dp))
                Text(stringResource(R.string.tx_error_daily_exceeded), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = accent)
            }
        }
    }
}

@Composable
private fun DailyPacingMetric(label: String, value: String, modifier: Modifier, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = valueColor, maxLines = 1)
    }
}
