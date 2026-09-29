package com.ssajudn.bareuang.ui.imports
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.TopAppBar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.CsvColumnMapping
import com.ssajudn.bareuang.domain.model.CsvRowIssueReason
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.common.labelRes
import com.ssajudn.bareuang.utils.CurrencyFormatter
import com.ssajudn.bareuang.presentation.R
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportMutasiScreen(
    onNavigateBack: () -> Unit,
    viewModel: ImportMutasiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { eff ->
            when (eff) {
                is com.ssajudn.bareuang.ui.common.UiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(eff.message)
                is com.ssajudn.bareuang.ui.common.UiEffect.ShowSnackbarRes -> snackbarHostState.showSnackbar(eff.uiText.asString(context))
                else -> {}
            }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onFilePicked(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_title)) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.summary != null) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        onClick = { viewModel.clearDrafts(); onNavigateBack() },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                    ) { Text(stringResource(R.string.import_done)) }
                }
            } else if (uiState.isMappingColumns && uiState.inspection != null) {
                Surface(tonalElevation = 3.dp) {
                    Button(
                        enabled = uiState.mapping.canParse && !uiState.isParsing,
                        onClick = viewModel::applyColumnMapping,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                    ) {
                        if (uiState.isParsing) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Default.Check, null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.import_preview_button))
                    }
                }
            } else if (uiState.hasParsedPreview) {
                val selectedCount = uiState.drafts.count { it.isSelected }
                val totalAmount = uiState.drafts.filter { it.isSelected }.sumOf { it.amount }
                Surface(tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(pluralStringResource(R.plurals.import_selected_count, selectedCount, selectedCount), style = MaterialTheme.typography.titleSmall)
                            Text(CurrencyFormatter.formatRupiah(totalAmount), style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            enabled = selectedCount > 0 && !uiState.isImporting && uiState.selectedWalletId != null,
                            onClick = viewModel::importSelected
                        ) {
                            if (uiState.isImporting) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.Check, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.import_btn))
                        }
                    }
                }
            }
        }
    ) { padding ->
        when {
            uiState.summary != null -> {
                val summary = uiState.summary!!
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                    Text(stringResource(R.string.import_complete_title), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.import_summary_added, summary.importedCount), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.import_summary_duplicates, summary.duplicateCount), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.import_summary_invalid, summary.invalidCount), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.import_summary_wallet, summary.walletName), style = MaterialTheme.typography.bodyMedium)
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (!uiState.isMappingColumns) {
                        WalletSelector(uiState = uiState, onWalletSelected = viewModel::onWalletSelected)
                        OutlinedButton(
                            onClick = { picker.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain", "text/tab-separated-values")) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (uiState.fileName != null) stringResource(R.string.import_change_file, uiState.fileName!!) else stringResource(R.string.import_pick_file))
                        }
                    }
                    if (uiState.wallets.isEmpty()) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Text(stringResource(R.string.import_wallet_empty_desc), modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (uiState.isParsing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(stringResource(R.string.import_parsing), style = MaterialTheme.typography.bodySmall)
                    }
                    uiState.error?.let { error ->
                        Text(error.asString(context), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (uiState.isMappingColumns && uiState.inspection != null) {
                        ColumnMappingContent(
                            inspection = uiState.inspection!!,
                            mapping = uiState.mapping,
                            onMappingChanged = viewModel::onMappingChanged,
                        )
                    } else if (uiState.hasParsedPreview) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(stringResource(R.string.import_review_title), style = MaterialTheme.typography.titleSmall)
                                Text(stringResource(R.string.import_review_counts, uiState.drafts.count { !it.isDuplicate }, uiState.duplicateCount, uiState.rowIssues.size), style = MaterialTheme.typography.bodySmall)
                            }
                            TextButton(onClick = viewModel::editColumnMapping) { Text(stringResource(R.string.import_edit_mapping)) }
                        }
                        if (uiState.rowIssues.isNotEmpty()) {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(stringResource(R.string.import_skipped_heading, uiState.rowIssues.size), style = MaterialTheme.typography.titleSmall)
                                    uiState.rowIssues.take(4).forEach { issue ->
                                        Text(stringResource(R.string.import_issue_row, issue.rowNumber, stringResource(issue.reason.toStringResource())), style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (uiState.rowIssues.size > 4) Text(stringResource(R.string.import_more_issues, uiState.rowIssues.size - 4), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { viewModel.selectAll(true) }) { Text(stringResource(R.string.import_select_all)) }
                            TextButton(onClick = { viewModel.selectAll(false) }) { Text(stringResource(R.string.import_deselect_all)) }
                        }
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(bottom = 12.dp),
                        ) {
                            items(uiState.drafts, key = { it.id }) { draft ->
                                ImportDraftRow(
                                    draft = draft,
                                    onToggle = { viewModel.onDraftToggle(draft.id) },
                                    onCategoryChange = { viewModel.onDraftCategoryChange(draft.id, it) },
                                    onTypeChange = { viewModel.onDraftTypeChange(draft.id, it) },
                                )
                            }
                            if (uiState.drafts.isEmpty()) {
                                item { Text(stringResource(R.string.import_no_valid_rows), style = MaterialTheme.typography.bodyMedium) }
                            }
                        }
                    } else if (uiState.inspection == null && !uiState.isParsing) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(R.string.import_supported_format), style = MaterialTheme.typography.titleSmall)
                                Text(stringResource(R.string.import_format_bca), style = MaterialTheme.typography.bodySmall)
                                Text(stringResource(R.string.import_format_generic), style = MaterialTheme.typography.bodySmall)
                                Text(stringResource(R.string.import_format_debit_credit), style = MaterialTheme.typography.bodySmall)
                                Text(stringResource(R.string.import_format_example), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    // Soft daily-budget nudge: confirm before importing drafts over today's allowance.
    if (uiState.pendingDailyOverride) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDailyOverrideImport() },
            title = { Text(stringResource(R.string.tx_error_daily_exceeded_title)) },
            text = { Text(stringResource(R.string.import_daily_override_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDailyOverrideImport) {
                    Text(stringResource(R.string.import_daily_override_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDailyOverrideImport() }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun WalletSelector(
    uiState: ImportUiState,
    onWalletSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedWallet = uiState.wallets.find { it.id == uiState.selectedWalletId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedWallet?.name ?: stringResource(R.string.import_wallet_label),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.import_wallet_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            uiState.wallets.forEach { wallet ->
                wallet.id?.let { id ->
                    DropdownMenuItem(text = { Text(wallet.name) }, onClick = { onWalletSelected(id); expanded = false })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnMappingContent(
    inspection: com.ssajudn.bareuang.domain.model.CsvImportInspection,
    mapping: CsvColumnMapping,
    onMappingChanged: (CsvColumnMapping) -> Unit,
) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.import_mapping_title), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.import_mapping_description), style = MaterialTheme.typography.bodySmall)
        ColumnSelector(stringResource(R.string.import_mapping_date), inspection.columns, mapping.date) {
            onMappingChanged(mapping.copy(date = it))
        }
        ColumnSelector(stringResource(R.string.import_mapping_description_column), inspection.columns, mapping.description) {
            onMappingChanged(mapping.copy(description = it))
        }
        ColumnSelector(stringResource(R.string.import_mapping_amount), inspection.columns, mapping.amount) {
            onMappingChanged(mapping.copy(amount = it, debit = if (it != null) null else mapping.debit, credit = if (it != null) null else mapping.credit))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ColumnSelector(stringResource(R.string.import_mapping_debit), inspection.columns, mapping.debit, Modifier.weight(1f)) {
                onMappingChanged(mapping.copy(debit = it, amount = if (it != null) null else mapping.amount))
            }
            ColumnSelector(stringResource(R.string.import_mapping_credit), inspection.columns, mapping.credit, Modifier.weight(1f)) {
                onMappingChanged(mapping.copy(credit = it, amount = if (it != null) null else mapping.amount))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ColumnSelector(stringResource(R.string.import_mapping_type), inspection.columns, mapping.type, Modifier.weight(1f)) {
                onMappingChanged(mapping.copy(type = it))
            }
            ColumnSelector(stringResource(R.string.import_mapping_category), inspection.columns, mapping.category, Modifier.weight(1f)) {
                onMappingChanged(mapping.copy(category = it))
            }
        }
        if (!mapping.canParse) Text(stringResource(R.string.import_mapping_required), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        Text(stringResource(R.string.import_mapping_sample), style = MaterialTheme.typography.titleSmall)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
            items(inspection.sampleRows) { row ->
                Card {
                    Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        inspection.columns.take(4).forEachIndexed { index, title ->
                            Text("$title: ${row.getOrNull(index).orEmpty().ifBlank { "—" }}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnSelector(
    label: String,
    columns: List<String>,
    selectedIndex: Int?,
    modifier: Modifier = Modifier,
    onSelected: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = selectedIndex?.let { index -> columns.getOrNull(index)?.let { "${index + 1}. $it" } }
        ?: stringResource(R.string.import_mapping_not_selected)
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, maxLines = 1) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            singleLine = true,
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.import_mapping_not_selected)) }, onClick = { onSelected(null); expanded = false })
            columns.forEachIndexed { index, column ->
                DropdownMenuItem(text = { Text("${index + 1}. $column", maxLines = 1) }, onClick = { onSelected(index); expanded = false })
            }
        }
    }
}

private fun CsvRowIssueReason.toStringResource(): Int = when (this) {
    CsvRowIssueReason.INVALID_DATE -> R.string.import_issue_invalid_date
    CsvRowIssueReason.MISSING_DESCRIPTION -> R.string.import_issue_missing_description
    CsvRowIssueReason.INVALID_AMOUNT -> R.string.import_issue_invalid_amount
    CsvRowIssueReason.AMOUNT_CONFLICT -> R.string.import_issue_amount_conflict
    CsvRowIssueReason.BALANCE_SUMMARY -> R.string.import_issue_balance_summary
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportDraftRow(
    draft: com.ssajudn.bareuang.domain.model.ImportDraft,
    onToggle: () -> Unit,
    onCategoryChange: (TransactionCategory) -> Unit,
    onTypeChange: (TransactionType) -> Unit
) {
    var catExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (draft.isDuplicate) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = draft.isSelected, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(draft.merchant, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text("${draft.date} • ${CurrencyFormatter.formatRupiah(draft.amount)}", style = MaterialTheme.typography.bodySmall)
                if (draft.isDuplicate) {
                    Text(stringResource(R.string.import_duplicate_skip), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = draft.type == TransactionType.EXPENSE, onClick = { onTypeChange(TransactionType.EXPENSE) }, label = { Text(stringResource(R.string.import_chip_expense)) })
                    FilterChip(selected = draft.type == TransactionType.INCOME, onClick = { onTypeChange(TransactionType.INCOME) }, label = { Text(stringResource(R.string.import_chip_income)) })
                }
            }
            Box {
                AssistChip(onClick = { catExpanded = true }, label = { Text(draft.category.name) })
                DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                    TransactionCategory.entries.forEach { cat ->
                        DropdownMenuItem(text = { Text(stringResource(cat.labelRes())) }, onClick = { onCategoryChange(cat); catExpanded = false })
                    }
                }
            }
        }
    }
}
