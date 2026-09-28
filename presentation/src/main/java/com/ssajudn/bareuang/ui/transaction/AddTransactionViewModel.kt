package com.ssajudn.bareuang.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionEntryTemplate
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.usecase.SaveTransactionEntryUseCase
import com.ssajudn.bareuang.domain.usecase.CheckTransactionEntryUseCase
import com.ssajudn.bareuang.domain.usecase.TransactionEntryCheck
import com.ssajudn.bareuang.domain.usecase.TransactionEntryPreferencesUseCase
import com.ssajudn.bareuang.domain.utils.DateUtils
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.utils.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.ssajudn.bareuang.ui.common.OperationState
import com.ssajudn.bareuang.ui.common.UiEffect
import com.ssajudn.bareuang.ui.common.UiText
import com.ssajudn.bareuang.ui.common.toUiText
import javax.inject.Inject

data class AddTransactionUiState(
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: String? = null,
    val selectedToWalletId: String? = null,
    val rawAmount: String = "",
    val parsedAmount: Long = 0L,
    val merchant: String = "",
    val selectedCategory: TransactionCategory = TransactionCategory.FOOD,
    val date: String = DateUtils.getCurrentDateISO(),
    val notes: String = "",
    val isRecurring: Boolean = false,
    val recurringInterval: com.ssajudn.bareuang.domain.model.RecurringInterval = com.ssajudn.bareuang.domain.model.RecurringInterval.MONTHLY,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val validationError: AddTransactionError? = null,
    val isSuccess: Boolean = false,
    val isBudgetMissing: Boolean = false,
    val categoryBudgets: List<com.ssajudn.bareuang.domain.model.CategoryBudget> = emptyList(),
    val favoriteTemplates: List<TransactionEntryTemplate> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val recurringIntervalsById: Map<String, RecurringInterval> = emptyMap(),
    val saveAsFavorite: Boolean = false,
    val includeFavoriteAmount: Boolean = false,
    // Soft daily-budget nudge: when today's allowance is exceeded we prompt the
    // user instead of silently blocking, letting them choose to save anyway.
    val pendingDailyOverride: Boolean = false,
    val pendingDailyMessage: UiText? = null
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val saveEntry: SaveTransactionEntryUseCase,
    private val checkTransactionEntry: CheckTransactionEntryUseCase,
    private val transactionEntryData: TransactionEntryDataCoordinator,
    private val entryPreferences: TransactionEntryPreferencesUseCase,
) : ViewModel() {
    private val _operation =
        kotlinx.coroutines.flow.MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: kotlinx.coroutines.flow.StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()


    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()
    init {
        _uiState.value = _uiState.value.copy(
            selectedCategory = entryPreferences.preferredCategory(TransactionType.EXPENSE),
            favoriteTemplates = entryPreferences.favoriteTemplates(),
        )
        observeTransactionEntryData()
    }

    private fun observeTransactionEntryData() {
        viewModelScope.launch {
            transactionEntryData.observe().collect { update ->
                when (update) {
                    is TransactionEntryDataUpdate.Wallets -> updateWallets(update.value)
                    is TransactionEntryDataUpdate.CategoryBudgets -> {
                        _uiState.value = _uiState.value.copy(categoryBudgets = update.value)
                    }
                    is TransactionEntryDataUpdate.RecentHistory -> {
                        _uiState.value = _uiState.value.copy(
                            recentTransactions = update.transactions,
                            recurringIntervalsById = update.recurringIntervalsById,
                        )
                    }
                    is TransactionEntryDataUpdate.MonthlyBudgetStatus -> {
                        _uiState.value = _uiState.value.copy(isBudgetMissing = update.isMissing)
                    }
                    is TransactionEntryDataUpdate.Failure -> showOperationFailure(update.cause)
                }
            }
        }
    }

    private fun updateWallets(wallets: List<Wallet>) {
        if (wallets.isEmpty()) return

        val current = _uiState.value
        val defaultWallet =
            if (wallets.any { it.id == current.selectedWalletId }) current.selectedWalletId
            else entryPreferences.preferredWallet(wallets, current.transactionType)
        val defaultToWallet =
            if (wallets.any { it.id == current.selectedToWalletId && it.id != defaultWallet }) {
                current.selectedToWalletId
            } else {
                entryPreferences.preferredDestinationWallet(wallets, defaultWallet)
            }
        _uiState.value = current.copy(
            wallets = wallets,
            selectedWalletId = defaultWallet,
            selectedToWalletId = defaultToWallet,
        )
    }

    fun onTransactionTypeChange(type: TransactionType) {
        val currentState = _uiState.value
        val selectedWalletId = entryPreferences.preferredWallet(currentState.wallets, type)
        val targetWalletId = entryPreferences.preferredDestinationWallet(currentState.wallets, selectedWalletId)
        val newCategory = entryPreferences.preferredCategory(type)

        _uiState.value = TransactionEntryFormReducer.transactionType(
            state = currentState,
            type = type,
            walletId = selectedWalletId,
            destinationWalletId = targetWalletId,
            category = newCategory,
        )
    }

    fun onWalletChange(walletId: String) = reduceForm {
        TransactionEntryFormReducer.selectWallet(it, walletId)
    }

    fun onToWalletChange(walletId: String) = reduceForm {
        TransactionEntryFormReducer.selectDestinationWallet(it, walletId)
    }

    fun swapWallets() = reduceForm { TransactionEntryFormReducer.swapWallets(it) }

    fun onAmountChange(input: String) = reduceForm {
        TransactionEntryFormReducer.amount(it, input)
    }

    fun onMerchantChange(merchant: String) = reduceForm {
        TransactionEntryFormReducer.merchant(it, merchant)
    }

    fun onCategoryChange(category: TransactionCategory) = reduceForm {
        TransactionEntryFormReducer.category(it, category)
    }

    fun onDateChange(date: String) = reduceForm { TransactionEntryFormReducer.date(it, date) }

    fun onNotesChange(notes: String) = reduceForm {
        TransactionEntryFormReducer.notes(it, notes)
    }

    fun onRecurringChange(isRecurring: Boolean) = reduceForm {
        TransactionEntryFormReducer.recurring(it, isRecurring)
    }

    fun onRecurringIntervalChange(interval: RecurringInterval) = reduceForm {
        TransactionEntryFormReducer.recurringInterval(it, interval)
    }

    fun onSaveAsFavoriteChange(enabled: Boolean) = reduceForm {
        TransactionEntryFormReducer.saveAsFavorite(it, enabled)
    }

    fun onIncludeFavoriteAmountChange(enabled: Boolean) = reduceForm {
        TransactionEntryFormReducer.includeFavoriteAmount(it, enabled)
    }

    private inline fun reduceForm(reducer: (AddTransactionUiState) -> AddTransactionUiState) {
        _uiState.value = reducer(_uiState.value)
    }

    fun onUseFavoriteTemplate(template: TransactionEntryTemplate) {
        val current = _uiState.value
        val selection = TransactionEntryFormMapper.fromFavorite(current, template, entryPreferences)
        _uiState.value = TransactionEntryFormMapper.applySelection(current, selection)
    }

    fun onUseRecentTransaction(transaction: Transaction) {
        val current = _uiState.value
        val selection = TransactionEntryFormMapper.fromRecent(current, transaction, entryPreferences)
        _uiState.value = TransactionEntryFormMapper.applySelection(current, selection)
    }

    fun deleteFavoriteTemplate(templateId: String) {
        entryPreferences.deleteFavoriteTemplate(templateId)
        _uiState.value = _uiState.value.copy(
            favoriteTemplates = _uiState.value.favoriteTemplates.filterNot { it.id == templateId }
        )
    }

    fun saveTransaction() {
        val state = _uiState.value
        if (state.isLoading || _operation.value is OperationState.Loading) return

        _uiState.value = state.copy(isLoading = true, errorMessage = null, validationError = null)
        _operation.value = OperationState.Loading

        viewModelScope.launch {
            val check = checkTransactionEntry(
                type = state.transactionType,
                amount = state.parsedAmount,
                date = state.date,
                category = state.selectedCategory,
                sourceWalletId = state.selectedWalletId,
                targetWalletId = state.selectedToWalletId,
                wallets = state.wallets,
            )
            val result = check.getOrElse { error ->
                showOperationFailure(error)
                return@launch
            }
            when (result) {
                is TransactionEntryCheck.Rejected -> {
                    showValidationFailure(result.reason, state)
                    return@launch
                }
                is TransactionEntryCheck.RequiresDailyBudgetOverride -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null,
                        pendingDailyOverride = true,
                        pendingDailyMessage = result.error.toUiText(),
                    )
                    return@launch
                }
                TransactionEntryCheck.Ready -> Unit
            }

            performCreate()
        }
    }

    private fun showValidationFailure(
        reason: com.ssajudn.bareuang.domain.error.TransactionValidationReason,
        state: AddTransactionUiState,
    ) {
        val error = reason.toUiError()
        val sourceBalance = state.wallets.firstOrNull { it.id == state.selectedWalletId }?.balance
        val ui = error.toUiText(
            if (error == AddTransactionError.INSUFFICIENT_BALANCE) {
                sourceBalance?.let(CurrencyFormatter::formatRupiah)
            } else {
                null
            }
        )
        _uiState.value = state.copy(
            isLoading = false,
            errorMessage = null,
            validationError = error,
        )
        _operation.value = OperationState.Error("", ui)
        _effect.trySend(UiEffect.ShowSnackbarRes(ui))
    }

    private fun showOperationFailure(error: Throwable) {
        val ui = (error as? AppException)?.toUiText()
            ?: UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_generic)
        _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
        _operation.value = OperationState.Error("", ui)
        _effect.trySend(UiEffect.ShowSnackbarRes(ui))
    }

    /** Proceed after the user accepts a daily-budget override prompt. */
    fun confirmDailyOverride() {
        val current = _uiState.value
        if (current.isLoading) return
        _uiState.value = current.copy(
            pendingDailyOverride = false,
            pendingDailyMessage = null,
            isLoading = true,
        )
        _operation.value = OperationState.Loading
        viewModelScope.launch { performCreate() }
    }

    /** Cancel a daily-budget override prompt. */
    fun dismissDailyOverride() {
        _uiState.value = _uiState.value.copy(
            pendingDailyOverride = false,
            pendingDailyMessage = null,
            isLoading = false,
            errorMessage = null,
            validationError = null
        )
        _operation.value = OperationState.Idle
    }

    private suspend fun performCreate() {
        val state = _uiState.value
        _uiState.value = state.copy(isLoading = true)
        _operation.value = OperationState.Loading

        val submission = TransactionEntryFormMapper.submission(state, UUID.randomUUID().toString())
        val favoriteTemplate = submission.favoriteTemplate

        saveEntry(
            request = submission.request,
            type = state.transactionType,
            walletId = state.selectedWalletId,
            destinationWalletId = state.selectedToWalletId,
            category = state.selectedCategory,
            favoriteTemplate = favoriteTemplate,
        )
            .onSuccess { result ->
                if (favoriteTemplate != null && result.favoriteSaved) _uiState.value = _uiState.value.copy(
                    favoriteTemplates = (_uiState.value.favoriteTemplates + favoriteTemplate).takeLast(20)
                )
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                _operation.value = OperationState.Success()
            }
            .onFailure { error ->
                android.util.Log.e("AddTx", "save failed", error)
                val ui = (error as? AppException)?.toUiText()
                    ?: UiText.Res(com.ssajudn.bareuang.presentation.R.string.error_generic)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "",
                    validationError = AddTransactionError.SAVE_FAILED
                )
                _operation.value = OperationState.Error(error.message ?: "", ui)
                _effect.trySend(UiEffect.ShowSnackbarRes(ui))
            }
    }
}
