package com.ssajudn.bareuang.ui.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.ui.components.AppDatePickerDialog
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.utils.CurrencyVisualTransformation
import com.ssajudn.bareuang.utils.DateUtils
import com.ssajudn.bareuang.ui.components.AppButton
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.ui.components.ConfettiBurst

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransferScreen(
    onNavigateBack: (() -> Unit)? = null,
    onTransferSuccess: (() -> Unit)? = null,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Set transaction type to TRANSFER on open
    LaunchedEffect(Unit) {
        viewModel.onTransactionTypeChange(TransactionType.TRANSFER)
    }

    // Refresh wallet balance on resume
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.onTransactionTypeChange(TransactionType.TRANSFER)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var sourceDropdownExpanded by remember { mutableStateOf(false) }
    var targetDropdownExpanded by remember { mutableStateOf(false) }

    val sourceWallet = uiState.wallets.find { it.id == uiState.selectedWalletId }
    val targetWallet = uiState.wallets.find { it.id == uiState.selectedToWalletId }

    val isBalanceInsufficient = sourceWallet != null && uiState.parsedAmount > sourceWallet.balance

    var showConfetti by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            showConfetti = true
            kotlinx.coroutines.delay(1400)
            onTransferSuccess?.invoke()
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateMillis = DateUtils.parseIsoToMillis(uiState.date),
            onDateSelected = { millis ->
                viewModel.onDateChange(DateUtils.formatMillisToIso(millis))
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tx_transfer_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        AppIconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                AppButton(
                    onClick = { viewModel.saveTransaction() },
                    enabled = !uiState.isLoading && uiState.parsedAmount > 0 && !isBalanceInsufficient && uiState.selectedWalletId != null && uiState.selectedToWalletId != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.tx_transfer_btn),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (showConfetti) ConfettiBurst(trigger = true, modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // DUAL WALLET SELECTOR CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .crispBorder(
                        shape = AppShapes.Squircle,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    ),
                shape = AppShapes.Squircle,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Source Wallet Dropdown
                    Text(
                        text = stringResource(R.string.tx_transfer_from),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ExposedDropdownMenuBox(
                        expanded = sourceDropdownExpanded,
                        onExpandedChange = { sourceDropdownExpanded = !sourceDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val selectedText = sourceWallet?.let { "${it.name} (${CurrencyFormatter.formatRupiah(it.balance)})" } ?: stringResource(R.string.tx_transfer_choose_from)
                        OutlinedTextField(
                            value = selectedText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.tx_from_wallet)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sourceDropdownExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = sourceDropdownExpanded,
                            onDismissRequest = { sourceDropdownExpanded = false }
                        ) {
                            uiState.wallets.forEach { wallet ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(wallet.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text(stringResource(R.string.bills_wallet_balance, CurrencyFormatter.formatRupiah(wallet.balance)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        viewModel.onWalletChange(wallet.id ?: "")
                                        sourceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Transfer Icon Arrow Down / Swap Button
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { viewModel.swapWallets() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SwapVert,
                                    contentDescription = stringResource(R.string.tx_transfer_swap),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    // Target Wallet Dropdown
                    Text(
                        text = stringResource(R.string.tx_transfer_to),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    ExposedDropdownMenuBox(
                        expanded = targetDropdownExpanded,
                        onExpandedChange = { targetDropdownExpanded = !targetDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val selectedText = targetWallet?.let { "${it.name} (${CurrencyFormatter.formatRupiah(it.balance)})" } ?: stringResource(R.string.tx_transfer_choose_to)
                        OutlinedTextField(
                            value = selectedText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.tx_to_wallet)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = targetDropdownExpanded) },
                            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = targetDropdownExpanded,
                            onDismissRequest = { targetDropdownExpanded = false }
                        ) {
                            uiState.wallets.forEach { wallet ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(wallet.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text(stringResource(R.string.bills_wallet_balance, CurrencyFormatter.formatRupiah(wallet.balance)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    },
                                    onClick = {
                                        viewModel.onToWalletChange(wallet.id ?: "")
                                        targetDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // AMOUNT INPUT
            Text(
                text = stringResource(R.string.tx_transfer_amount),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedTextField(
                value = uiState.rawAmount,
                onValueChange = { viewModel.onAmountChange(it) },
                label = { Text(stringResource(R.string.tx_transfer_amount_rp)) },
                placeholder = { Text(stringResource(R.string.common_rp_zero)) },
                visualTransformation = CurrencyVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isBalanceInsufficient,
                modifier = Modifier.fillMaxWidth()
            )

            if (isBalanceInsufficient) {
                Text(
                    text = stringResource(R.string.tx_transfer_exceed, CurrencyFormatter.formatRupiah(sourceWallet?.balance ?: 0L)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // DATE & NOTES
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = DateUtils.formatDisplayDate(uiState.date),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.tx_transfer_date)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showDatePicker = true }
                )
            }

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.onNotesChange(it) },
                label = { Text(stringResource(R.string.tx_transfer_notes)) },
                placeholder = { Text(stringResource(R.string.tx_transfer_notes_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            val trError = uiState.validationError?.let {
                when (it) {
                    AddTransactionError.INSUFFICIENT_BALANCE -> stringResource(it.resId, CurrencyFormatter.formatRupiah(sourceWallet?.balance ?: 0L))
                    else -> stringResource(it.resId)
                }
            } ?: uiState.errorMessage
            if (trError != null) {
                Text(text = trError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
        }
    }
}
