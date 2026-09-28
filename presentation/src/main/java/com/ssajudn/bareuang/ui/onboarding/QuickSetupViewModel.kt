package com.ssajudn.bareuang.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssajudn.bareuang.domain.AppConfig
import com.ssajudn.bareuang.domain.model.CreateWalletRequest
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class QuickSetupUiState(
    val walletName: String = AppConfig.DEFAULT_WALLET_NAME,
    val openingBalanceInput: String = "",
    val monthlyBudgetInput: String = "",
    val isSaving: Boolean = false,
    val hasError: Boolean = false,
)

@HiltViewModel
class QuickSetupViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val budgetRepository: BudgetRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuickSetupUiState())
    val uiState = _uiState.asStateFlow()

    fun onWalletNameChange(value: String) {
        _uiState.value = _uiState.value.copy(walletName = value, hasError = false)
    }

    fun onOpeningBalanceChange(value: String) {
        _uiState.value = _uiState.value.copy(
            openingBalanceInput = value.filter(Char::isDigit).take(12),
            hasError = false,
        )
    }

    fun onMonthlyBudgetChange(value: String) {
        _uiState.value = _uiState.value.copy(
            monthlyBudgetInput = value.filter(Char::isDigit).take(12),
            hasError = false,
        )
    }

    fun save(onComplete: () -> Unit) = save(useEnteredValues = true, onComplete = onComplete)

    fun skip(onComplete: () -> Unit) = save(useEnteredValues = false, onComplete = onComplete)

    private fun save(useEnteredValues: Boolean, onComplete: () -> Unit) {
        val state = _uiState.value
        if (state.isSaving) return

        val walletName = if (useEnteredValues) state.walletName.trim() else AppConfig.DEFAULT_WALLET_NAME
        val openingBalance = if (useEnteredValues) state.openingBalanceInput.toLongOrNull() ?: 0L else 0L
        val monthlyBudget = if (useEnteredValues) state.monthlyBudgetInput.toLongOrNull() ?: 0L else 0L
        if (walletName.isBlank()) {
            _uiState.value = state.copy(hasError = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, hasError = false)
            val succeeded = try {
                // Observe first so an empty database does not provision the zero-balance
                // default wallet before the user-selected opening balance is saved.
                val wallets = walletRepository.observeWallets().first()
                if (wallets.isEmpty()) {
                    walletRepository.createWallet(
                        CreateWalletRequest(
                            name = walletName,
                            balance = openingBalance,
                            colorHex = AppConfig.DEFAULT_WALLET_COLOR,
                            iconName = AppConfig.DEFAULT_WALLET_ICON,
                        )
                    ).getOrThrow()
                }

                if (monthlyBudget > 0L) {
                    val existingBudget = budgetRepository.getMonthlyBudget().getOrThrow()
                    if (existingBudget <= 0L) {
                        budgetRepository.setBudget(monthlyBudget).getOrThrow()
                    }
                }
                true
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                false
            }

            if (succeeded) {
                _uiState.value = _uiState.value.copy(isSaving = false)
                onComplete()
            } else {
                _uiState.value = _uiState.value.copy(isSaving = false, hasError = true)
            }
        }
    }
}
