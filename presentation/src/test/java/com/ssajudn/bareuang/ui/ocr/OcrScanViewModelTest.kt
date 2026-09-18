package com.ssajudn.bareuang.ui.ocr

import android.net.Uri
import com.ssajudn.bareuang.domain.model.ParsedReceipt
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.port.ReceiptOcrPort
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.usecase.CheckDailyBudgetUseCase
import com.ssajudn.bareuang.domain.usecase.HasMonthlyBudgetUseCase
import com.ssajudn.bareuang.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@kotlinx.coroutines.ExperimentalCoroutinesApi
class OcrScanViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val wallets = mockk<WalletRepository>(relaxed = true)
    private val transactions = mockk<TransactionRepository>(relaxed = true)
    private val receiptOcr = mockk<ReceiptOcrPort>(relaxed = true)
    private val hasBudget = mockk<HasMonthlyBudgetUseCase>(relaxed = true)
    private val dailyBudget = mockk<CheckDailyBudgetUseCase>(relaxed = true)

    private fun createViewModel(ocrAvailable: Boolean = true): OcrScanViewModel {
        every { receiptOcr.isAvailable } returns ocrAvailable
        coEvery { wallets.getWallets() } returns Result.success(emptyList())
        every { wallets.observeWallets() } returns flowOf(emptyList())
        return OcrScanViewModel(
            wallets,
            transactions,
            receiptOcr,
            hasBudget,
            dailyBudget,
        )
    }

    @Test
    fun `release state reports OCR unavailable`() = runTest {
        val vm = createViewModel(ocrAvailable = false)

        assertFalse(vm.uiState.value.isOcrAvailable)
    }

    @Test
    fun `selecting image only opens preview without OCR`() = runTest {
        val vm = createViewModel()
        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://receipt/preview"

        vm.selectImage(uri)

        assertEquals(uri, vm.uiState.value.selectedImageUri)
        assertTrue(vm.uiState.value.showImagePreview)
        coVerify(exactly = 0) { receiptOcr.parseReceiptImage(any()) }
    }

    @Test
    fun `using selected image maps OCR result into editable draft`() = runTest {
        val vm = createViewModel()
        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://receipt/success"
        coEvery { receiptOcr.parseReceiptImage("content://receipt/success") } returns Result.success(
            ParsedReceipt(
                merchantName = "Kopi Lokal",
                totalAmount = 25000L,
                suggestedCategory = TransactionCategory.FOOD,
                rawText = "Kopi Lokal\nTOTAL 25.000",
                date = "2026-09-18",
            ),
        )

        vm.selectImage(uri)
        vm.processSelectedImage()
        advanceUntilIdle()

        assertEquals("Kopi Lokal", vm.uiState.value.merchant)
        assertEquals("25000", vm.uiState.value.amount)
        assertEquals(TransactionCategory.FOOD, vm.uiState.value.category)
        assertFalse(vm.uiState.value.showImagePreview)
        assertFalse(vm.uiState.value.ocrError)
    }

    @Test
    fun `OCR failure keeps preview and exposes retry state`() = runTest {
        val vm = createViewModel()
        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://receipt/failure"
        coEvery { receiptOcr.parseReceiptImage("content://receipt/failure") } returns Result.failure(
            IllegalStateException("recognition failed"),
        )

        vm.selectImage(uri)
        vm.processSelectedImage()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.showImagePreview)
        assertTrue(vm.uiState.value.ocrError)
        assertFalse(vm.uiState.value.isProcessing)
    }

    @Test
    fun `new image cancels stale OCR result`() = runTest {
        val vm = createViewModel()
        val firstUri = mockk<Uri>()
        val secondUri = mockk<Uri>()
        every { firstUri.toString() } returns "content://receipt/first"
        every { secondUri.toString() } returns "content://receipt/second"
        val firstRequest = CompletableDeferred<Result<ParsedReceipt>>()
        coEvery { receiptOcr.parseReceiptImage("content://receipt/first") } coAnswers { firstRequest.await() }
        coEvery { receiptOcr.parseReceiptImage("content://receipt/second") } returns Result.success(
            ParsedReceipt(
                merchantName = "Toko Kedua",
                totalAmount = 42000L,
                suggestedCategory = TransactionCategory.SHOPPING,
                rawText = "Toko Kedua\nTOTAL 42.000",
                date = "2026-09-18",
            ),
        )

        vm.selectImage(firstUri)
        vm.processSelectedImage()
        runCurrent()

        vm.selectImage(secondUri)
        vm.processSelectedImage()
        advanceUntilIdle()

        assertEquals("Toko Kedua", vm.uiState.value.merchant)
        assertEquals(secondUri, vm.uiState.value.selectedImageUri)
        assertFalse(vm.uiState.value.isProcessing)
    }

    @Test
    fun `manual fallback opens editable transaction form`() = runTest {
        val vm = createViewModel()

        vm.startManualEntry()
        vm.onMerchantChange("Catatan manual")
        vm.onAmountChange("25000")

        assertTrue(vm.uiState.value.isManualEntry)
        assertEquals("Catatan manual", vm.uiState.value.merchant)
        assertEquals(25000L, vm.uiState.value.parsedAmount)
        assertEquals("", vm.uiState.value.rawText)
    }
}
