package com.ssajudn.bareuang.ui.imports

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.port.CsvParserPort
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.model.CsvColumnMapping
import com.ssajudn.bareuang.domain.model.CsvImportInspection
import com.ssajudn.bareuang.domain.model.CsvImportSummary
import com.ssajudn.bareuang.domain.model.CsvRowIssue
import com.ssajudn.bareuang.domain.model.ImportDraft
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.usecase.BulkCreateTransactionsUseCase
import com.ssajudn.bareuang.domain.usecase.ParseMutasiCsvUseCase
import com.ssajudn.bareuang.domain.port.ImportPreferencesPort
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.ui.common.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImportUiState(
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: String? = null,
    val drafts: List<ImportDraft> = emptyList(),
    val fileName: String? = null,
    val isParsing: Boolean = false,
    val isImporting: Boolean = false,
    val skippedRows: Int = 0,
    val csvText: String? = null,
    val inspection: CsvImportInspection? = null,
    val mapping: CsvColumnMapping = CsvColumnMapping(),
    val isMappingColumns: Boolean = false,
    val hasParsedPreview: Boolean = false,
    val rowIssues: List<CsvRowIssue> = emptyList(),
    val duplicateCount: Int = 0,
    val summary: CsvImportSummary? = null,
    val error: UiText? = null,
    // Soft daily-budget nudge: some selected drafts exceed today's allowance.
    val pendingDailyOverride: Boolean = false
)

@HiltViewModel
class ImportMutasiViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val walletRepository: WalletRepository,
    private val csvParser: CsvParserPort,
    private val parseMutasiCsvUseCase: ParseMutasiCsvUseCase,
    private val bulkCreate: BulkCreateTransactionsUseCase,
    private val importPrefs: ImportPreferencesPort
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    private val _operation = MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: StateFlow<OperationState> = _operation.asStateFlow()

    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val wallets = walletRepository.getWallets().getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(
                wallets = wallets,
                selectedWalletId = wallets.firstOrNull()?.id
            )
            walletRepository.observeWallets().collect { list ->
                _uiState.value = _uiState.value.copy(wallets = list)
            }
        }
    }

    fun onWalletSelected(id: String) {
        _uiState.value = _uiState.value.copy(selectedWalletId = id)
    }

    fun onDraftToggle(id: String) {
        _uiState.value = _uiState.value.copy(
            drafts = _uiState.value.drafts.map { if (it.id == id) it.copy(isSelected = !it.isSelected) else it }
        )
    }

    fun onDraftCategoryChange(id: String, category: TransactionCategory) {
        _uiState.value = _uiState.value.copy(drafts = _uiState.value.drafts.map { if (it.id == id) it.copy(category = category) else it })
    }

    fun onDraftTypeChange(id: String, type: TransactionType) {
        _uiState.value = _uiState.value.copy(drafts = _uiState.value.drafts.map { if (it.id == id) it.copy(type = type) else it })
    }

    fun selectAll(select: Boolean) {
        _uiState.value = _uiState.value.copy(
            drafts = _uiState.value.drafts.map { if (it.isDuplicate) it else it.copy(isSelected = select) }
        )
    }

    fun onMappingChanged(mapping: CsvColumnMapping) {
        _uiState.value = _uiState.value.copy(mapping = mapping)
    }

    fun editColumnMapping() {
        if (_uiState.value.inspection != null) _uiState.value = _uiState.value.copy(isMappingColumns = true)
    }

    fun applyColumnMapping() {
        val current = _uiState.value
        val csvText = current.csvText ?: return
        if (!current.mapping.canParse) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isParsing = true, error = null)
            try {
                val parsed = csvParser.parse(csvText, current.mapping)
                val marked = parseMutasiCsvUseCase.markDuplicates(parsed.drafts, parsed.issues.size).getOrElse { throw it }
                _uiState.value = _uiState.value.copy(
                    drafts = marked.drafts,
                    rowIssues = parsed.issues,
                    skippedRows = parsed.issues.size,
                    duplicateCount = marked.duplicateCount,
                    isParsing = false,
                    hasParsedPreview = true,
                    isMappingColumns = false,
                )
                _operation.value = OperationState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("Import", "applyColumnMapping failed", e)
                val ui = UiText.Res(com.ssajudn.bareuang.presentation.R.string.import_error_read)
                _uiState.value = _uiState.value.copy(isParsing = false, error = ui)
                _operation.value = OperationState.Error("", ui)
            }
        }
    }

    fun onFilePicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                drafts = emptyList(),
                fileName = null,
                isParsing = true,
                csvText = null,
                inspection = null,
                mapping = CsvColumnMapping(),
                isMappingColumns = false,
                hasParsedPreview = false,
                rowIssues = emptyList(),
                skippedRows = 0,
                duplicateCount = 0,
                summary = null,
                error = null,
            )
            _operation.value = OperationState.Loading
            try {
                // file size guard 5MB
                val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                val size = pfd?.statSize ?: -1L
                pfd?.close()
                if (size > 5 * 1024 * 1024) {
                    val ui = UiText.Res(com.ssajudn.bareuang.presentation.R.string.import_file_too_large)
                    _uiState.value = _uiState.value.copy(isParsing = false, error = ui)
                    _operation.value = OperationState.Error("", ui)
                    _effect.send(UiEffect.ShowSnackbarRes(ui))
                    return@launch
                }
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                // DocumentFile display name
                var fileName = "mutasi.csv"
                context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                    val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (c.moveToFirst() && idx >= 0) fileName = c.getString(idx) ?: fileName
                }
                if (fileName.isBlank()) fileName = uri.lastPathSegment ?: "mutasi.csv"
                val inspection = csvParser.inspect(text)
                android.util.Log.d("Import", "inspected ${inspection.dataRowCount} rows")
                if (inspection.error == com.ssajudn.bareuang.domain.model.CsvImportInspectionError.TOO_MANY_ROWS) {
                    val ui = UiText.Res(com.ssajudn.bareuang.presentation.R.string.import_too_many_rows)
                    _uiState.value = _uiState.value.copy(isParsing = false, fileName = fileName, error = ui)
                    _operation.value = OperationState.Error("", ui)
                    _effect.send(UiEffect.ShowSnackbarRes(ui))
                    return@launch
                }
                if (inspection.dataRowCount == 0) {
                    val ui = UiText.Res(com.ssajudn.bareuang.presentation.R.string.import_empty_csv)
                    _uiState.value = _uiState.value.copy(isParsing = false, fileName = fileName, error = ui)
                    _operation.value = OperationState.Error("", ui)
                    _effect.send(UiEffect.ShowSnackbarRes(ui))
                    return@launch
                }
                _uiState.value = _uiState.value.copy(
                    drafts = emptyList(),
                    fileName = fileName,
                    csvText = text,
                    inspection = inspection,
                    mapping = inspection.autoMapping,
                    isMappingColumns = inspection.needsMapping,
                    isParsing = false,
                    hasParsedPreview = false,
                    rowIssues = emptyList(),
                    skippedRows = 0,
                    duplicateCount = 0,
                    summary = null,
                )
                _operation.value = OperationState.Idle
                if (!inspection.needsMapping) applyColumnMapping()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("Import", "onFilePicked failed", e)
                val ui = UiText.Res(com.ssajudn.bareuang.presentation.R.string.import_error_read)
                _uiState.value = _uiState.value.copy(isParsing = false, error = ui)
                _operation.value = OperationState.Error("", ui)
                _effect.send(UiEffect.ShowSnackbarRes(ui))
            }
        }
    }

    fun importSelected() {
        val walletId = _uiState.value.selectedWalletId
        if (walletId.isNullOrBlank()) {
            viewModelScope.launch { _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_wallet_required))) }
            return
        }
        doImport(force = false)
    }

    /** Proceed after the user accepts the daily-budget override prompt for a batch. */
    fun confirmDailyOverrideImport() {
        _uiState.value = _uiState.value.copy(pendingDailyOverride = false)
        doImport(force = true)
    }

    /** Cancel a daily-budget override prompt for a batch. */
    fun dismissDailyOverrideImport() {
        _uiState.value = _uiState.value.copy(pendingDailyOverride = false, isImporting = false)
        _operation.value = OperationState.Idle
    }

    private fun doImport(force: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true)
            _operation.value = OperationState.Loading
            val walletId = _uiState.value.selectedWalletId
            if (walletId.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(isImporting = false)
                _operation.value = OperationState.Idle
                return@launch
            }
            val stateBeforeImport = _uiState.value
            val walletName = stateBeforeImport.wallets.firstOrNull { it.id == walletId }?.name.orEmpty()
            val duplicatesSkipped = stateBeforeImport.drafts.count { it.isDuplicate && !it.isSelected }
            val res = bulkCreate(
                _uiState.value.drafts,
                walletId,
                force
            )
            res.onSuccess { count ->
                importPrefs.increment(count)
                android.util.Log.d("Import", "import success $count, total ${importPrefs.importCount.value}")
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    drafts = emptyList(),
                    summary = CsvImportSummary(
                        importedCount = count,
                        duplicateCount = duplicatesSkipped,
                        invalidCount = stateBeforeImport.rowIssues.size,
                        walletName = walletName,
                    ),
                )
                _operation.value = OperationState.Success("$count transaksi diimpor")
            }.onFailure { e ->
                android.util.Log.e("Import", "import failed", e)
                if (e is AppException.DailyBudgetExceededException) {
                    // Soft nudge: ask before importing drafts that exceed today's allowance.
                    _uiState.value = _uiState.value.copy(isImporting = false, pendingDailyOverride = true)
                    return@launch
                }
                val ui = (e as? AppException)?.toUiText()
                    ?: UiText.Res(com.ssajudn.bareuang.presentation.R.string.import_error_save)
                _uiState.value = _uiState.value.copy(isImporting = false)
                _operation.value = OperationState.Error("", ui)
                _effect.send(UiEffect.ShowSnackbarRes(ui))
            }
        }
    }

    fun clearDrafts() {
        _uiState.value = ImportUiState(
            wallets = _uiState.value.wallets,
            selectedWalletId = _uiState.value.selectedWalletId,
        )
    }
}
