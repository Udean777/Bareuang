package com.ssajudn.bareuang.ui.ocr

import com.ssajudn.bareuang.domain.port.OcrConsentPort
import com.ssajudn.bareuang.domain.port.ReceiptAiPort
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.usecase.CheckDailyBudgetUseCase
import com.ssajudn.bareuang.domain.usecase.HasMonthlyBudgetUseCase
import com.ssajudn.bareuang.testutil.MainDispatcherRule
import com.ssajudn.bareuang.domain.port.NetworkMonitorPort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@kotlinx.coroutines.ExperimentalCoroutinesApi
class OcrScanViewModelConsentTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val wallets = mockk<WalletRepository>(relaxed = true)
    private val transactions = mockk<TransactionRepository>(relaxed = true)
    private val receiptAi = mockk<ReceiptAiPort>(relaxed = true)
    private val hasBudget = mockk<HasMonthlyBudgetUseCase>(relaxed = true)
    private val dailyBudget = mockk<CheckDailyBudgetUseCase>(relaxed = true)
    private val network = mockk<NetworkMonitorPort>()
    private val consent = mockk<OcrConsentPort>(relaxed = true)

    private fun createViewModel(hasConsent: Boolean): OcrScanViewModel {
        every { consent.hasCurrentConsent } returns hasConsent
        every { network.isOnline() } returns true
        every { network.observeIsOnline() } returns flowOf(true)
        coEvery { wallets.getWallets() } returns Result.success(emptyList())
        every { wallets.observeWallets() } returns flowOf(emptyList())
        return OcrScanViewModel(
            wallets,
            transactions,
            receiptAi,
            hasBudget,
            dailyBudget,
            network,
            consent,
        )
    }

    @Test
    fun `scan request opens consent and accept persists current version`() = runTest {
        val vm = createViewModel(hasConsent = false)
        advanceUntilIdle()

        vm.requestOcrConsent()
        assertTrue(vm.uiState.value.showOcrConsent)

        vm.acceptOcrConsent()

        verify(exactly = 1) { consent.grantCurrentConsent() }
        assertTrue(vm.uiState.value.hasOcrConsent)
        assertFalse(vm.uiState.value.showOcrConsent)
    }

    @Test
    fun `image processing refuses to call AI before consent`() = runTest {
        val vm = createViewModel(hasConsent = false)
        advanceUntilIdle()

        vm.processImage(mockk())

        assertTrue(vm.uiState.value.showOcrConsent)
        coVerify(exactly = 0) { receiptAi.parseReceiptImage(any()) }
    }
}
