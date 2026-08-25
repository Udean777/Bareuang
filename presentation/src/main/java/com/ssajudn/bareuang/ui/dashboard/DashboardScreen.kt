package com.ssajudn.bareuang.ui.dashboard

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.bareuang.domain.model.DashboardSummary
import com.ssajudn.bareuang.ui.components.ErrorState
import com.ssajudn.bareuang.ui.components.FinancialRunwayCard
import com.ssajudn.bareuang.ui.components.RollingNumber
import com.ssajudn.bareuang.ui.components.TransactionItem
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.ui.components.AppTextButton
import com.ssajudn.bareuang.ui.components.pressScale
import com.ssajudn.bareuang.ui.theme.*
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import com.ssajudn.bareuang.utils.CurrencyFormatter
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.tour.tourAnchor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddManual: () -> Unit,
    onNavigateToWallets: () -> Unit,
    onNavigateToDueBills: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToTransactionDetail: (String) -> Unit,
    onNavigateToAllTransactions: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    // Auto-refresh dashboard data whenever returning back to this screen
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        viewModel.loadDashboardData()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val squishX by animateFloatAsState(if (isRefreshing) 1.12f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "squishX")
                        val squishY by animateFloatAsState(if (isRefreshing) 0.88f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), label = "squishY")
                        Image(
                            painter = painterResource(id = R.drawable.ic_app_logo),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.size(38.dp).clip(MaterialTheme.shapes.small).graphicsLayer { scaleX = squishX; scaleY = squishY }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Bareuang",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                text = stringResource(R.string.dashboard_subtitle),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    var showThemeDialog by remember { mutableStateOf(false) }
                    val currentDarkMode by viewModel.darkMode.collectAsStateWithLifecycle()

                    val themeIcon = when (currentDarkMode) {
                        AppThemeDarkMode.Dark -> Icons.Default.DarkMode
                        AppThemeDarkMode.Light -> Icons.Default.LightMode
                        AppThemeDarkMode.FollowSystem -> Icons.Default.BrightnessAuto
                    }

                    AppIconButton(onClick = { showThemeDialog = true }) {
                        Icon(
                            imageVector = themeIcon,
                            contentDescription = stringResource(R.string.dashboard_theme_desc)
                        )
                    }

                    AppIconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }

                    if (showThemeDialog) {
                        AlertDialog(
                            onDismissRequest = { showThemeDialog = false },
                            title = {
                                Text(
                                    text = stringResource(R.string.dashboard_choose_theme),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val options = listOf(
                                        Triple(AppThemeDarkMode.FollowSystem, stringResource(R.string.dashboard_follow_system), Icons.Default.BrightnessAuto),
                                        Triple(AppThemeDarkMode.Light, stringResource(R.string.dashboard_light), Icons.Default.LightMode),
                                        Triple(AppThemeDarkMode.Dark, stringResource(R.string.dashboard_dark), Icons.Default.DarkMode)
                                    )

                                    options.forEach { (mode, label, icon) ->
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.setDarkMode(mode)
                                                    showThemeDialog = false
                                                },
                                            color = if (currentDarkMode == mode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = if (currentDarkMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = if (currentDarkMode == mode) FontWeight.Bold else FontWeight.Normal
                                                    ),
                                                    color = if (currentDarkMode == mode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (currentDarkMode == mode) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                AppTextButton(onClick = { showThemeDialog = false }) {
                                    Text(stringResource(R.string.common_close))
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadDashboardData(isPullToRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                is DashboardUiState.Error -> {
                    ErrorState(
                        title = stringResource(R.string.dashboard_load_error),
                        message = state.message,
                        retryLabel = stringResource(R.string.common_retry),
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = { viewModel.loadDashboardData() }
                    )
                }
                is DashboardUiState.Success -> {
                    DashboardContent(
                        summary = state.summary,
                        onSetBudgetClick = onNavigateToBudget,
                        onAddManualClick = onNavigateToAddManual,
                        onWalletsClick = onNavigateToWallets,
                        onDueBillsClick = onNavigateToDueBills,
                        onGoalsClick = onNavigateToGoals,
                        onTransactionClick = onNavigateToTransactionDetail,
                        onSeeAllTransactionsClick = onNavigateToAllTransactions,
                        onAnalyticsClick = onNavigateToAnalytics
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContent(
    summary: DashboardSummary,
    onSetBudgetClick: () -> Unit,
    onAddManualClick: () -> Unit,
    onWalletsClick: () -> Unit,
    onDueBillsClick: () -> Unit,
    onGoalsClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onSeeAllTransactionsClick: () -> Unit,
    onAnalyticsClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = Spacing.Small, bottom = Spacing.FabClearance)
    ) {
        // 1. FINANCIAL RUNWAY CARD (Core Feature)
        item {
            FinancialRunwayCard(
                modifier = Modifier.tourAnchor("dashboard_runway"),
                remainingBudget = summary.remainingBudget,
                netWorth = summary.netWorth,
                totalBudget = summary.monthlyBudget,
                estimatedDeathDay = summary.estimatedDeathDay,
                daysInMonth = summary.daysInMonth,
                message = summary.runwayMessage,
                onSetBudgetClick = onSetBudgetClick
            )
        }

        // 2. QUICK ACTION TILES (M3 Surface Containers)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .tourAnchor("dashboard_quick_actions"),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = stringResource(R.string.dashboard_quick_wallet),
                    subtitle = stringResource(R.string.dashboard_quick_wallet_desc),
                    icon = Icons.Default.AccountBalanceWallet,
                    bgColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tintColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onWalletsClick
                )

                QuickActionCard(
                    title = stringResource(R.string.dashboard_quick_bills),
                    subtitle = if (summary.unpaidDueBillsSum > 0) {
                        CurrencyFormatter.formatCompact(summary.unpaidDueBillsSum)
                    } else stringResource(R.string.bills_badge_paid),
                    icon = Icons.Default.ReceiptLong,
                    bgColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tintColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onDueBillsClick
                )

                QuickActionCard(
                    title = stringResource(R.string.dashboard_quick_goals),
                    subtitle = stringResource(R.string.dashboard_quick_goals_desc),
                    icon = Icons.Default.Payments,
                    bgColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tintColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = onGoalsClick
                )
            }
        }

        // 3. MONTHLY SPENDING METRICS
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium, // rounded-md = 24dp
                color = MaterialTheme.colorScheme.surfaceContainerLowest, // white on cream canvas
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(
                    0.8.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.dashboard_total_spent),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RollingNumber(
                            value = summary.totalSpent,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.dashboard_daily_avg),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RollingNumber(
                            value = summary.averageDailySpend,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        )
                    }
                }
            }
        }

        // 4. RECURRING SCHEDULES (Hanya jika ada template recurring aktif)
        if (summary.recurringTransactions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_recurring_schedule),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            summary.recurringTransactions.forEachIndexed { idx, tx ->
                item {
                    com.ssajudn.bareuang.ui.components.StaggeredFadeIn(idx) {
                        com.ssajudn.bareuang.ui.components.RecurringTransactionItem(
                            transaction = tx,
                            onClick = { tx.id?.let(onTransactionClick) }
                        )
                    }
                }
            }
        }

        // 5. RECENT TRANSACTIONS HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_recent),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.dashboard_see_all),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onSeeAllTransactionsClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        val recent = summary.recentTransactions
        if (recent.isNullOrEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.dashboard_no_tx),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            recent.forEachIndexed { idx, tx ->
                item {
                    com.ssajudn.bareuang.ui.components.StaggeredFadeIn(idx) {
                        TransactionItem(
                            transaction = tx,
                            onClick = { tx.id?.let(onTransactionClick) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    tintColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Surface(
        modifier = modifier
            .clip(AppShapes.Squircle)
            .pressScale(interactionSource, pressedScale = 0.90f)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            ),
        color = bgColor,
        shape = AppShapes.Squircle,
        border = androidx.compose.foundation.BorderStroke(
            0.8.dp,
            tintColor.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(AppShapes.Squircle)
                    .background(tintColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
