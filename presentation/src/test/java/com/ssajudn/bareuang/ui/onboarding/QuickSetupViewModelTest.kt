package com.ssajudn.bareuang.ui.onboarding

import com.ssajudn.bareuang.domain.model.CreateWalletRequest
import com.ssajudn.bareuang.domain.model.Wallet
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.testutil.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuickSetupViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val walletRepository = mockk<WalletRepository>()
    private val budgetRepository = mockk<BudgetRepository>()

    @Test
    fun `save creates first wallet with opening balance and optional monthly budget`() = runTest {
        every { walletRepository.observeWallets() } returns flowOf(emptyList())
        coEvery { walletRepository.createWallet(any()) } returns Result.success(
            Wallet(id = "cash", name = "Uang Tunai", balance = 1_500_000L)
        )
        coEvery { budgetRepository.getMonthlyBudget(any()) } returns Result.success(0L)
        coEvery { budgetRepository.setBudget(any(), any()) } returns Result.success(true)
        val viewModel = QuickSetupViewModel(walletRepository, budgetRepository)
        var completed = false

        viewModel.onWalletNameChange("Dompet Harian")
        viewModel.onOpeningBalanceChange("Rp 1.500.000")
        viewModel.onMonthlyBudgetChange("2.000.000")
        viewModel.save { completed = true }
        advanceUntilIdle()

        assertTrue(completed)
        val request = slot<CreateWalletRequest>()
        coVerify(exactly = 1) { walletRepository.createWallet(capture(request)) }
        assertTrue(request.captured.name == "Dompet Harian")
        assertTrue(request.captured.balance == 1_500_000L)
        coVerify(exactly = 1) { budgetRepository.setBudget(2_000_000L, "") }
        assertFalse(viewModel.uiState.value.hasError)
    }

    @Test
    fun `skip provisions zero balance default wallet without setting budget`() = runTest {
        every { walletRepository.observeWallets() } returns flowOf(emptyList())
        coEvery { walletRepository.createWallet(any()) } returns Result.success(
            Wallet(id = "cash", name = "Uang Tunai", balance = 0L)
        )
        val viewModel = QuickSetupViewModel(walletRepository, budgetRepository)
        var completed = false

        viewModel.skip { completed = true }
        advanceUntilIdle()

        assertTrue(completed)
        val request = slot<CreateWalletRequest>()
        coVerify(exactly = 1) { walletRepository.createWallet(capture(request)) }
        assertTrue(request.captured.name == "Uang Tunai")
        assertTrue(request.captured.balance == 0L)
        coVerify(exactly = 0) { budgetRepository.setBudget(any(), any()) }
    }
}
