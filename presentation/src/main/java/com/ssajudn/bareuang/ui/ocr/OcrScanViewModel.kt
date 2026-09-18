package com.ssajudn.bareuang.ui.ocr

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.port.ReceiptOcrPort
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.usecase.CheckDailyBudgetUseCase
import com.ssajudn.bareuang.domain.usecase.HasMonthlyBudgetUseCase
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.domain.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OcrUiState(
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: String? = null,
    val isProcessing: Boolean = false,
    val rawText: String? = null,
    // editable fields
    val merchant: String = "",
    val amount: String = "", // digits only
    val parsedAmount: Long = 0L,
    val category: TransactionCategory = TransactionCategory.SHOPPING,
    val date: String = DateUtils.getCurrentDateISO(),
    val isSaving: Boolean = false,
    val pendingDailyOverride: Boolean = false,
    val pendingDailyMessage: String? = null,
    val isOcrAvailable: Boolean = false,
    val selectedImageUri: Uri? = null,
    val showImagePreview: Boolean = false,
    val isManualEntry: Boolean = false,
    val ocrError: Boolean = false,
)

@HiltViewModel
class OcrScanViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val receiptOcr: ReceiptOcrPort,
    private val hasMonthlyBudget: HasMonthlyBudgetUseCase,
    private val checkDailyBudget: CheckDailyBudgetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OcrUiState(
            isOcrAvailable = receiptOcr.isAvailable,
        )
    )
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var ocrJob: Job? = null

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

    fun onWalletSelected(id: String) { _uiState.value = _uiState.value.copy(selectedWalletId = id) }
    fun onMerchantChange(v: String) { _uiState.value = _uiState.value.copy(merchant = v.take(100)) }
    fun onCategoryChange(c: TransactionCategory) { _uiState.value = _uiState.value.copy(category = c) }
    fun onDateChange(d: String) { _uiState.value = _uiState.value.copy(date = d) }
    fun onAmountChange(input: String) {
        val digits = input.filter { it.isDigit() }.take(12)
        _uiState.value = _uiState.value.copy(amount = digits, parsedAmount = digits.toLongOrNull() ?: 0L)
    }

    fun selectImage(uri: Uri) {
        ocrJob?.cancel()
        _uiState.value = _uiState.value.copy(
            selectedImageUri = uri,
            showImagePreview = true,
            isProcessing = false,
            ocrError = false,
            rawText = null,
            isManualEntry = false,
        )
    }

    fun clearSelectedImage() {
        ocrJob?.cancel()
        _uiState.value = _uiState.value.copy(
            selectedImageUri = null,
            showImagePreview = false,
            isProcessing = false,
            ocrError = false,
        )
    }

    fun processSelectedImage() {
        val uri = _uiState.value.selectedImageUri ?: return
        ocrJob?.cancel()
        ocrJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                showImagePreview = true,
                ocrError = false,
                rawText = null,
                isManualEntry = false,
            )
            val result = receiptOcr.parseReceiptImage(uri.toString())
            if (!isActive || _uiState.value.selectedImageUri != uri) return@launch
            result.onSuccess { parsed ->
                if (!isActive || _uiState.value.selectedImageUri != uri) return@onSuccess
                if (parsed.rawText.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        showImagePreview = true,
                        ocrError = true,
                    )
                    return@onSuccess
                }
                val parsedDate = parsed.date?.takeIf {
                    runCatching { java.time.LocalDate.parse(it) }.isSuccess
                } ?: _uiState.value.date
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    showImagePreview = false,
                    ocrError = false,
                    rawText = parsed.rawText,
                    merchant = parsed.merchantName,
                    amount = if (parsed.totalAmount > 0) parsed.totalAmount.toString() else "",
                    parsedAmount = parsed.totalAmount,
                    category = parsed.suggestedCategory,
                    date = parsedDate,
                )
            }.onFailure { e ->
                if (!isActive || _uiState.value.selectedImageUri != uri) return@onFailure
                android.util.Log.e("Ocr", "OCR failed", e)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    showImagePreview = true,
                    ocrError = true,
                )
            }
        }
    }

    fun retryOcr() {
        processSelectedImage()
    }

    fun startManualEntry() {
        ocrJob?.cancel()
        _uiState.value = _uiState.value.copy(
            rawText = "",
            merchant = "",
            amount = "",
            parsedAmount = 0L,
            category = TransactionCategory.SHOPPING,
            date = DateUtils.getCurrentDateISO(),
            selectedImageUri = null,
            showImagePreview = false,
            isManualEntry = true,
            ocrError = false,
        )
    }

    fun save(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (s.selectedWalletId.isNullOrBlank()) {
            viewModelScope.launch { _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_wallet_required))) }; return
        }
        if (s.parsedAmount <= 0) {
            viewModelScope.launch { _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_invalid_amount))) }; return
        }
        viewModelScope.launch {
            if (!hasMonthlyBudget()) {
                _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_budget_required)))
                return@launch
            }
            val dailyCheck = checkDailyBudget(s.parsedAmount, s.date, com.ssajudn.bareuang.utils.CurrencyFormatter.getActiveCurrency(), s.category)
            if (dailyCheck.isFailure) {
                val msg = dailyCheck.exceptionOrNull()?.message ?: ""
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    pendingDailyOverride = true,
                    pendingDailyMessage = msg.ifBlank { null }
                )
                return@launch
            }
            performCreate(onSuccess)
        }
    }

    fun confirmDailyOverride(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(pendingDailyOverride = false, pendingDailyMessage = null)
        viewModelScope.launch { performCreate(onSuccess) }
    }

    fun dismissDailyOverride() {
        _uiState.value = _uiState.value.copy(pendingDailyOverride = false, pendingDailyMessage = null, isSaving = false)
    }

    private suspend fun performCreate(onSuccess: () -> Unit) {
        val s = _uiState.value
        val wallet = walletRepository.getWallets().getOrNull()?.find { it.id == s.selectedWalletId }
        if (wallet != null && wallet.balance < s.parsedAmount) {
            _uiState.value = _uiState.value.copy(isSaving = false)
            _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_insufficient_balance, listOf(com.ssajudn.bareuang.utils.CurrencyFormatter.formatRupiah(wallet.balance)))))
            return
        }
        _uiState.value = _uiState.value.copy(isSaving = true)
        val req = CreateTransactionRequest(
            amount = s.parsedAmount,
            type = TransactionType.EXPENSE,
            category = s.category,
            merchant = s.merchant.ifBlank { s.category.name },
            date = s.date,
            walletId = s.selectedWalletId
        )
        val res = transactionRepository.createTransaction(req)
        res.onSuccess {
            _uiState.value = _uiState.value.copy(isSaving = false)
            _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.ocr_save_success)))
            onSuccess()
        }.onFailure { e ->
            android.util.Log.e("Ocr", "save failed", e)
            _uiState.value = _uiState.value.copy(isSaving = false)
            _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.ocr_error_save)))
        }
    }

    fun reset() {
        ocrJob?.cancel()
        _uiState.value = _uiState.value.copy(
            rawText = null,
            merchant = "",
            amount = "",
            parsedAmount = 0L,
            selectedImageUri = null,
            showImagePreview = false,
            isManualEntry = false,
            ocrError = false,
        )
    }
}
