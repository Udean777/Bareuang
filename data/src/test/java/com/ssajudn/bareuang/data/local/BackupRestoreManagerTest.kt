package com.ssajudn.bareuang.data.local

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ssajudn.bareuang.data.local.room.AppDatabase
import com.ssajudn.bareuang.data.local.room.BudgetDao
import com.ssajudn.bareuang.data.local.room.DueBillDao
import com.ssajudn.bareuang.data.local.room.GoalDao
import com.ssajudn.bareuang.data.local.room.LocalBudgetEntity
import com.ssajudn.bareuang.data.local.room.LocalCategoryBudgetEntity
import com.ssajudn.bareuang.data.local.room.LocalDueBillEntity
import com.ssajudn.bareuang.data.local.room.LocalGoalEntity
import com.ssajudn.bareuang.data.local.room.LocalTransactionEntity
import com.ssajudn.bareuang.data.local.room.LocalWalletEntity
import com.ssajudn.bareuang.data.local.room.TransactionDao
import com.ssajudn.bareuang.data.local.room.WalletDao
import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.port.CurrencyPreferencesPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest

class BackupRestoreManagerTest {
    private val fileUri = "content://backup/location"
    private lateinit var context: Context
    private lateinit var resolver: ContentResolver
    private lateinit var manager: BackupRestoreManager
    private lateinit var output: ByteArrayOutputStream
    private lateinit var insertedWallets: WalletDao
    private lateinit var insertedTransactions: TransactionDao

    @Before
    fun setUp() {
        mockkStatic(Uri::class)
        val uri = mockk<Uri>()
        every { Uri.parse(fileUri) } returns uri
        resolver = mockk()
        context = mockk()
        every { context.contentResolver } returns resolver
        every { context.packageName } returns "com.ssajudn.bareuang.test"
        every { context.packageManager } returns mockk<PackageManager>(relaxed = true)
        output = ByteArrayOutputStream()
        every { resolver.openOutputStream(uri) } answers { output }

        val db = mockk<AppDatabase>()
        val transactionDao = mockk<TransactionDao>(relaxed = true)
        val dueBillDao = mockk<DueBillDao>(relaxed = true)
        val budgetDao = mockk<BudgetDao>(relaxed = true)
        val goalDao = mockk<GoalDao>(relaxed = true)
        val walletDao = mockk<WalletDao>(relaxed = true)
        insertedWallets = walletDao
        insertedTransactions = transactionDao
        every { db.transactionDao() } returns transactionDao
        every { db.dueBillDao() } returns dueBillDao
        every { db.budgetDao() } returns budgetDao
        every { db.goalDao() } returns goalDao
        every { db.walletDao() } returns walletDao
        every { transactionDao.getAllTransactions() } returns emptyList()
        every { dueBillDao.getAllDueBills() } returns emptyList()
        every { budgetDao.getAllBudgets() } returns emptyList()
        every { budgetDao.getAllCategoryBudgets() } returns emptyList()
        every { goalDao.getAllGoals() } returns emptyList()
        every { walletDao.getAllWallets() } returns emptyList()
        val currency = mockk<CurrencyPreferencesPort>()
        every { currency.getCurrency() } returns AppCurrency.USD
        manager = BackupRestoreManager(context, db, currency)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `exported file can be previewed and preview does not write data`() = runTest {
        assertTrue(manager.exportBackup(fileUri, "my secure backup password".toCharArray()).isSuccess)
        val encryptedBytes = output.toByteArray()
        assertTrue(BackupCryptoCodec().isEncrypted(encryptedBytes))
        every { resolver.openInputStream(any()) } answers { ByteArrayInputStream(encryptedBytes) }

        val result = manager.previewBackup(fileUri, "my secure backup password".toCharArray())

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEncrypted)
        assertEquals("USD", result.getOrThrow().currencyCode)
        verify(exactly = 0) { insertedWallets.insertWallets(any()) }
        verify(exactly = 0) { insertedTransactions.insertTransactions(any()) }
    }

    @Test
    fun `legacy version one and checksummed version two files remain previewable`() = runTest {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val legacyData = BareuangBackupData(version = 1)
        val legacyJson = gson.toJson(legacyData).toByteArray()
        every { resolver.openInputStream(any()) } answers { ByteArrayInputStream(legacyJson) }
        val v1 = manager.previewBackup(fileUri, null).getOrThrow()
        assertFalse(v1.isEncrypted)
        manager.discardRestore(v1.id)

        val payloadJson = gson.toJson(legacyData)
        val envelope = JsonObject().apply {
            addProperty("formatVersion", 2)
            add("payload", JsonParser.parseString(payloadJson))
            addProperty("sha256", sha256(payloadJson))
        }
        val v2Bytes = gson.toJson(envelope).toByteArray()
        every { resolver.openInputStream(any()) } answers { ByteArrayInputStream(v2Bytes) }
        val v2 = manager.previewBackup(fileUri, null).getOrThrow()
        assertFalse(v2.isEncrypted)
        assertEquals(0, v2.transactions.total)
    }

    @Test
    fun `encrypted restore preview rejects wrong password and corrupted file`() = runTest {
        assertTrue(manager.exportBackup(fileUri, "my secure backup password".toCharArray()).isSuccess)
        val encryptedBytes = output.toByteArray()
        every { resolver.openInputStream(any()) } answers { ByteArrayInputStream(encryptedBytes) }

        assertTrue(manager.previewBackup(fileUri, "a different password".toCharArray()).isFailure)
        val corrupted = encryptedBytes.copyOf().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
        every { resolver.openInputStream(any()) } answers { ByteArrayInputStream(corrupted) }
        assertTrue(manager.previewBackup(fileUri, "my secure backup password".toCharArray()).isFailure)
    }

    @Test
    fun `preview counts inserts and ID conflicts for each record type`() = runTest {
        val backupWallet = LocalWalletEntity("wallet-backup", "Backup wallet", 100_000, "#fff", "wallet", "2026-09-28")
        val backupOnlyWallet = LocalWalletEntity("wallet-new", "New wallet", 25_000, "#000", "wallet", "2026-09-28")
        val backupTransaction = LocalTransactionEntity(
            id = "tx-backup",
            amount = 20_000,
            type = "EXPENSE",
            category = "FOOD",
            merchant = "Coffee",
            date = "2026-09-28",
            notes = null,
            receiptUrl = null,
            walletId = backupWallet.id,
        )
        val backupOnlyTransaction = backupTransaction.copy(id = "tx-new", merchant = "Bread")
        every { insertedWallets.getAllWallets() } returns listOf(backupWallet, backupOnlyWallet)
        every { insertedTransactions.getAllTransactions() } returns listOf(backupTransaction, backupOnlyTransaction)
        assertTrue(manager.exportBackup(fileUri, "my secure backup password".toCharArray()).isSuccess)
        val encryptedBytes = output.toByteArray()

        // Model another device with one conflicting ID and one local-only record.
        every { insertedWallets.getAllWallets() } returns listOf(backupWallet.copy(name = "Local name"), backupWallet.copy(id = "wallet-local"))
        every { insertedTransactions.getAllTransactions() } returns listOf(backupTransaction.copy(merchant = "Local merchant"), backupTransaction.copy(id = "tx-local"))
        every { resolver.openInputStream(any()) } answers { ByteArrayInputStream(encryptedBytes) }

        val preview = manager.previewBackup(fileUri, "my secure backup password".toCharArray()).getOrThrow()

        assertEquals(2, preview.wallets.total)
        assertEquals(1, preview.wallets.new)
        assertEquals(1, preview.wallets.replaced)
        assertEquals(2, preview.transactions.total)
        assertEquals(1, preview.transactions.new)
        assertEquals(1, preview.transactions.replaced)
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}
