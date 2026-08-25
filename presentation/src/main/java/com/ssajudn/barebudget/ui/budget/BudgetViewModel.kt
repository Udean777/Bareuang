package com.ssajudn.barebudget.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.barebudget.domain.repository.BudgetRepository
import com.ssajudn.barebudget.domain.error.AppException
import com.ssajudn.barebudget.domain.error.userMessage
import com.ssajudn.barebudget.presentation.R
import com.ssajudn.barebudget.ui.common.UiText
import com.ssajudn.barebudget.ui.common.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.ssajudn.barebudget.ui.common.OperationState
import com.ssajudn.barebudget.ui.common.UiEffect
import javax.inject.Inject

enum class BudgetError(val resId: Int) {
    LOCKED(R.string.budget_error_locked), INVALID_AMOUNT(R.string.budget_error_invalid),
    SET_FAILED(R.string.budget_error_set_failed), CATEGORY_SET(R.string.budget_error_category_set),
    CATEGORY_DELETE(R.string.budget_error_category_delete)
}
data class BudgetUiState(
    val currentLimit: Long = 0L,
    val rawAmount: String = "",
    val parsedAmount: Long = 0L,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val error: UiText? = null,
    val categoryBudgets: List<com.ssajudn.barebudget.domain.model.CategoryBudget> = emptyList()
) {
    val isLocked: Boolean get() = currentLimit > 0
    val totalAllocatedCategory: Long get() = categoryBudgets.sumOf { it.limitAmount }
    val isOverAllocated: Boolean get() = currentLimit > 0 && totalAllocatedCategory > currentLimit
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {
    private val _operation = kotlinx.coroutines.flow.MutableStateFlow<OperationState>(OperationState.Idle)
    val operation: kotlinx.coroutines.flow.StateFlow<OperationState> = _operation.asStateFlow()
    private val _effect = Channel<UiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadCurrentBudget()
        observeCategoryBudgets()
    }

    private fun loadCurrentBudget() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getMonthlyBudget()
                .onSuccess { existing ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentLimit = existing,
                        rawAmount = if (existing > 0) existing.toString() else "",
                        parsedAmount = existing
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
        }
    }

    private fun observeCategoryBudgets() {
        viewModelScope.launch {
            repository.getCategoryBudgets().collect { list ->
                _uiState.value = _uiState.value.copy(categoryBudgets = list)
            }
        }
    }

    fun onAmountChange(input: String) {
        val digitsOnly = input.filter { it.isDigit() }.take(12)
        val parsed = digitsOnly.toLongOrNull() ?: 0L
        _uiState.value = _uiState.value.copy(
            rawAmount = digitsOnly,
            parsedAmount = parsed
        )
    }

    fun saveBudget() {
        val state = _uiState.value
        if (state.isLocked) {
            val ui = UiText.Res(BudgetError.LOCKED.resId)
            _uiState.value = state.copy(errorMessage = "Budget bulan ini sudah terkunci. Hanya bisa diubah bulan depan.", error = ui)
            return
        }
        if (state.parsedAmount <= 0) {
            val ui = UiText.Res(BudgetError.INVALID_AMOUNT.resId)
            _uiState.value = state.copy(errorMessage = "Please enter a valid budget amount", error = ui)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null, error = null)
            _operation.value = OperationState.Loading
            repository.setBudget(state.parsedAmount)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                    _operation.value = OperationState.Success()
                    viewModelScope.launch { _effect.send(UiEffect.PopBackStack) }
                }
                .onFailure { error ->
                    val msg = (error as? AppException)?.userMessage() ?: error.localizedMessage ?: "Failed to set budget"
                    val ui = (error as? AppException)?.toUiText() ?: UiText.Res(BudgetError.SET_FAILED.resId)
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg, error = ui)
                    _operation.value = OperationState.Error(msg, ui)
                    viewModelScope.launch { _effect.send(UiEffect.ShowSnackbarRes(ui)) }
                }
        }
    }

    fun setCategoryBudget(category: com.ssajudn.barebudget.domain.model.TransactionCategory, limit: Long) {
        if (limit <= 0) return
        viewModelScope.launch {
            repository.setCategoryBudget(category, limit)
                .onFailure { error ->
                    val ui = (error as? AppException)?.toUiText() ?: UiText.Res(BudgetError.CATEGORY_SET.resId)
                    val msg = (error as? AppException)?.userMessage() ?: error.localizedMessage ?: "Gagal mengatur limit kategori"
                    _effect.send(UiEffect.ShowSnackbarRes(ui))
                    _effect.send(UiEffect.ShowSnackbar(msg))
                }
        }
    }

    fun deleteCategoryBudget(category: com.ssajudn.barebudget.domain.model.TransactionCategory) {
        viewModelScope.launch {
            repository.deleteCategoryBudget(category)
                .onFailure { error ->
                    val ui = (error as? AppException)?.toUiText() ?: UiText.Res(BudgetError.CATEGORY_DELETE.resId)
                    val msg = (error as? AppException)?.userMessage() ?: error.localizedMessage ?: "Gagal menghapus limit kategori"
                    _effect.send(UiEffect.ShowSnackbarRes(ui))
                    _effect.send(UiEffect.ShowSnackbar(msg))
                }
        }
    }
}