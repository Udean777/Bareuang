package com.ssajudn.bareuang.ui.settings

import android.net.Uri
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.model.BackupRecordPreview
import com.ssajudn.bareuang.domain.model.BackupRestorePreview
import com.ssajudn.bareuang.domain.model.BackupRestoreSummary
import com.ssajudn.bareuang.domain.port.BackupRestorePort
import com.ssajudn.bareuang.domain.usecase.ResetLocalDataUseCase
import com.ssajudn.bareuang.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@kotlinx.coroutines.ExperimentalCoroutinesApi
class SettingsBackupViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val backup = mockk<BackupRestorePort>(relaxed = true)
    private val preview = BackupRestorePreview(
        id = "preview-token",
        backupDate = "2026-09-28 12:00:00",
        appVersion = "1.2.3",
        currencyCode = "IDR",
        isEncrypted = true,
        hasReceiptReferences = false,
        wallets = BackupRecordPreview(total = 1, new = 1, replaced = 0),
        transactions = BackupRecordPreview(total = 3, new = 2, replaced = 1),
        dueBills = BackupRecordPreview(total = 0, new = 0, replaced = 0),
        budgets = BackupRecordPreview(total = 0, new = 0, replaced = 0),
        categoryBudgets = BackupRecordPreview(total = 0, new = 0, replaced = 0),
        goals = BackupRecordPreview(total = 0, new = 0, replaced = 0),
    )

    private fun createViewModel(): SettingsBackupViewModel = SettingsBackupViewModel(
        backupManager = backup,
        resetLocalDataUseCase = mockk<ResetLocalDataUseCase>(relaxed = true),
    )

    @Test
    fun `successful file inspection exposes restore preview without committing`() = runTest {
        val vm = createViewModel()
        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://backup/file"
        coEvery { backup.previewBackup("content://backup/file", any()) } returns Result.success(preview)

        vm.previewBackup(uri, "good password".toCharArray())
        advanceUntilIdle()

        assertEquals(preview, vm.uiState.value.restorePreview)
        assertNull(vm.uiState.value.restoreSummary)
        coVerify(exactly = 0) { backup.confirmRestore(any()) }
    }

    @Test
    fun `failed preview leaves no confirmable data and reports an error`() = runTest {
        val vm = createViewModel()
        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://backup/wrong-password"
        coEvery { backup.previewBackup("content://backup/wrong-password", any()) } returns Result.failure(
            AppException.DataException("incorrect password"),
        )

        vm.previewBackup(uri, "wrong password".toCharArray())
        advanceUntilIdle()

        assertNull(vm.uiState.value.restorePreview)
        assertNotNull(vm.uiState.value.restoreError)
        coVerify(exactly = 0) { backup.confirmRestore(any()) }
    }

    @Test
    fun `successful confirmation shows summary and consumes preview`() = runTest {
        val vm = createViewModel()
        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://backup/file"
        coEvery { backup.previewBackup("content://backup/file", any()) } returns Result.success(preview)
        val summary = BackupRestoreSummary(added = 3, replaced = 1, currencyCode = "IDR")
        coEvery { backup.confirmRestore(preview.id) } returns Result.success(summary)

        vm.previewBackup(uri, null)
        advanceUntilIdle()
        vm.confirmRestore(preview.id)
        advanceUntilIdle()

        assertNull(vm.uiState.value.restorePreview)
        assertEquals(summary, vm.uiState.value.restoreSummary)
    }
}
