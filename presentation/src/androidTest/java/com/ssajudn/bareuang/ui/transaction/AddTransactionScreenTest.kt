package com.ssajudn.bareuang.ui.transaction

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.port.DailyPacingPreferencesPort
import com.ssajudn.bareuang.domain.port.TransactionEntryPreferencesPort
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.usecase.CheckDailyBudgetUseCase
import com.ssajudn.bareuang.domain.usecase.CheckTransactionEntryUseCase
import com.ssajudn.bareuang.domain.usecase.CreateTransactionUseCase
import com.ssajudn.bareuang.domain.usecase.HasMonthlyBudgetUseCase
import com.ssajudn.bareuang.domain.usecase.ObserveTransactionEntryHistoryUseCase
import com.ssajudn.bareuang.domain.usecase.SaveTransactionEntryUseCase
import com.ssajudn.bareuang.domain.usecase.TransactionCategoryPolicy
import com.ssajudn.bareuang.domain.usecase.TransactionEntryBudgetUseCase
import com.ssajudn.bareuang.domain.usecase.TransactionEntryPreferencesUseCase
import com.ssajudn.bareuang.domain.usecase.ValidateTransactionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Clock
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class AddTransactionScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val wallets = listOf(Wallet(id = "cash", name = "Cash", balance = 1_000_000L))
    private val transactionRepository = mockk<TransactionRepository>(relaxed = true)
    private val walletRepository = mockk<WalletRepository>(relaxed = true)
    private val budgetRepository = mockk<BudgetRepository>(relaxed = true)
    private val entryPreferences = mockk<TransactionEntryPreferencesPort>(relaxed = true)
    private val dailyTarget = MutableStateFlow<Long?>(null)
    private val pacingPreferences = object : DailyPacingPreferencesPort {
        override val customTarget = dailyTarget
        override val lastCustomTarget = dailyTarget
        override fun setCustomTarget(amount: Long?) {
            dailyTarget.value = amount
        }
        override fun reset() {
            dailyTarget.value = null
        }
    }

    private fun createViewModel(): AddTransactionViewModel {
        coEvery { walletRepository.getWallets() } returns Result.success(wallets)
        every { walletRepository.observeWallets() } returns flowOf(wallets)
        coEvery { budgetRepository.getMonthlyBudget(any()) } returns Result.success(2_000_000L)
        every { budgetRepository.getCategoryBudgets(any()) } returns flowOf(emptyList())
        every { transactionRepository.observeTransactions() } returns flowOf(emptyList())
        coEvery { transactionRepository.getTransactions(any(), any(), any()) } returns Result.success(emptyList())
        every { entryPreferences.lastWalletId(any()) } returns null
        every { entryPreferences.lastDestinationWalletId() } returns null
        every { entryPreferences.lastCategory(any()) } returns null
        every { entryPreferences.favoriteTemplates() } returns emptyList()

        coEvery { transactionRepository.createTransaction(any()) } coAnswers {
            val request = firstArg<CreateTransactionRequest>()
            Result.success(
                Transaction(
                    id = "created-transaction",
                    amount = request.amount,
                    type = request.type,
                    category = request.category,
                    merchant = request.merchant,
                    date = request.date,
                    walletId = request.walletId,
                    toWalletId = request.toWalletId,
                )
            )
        }

        val preferencesUseCase = TransactionEntryPreferencesUseCase(
            entryPreferences,
            TransactionCategoryPolicy(),
        )
        val validation = ValidateTransactionUseCase()
        val dailyBudget = CheckDailyBudgetUseCase(
            budgetRepository,
            transactionRepository,
            pacingPreferences,
            Clock.systemDefaultZone(),
        )
        val entryBudget = TransactionEntryBudgetUseCase(
            HasMonthlyBudgetUseCase(budgetRepository),
            dailyBudget,
        )
        val checkEntry = CheckTransactionEntryUseCase(validation, entryBudget)
        val saveEntry = SaveTransactionEntryUseCase(
            CreateTransactionUseCase(transactionRepository, walletRepository, validation),
            preferencesUseCase,
        )
        val dataCoordinator = TransactionEntryDataCoordinator(
            walletRepository,
            budgetRepository,
            checkEntry,
            ObserveTransactionEntryHistoryUseCase(transactionRepository),
        )
        return AddTransactionViewModel(saveEntry, checkEntry, dataCoordinator, preferencesUseCase)
    }

    private fun showScreen(onNavigateBack: () -> Unit = {}) {
        val viewModel = createViewModel()
        composeRule.setContent {
            MaterialTheme {
                AddTransactionScreen(
                    onNavigateBack = onNavigateBack,
                    viewModel = viewModel,
                )
            }
        }
    }

    @Test
    fun savingFromTheFormPersistsTransactionAndReturns() {
        val returned = AtomicBoolean(false)
        showScreen { returned.set(true) }

        composeRule.onNodeWithText("+10 rb").performScrollTo().performClick()
        composeRule.onNodeWithText("Save").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { returned.get() }

        coVerify(exactly = 1) {
            transactionRepository.createTransaction(match { it.amount == 10_000L })
        }
    }

    @Test
    fun cancellingDailyBudgetPromptDoesNotPersistTransaction() {
        dailyTarget.value = 1L
        showScreen()

        composeRule.onNodeWithText("+10 rb").performScrollTo().performClick()
        composeRule.onNodeWithText("Save").performClick()
        composeRule.onNodeWithText("Daily pacing target exceeded").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").performClick()

        composeRule.onAllNodesWithText("Daily pacing target exceeded").assertCountEquals(0)
        coVerify(exactly = 0) { transactionRepository.createTransaction(any()) }
    }

    @Test
    fun confirmingDailyBudgetPromptPersistsTransaction() {
        dailyTarget.value = 1L
        showScreen()

        composeRule.onNodeWithText("+10 rb").performScrollTo().performClick()
        composeRule.onNodeWithText("Save").performClick()
        composeRule.onNodeWithText("Daily pacing target exceeded").assertIsDisplayed()
        composeRule.onNodeWithText("Save anyway").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Daily pacing target exceeded").fetchSemanticsNodes().isEmpty()
        }

        coVerify(exactly = 1) {
            transactionRepository.createTransaction(match { it.amount == 10_000L })
        }
    }
}
