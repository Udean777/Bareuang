package com.ssajudn.bareuang.ui.imports

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.utils.CurrencyFormatter

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
                title = { Text("Import Mutasi") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.drafts.isNotEmpty()) {
                val selectedCount = uiState.drafts.count { it.isSelected && !it.isDuplicate }
                val totalAmount = uiState.drafts.filter { it.isSelected && !it.isDuplicate }.sumOf { it.amount }
                Surface(tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("$selectedCount dipilih", style = MaterialTheme.typography.titleSmall)
                            Text(CurrencyFormatter.formatRupiah(totalAmount), style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            enabled = selectedCount > 0 && !uiState.isImporting && uiState.selectedWalletId != null,
                            onClick = { viewModel.importSelected { onNavigateBack() } }
                        ) {
                            if (uiState.isImporting) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Default.Check, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Import")
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wallet selector
            var expanded by remember { mutableStateOf(false) }
            val selectedWallet = uiState.wallets.find { it.id == uiState.selectedWalletId }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedWallet?.name ?: "Pilih dompet",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dompet tujuan") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    uiState.wallets.forEach { w ->
                        DropdownMenuItem(text = { Text(w.name) }, onClick = { viewModel.onWalletSelected(w.id!!); expanded = false })
                    }
                }
            }

            OutlinedButton(
                onClick = { picker.launch(arrayOf("text/csv", "text/comma-separated-values", "text/plain")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (uiState.fileName != null) "Ganti file (${uiState.fileName})" else "Pilih file CSV")
            }
            if (uiState.wallets.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("Belum ada dompet — buat dompet dulu", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (uiState.skippedRows > 0) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Text("${uiState.skippedRows} baris dilewati (format salah)", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }

            if (uiState.isParsing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text("Mem-parsing CSV...", style = MaterialTheme.typography.bodySmall)
            }

            if (uiState.drafts.isEmpty() && !uiState.isParsing) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Format didukung", style = MaterialTheme.typography.titleSmall)
                        Text("• BCA e-statement: Tanggal,Keterangan,Mutasi/Cabang", style = MaterialTheme.typography.bodySmall)
                        Text("• Generic: tanggal, keterangan, jumlah (delimiter , atau ;)", style = MaterialTheme.typography.bodySmall)
                        Text("• Debit/Kredit terpisah juga didukung", style = MaterialTheme.typography.bodySmall)
                        Text("Contoh: 01/01/2026,Top Up GoPay,50000", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else if (uiState.drafts.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${uiState.drafts.size} baris", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { viewModel.selectAll(true) }) { Text("Pilih semua") }
                        TextButton(onClick = { viewModel.selectAll(false) }) { Text("Batal pilih") }
                    }
                }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.drafts, key = { it.id }) { draft ->
                        ImportDraftRow(
                            draft = draft,
                            onToggle = { viewModel.onDraftToggle(draft.id) },
                            onCategoryChange = { viewModel.onDraftCategoryChange(draft.id, it) },
                            onTypeChange = { viewModel.onDraftTypeChange(draft.id, it) }
                        )
                    }
                }
            }
        }
    }
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
            Checkbox(checked = draft.isSelected, onCheckedChange = { onToggle() }, enabled = !draft.isDuplicate)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(draft.merchant, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                Text("${draft.date} • ${CurrencyFormatter.formatRupiah(draft.amount)}", style = MaterialTheme.typography.bodySmall)
                if (draft.isDuplicate) {
                    Text("Duplikat — auto skip", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = draft.type == TransactionType.EXPENSE, onClick = { onTypeChange(TransactionType.EXPENSE) }, label = { Text("Keluar") })
                        FilterChip(selected = draft.type == TransactionType.INCOME, onClick = { onTypeChange(TransactionType.INCOME) }, label = { Text("Masuk") })
                    }
                }
            }
            Box {
                AssistChip(onClick = { catExpanded = true }, label = { Text(draft.category.name) })
                DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                    TransactionCategory.entries.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat.displayName) }, onClick = { onCategoryChange(cat); catExpanded = false })
                    }
                }
            }
        }
    }
}
