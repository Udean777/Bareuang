package com.ssajudn.bareuang.ui.navigation
import androidx.compose.material.icons.filled.ReceiptLong

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Savings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.bills.DueBillFormDialog
import com.ssajudn.bareuang.ui.bills.DueBillsViewModel
import com.ssajudn.bareuang.ui.components.AppSpeedDialFab
import com.ssajudn.bareuang.ui.components.SpeedDialItem
import com.ssajudn.bareuang.ui.goals.GoalFormDialog
import com.ssajudn.bareuang.ui.goals.GoalsViewModel
import com.ssajudn.bareuang.ui.tour.tourAnchor
import androidx.compose.ui.res.stringResource

@Composable
fun AppFabHost(
    visible: Boolean,
    currentRoute: String?,
    navController: NavHostController,
) {
    var showBillDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) { expanded = false }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(350)),
        exit = fadeOut(tween(350)),
    ) {
        val items = listOf(
            SpeedDialItem(stringResource(R.string.fab_menu_transaction), Icons.Filled.Add, { navController.navigate(Screen.AddTransaction.route) }),
            SpeedDialItem(stringResource(R.string.fab_menu_scan), Icons.Default.DocumentScanner, { navController.navigate(Screen.OcrScan.route) }),
            SpeedDialItem(stringResource(R.string.fab_menu_bill), Icons.AutoMirrored.Filled.ReceiptLong, { showBillDialog = true }),
            SpeedDialItem(stringResource(R.string.fab_menu_goal), Icons.Filled.Savings, { showGoalDialog = true }),
        )
        AppSpeedDialFab(items, expanded, { expanded = it }, Modifier.tourAnchor("fab_add_transaction"))
    }

    if (showBillDialog) {
        val viewModel: DueBillsViewModel = hiltViewModel()
        DueBillFormDialog(
            onDismiss = { showBillDialog = false },
            onConfirm = { provider, iconUrl, amount, dueDate, recurring, interval, notes ->
                viewModel.addDueBill(provider, iconUrl, amount, dueDate, recurring, interval, notes)
                showBillDialog = false
            },
        )
    }
    if (showGoalDialog) {
        val viewModel: GoalsViewModel = hiltViewModel()
        GoalFormDialog(
            onDismiss = { showGoalDialog = false },
            onConfirm = { name, targetAmount, targetDate, colorHex, notes ->
                viewModel.addGoal(name, targetAmount, targetDate, colorHex, notes)
                showGoalDialog = false
            },
        )
    }
}
