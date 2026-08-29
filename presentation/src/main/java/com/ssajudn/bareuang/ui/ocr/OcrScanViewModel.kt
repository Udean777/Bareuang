package com.ssajudn.bareuang.ui.ocr

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.data.service.OcrService
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
import com.ssajudn.bareuang.utils.DateUtils
import com.ssajudn.bareuang.utils.ParsedReceipt
import com.ssajudn.bareuang.utils.ReceiptParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OcrUiState(
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: String? = null,
    val isProcessing: Boolean = false,
    val rawText: String? = null,
    val parsed: ParsedReceipt? = null,
    // editable fields
    val merchant: String = "",
    val amount: String = "", // digits only
    val parsedAmount: Long = 0L,
    val category: TransactionCategory = TransactionCategory.SHOPPING,
    val date: String = DateUtils.getCurrentDateISO(),
    val isSaving: Boolean = false
)

@HiltViewModel
class OcrScanViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
    private val ocrService: OcrService,
    private val hasMonthlyBudget: HasMonthlyBudgetUseCase,
    private val checkDailyBudget: CheckDailyBudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

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

    fun onWalletSelected(id: String) { _uiState.value = _uiState.value.copy(selectedWalletId = id) }
    fun onMerchantChange(v: String) { _uiState.value = _uiState.value.copy(merchant = v) }
    fun onCategoryChange(c: TransactionCategory) { _uiState.value = _uiState.value.copy(category = c) }
    fun onDateChange(d: String) { _uiState.value = _uiState.value.copy(date = d) }
    fun onAmountChange(input: String) {
        val digits = input.filter { it.isDigit() }.take(12)
        _uiState.value = _uiState.value.copy(amount = digits, parsedAmount = digits.toLongOrNull() ?: 0L)
    }

    fun processImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, rawText = null, parsed = null)
            val result = ocrService.recognizeFromUri(uri)
            result.onSuccess { text ->
                android.util.Log.d("Ocr", "recognized ${text.length} chars")
                if (text.isBlank()) {
                    _uiState.value = _uiState.value.copy(isProcessing = false)
                    android.util.Log.w("Ocr", "empty text")
                    _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.ocr_no_text)))
                    return@launch
                }
                val parsed = ReceiptParser.parse(text)
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    rawText = text,
                    parsed = parsed,
                    merchant = parsed.merchantName,
                    amount = if (parsed.totalAmount > 0) parsed.totalAmount.toString() else "",
                    parsedAmount = parsed.totalAmount,
                    category = parsed.suggestedCategory
                )
            }.onFailure { e ->
                android.util.Log.e("Ocr", "recognize failed", e)
                _uiState.value = _uiState.value.copy(isProcessing = false)
                _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.ocr_error_generic)))
            }
        }
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
            val dailyCheck = checkDailyBudget(s.parsedAmount, s.date, com.ssajudn.bareuang.utils.CurrencyFormatter.getActiveCurrency())
            if (dailyCheck.isFailure) {
                val msg = dailyCheck.exceptionOrNull()?.message ?: ""
                _effect.send(UiEffect.ShowSnackbarRes(if (msg.isNotBlank()) UiText.Dyn(msg) else UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_daily_exceeded)))
                return@launch
            }
            val wallet = walletRepository.getWallets().getOrNull()?.find { it.id == s.selectedWalletId }
            if (wallet != null && wallet.balance < s.parsedAmount) {
                _effect.send(UiEffect.ShowSnackbarRes(UiText.Res(com.ssajudn.bareuang.presentation.R.string.tx_error_insufficient_balance, listOf(com.ssajudn.bareuang.utils.CurrencyFormatter.formatRupiah(wallet.balance)))))
                return@launch
            }
            _uiState.value = _uiState.value.copy(isSaving = true)
            val req = CreateTransactionRequest(
                amount = s.parsedAmount,
                type = TransactionType.EXPENSE,
                category = s.category,
                merchant = s.merchant.ifBlank { s.category.displayName },
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
    }

    fun onRawTextEdited(newText: String) {
        val parsed = ReceiptParser.parse(newText)
        _uiState.value = _uiState.value.copy(
            rawText = newText,
            parsed = parsed,
            merchant = parsed.merchantName.ifBlank { _uiState.value.merchant },
            // only override amount if parser found valid amount and current amount was auto
            parsedAmount = if (parsed.totalAmount > 0) parsed.totalAmount else _uiState.value.parsedAmount,
            amount = if (parsed.totalAmount > 0) parsed.totalAmount.toString() else _uiState.value.amount,
            category = parsed.suggestedCategory
        )
    }

    fun reset() {
        _uiState.value = _uiState.value.copy(rawText = null, parsed = null, merchant = "", amount = "", parsedAmount = 0L)
    }
}
