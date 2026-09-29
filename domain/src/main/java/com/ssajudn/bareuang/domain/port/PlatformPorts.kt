package com.ssajudn.bareuang.domain.port

import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import com.ssajudn.bareuang.domain.model.BackupRestorePreview
import com.ssajudn.bareuang.domain.model.BackupRestoreSummary
import com.ssajudn.bareuang.domain.model.CsvColumnMapping
import com.ssajudn.bareuang.domain.model.CsvImportInspection
import com.ssajudn.bareuang.domain.model.CsvImportParseOutput
import com.ssajudn.bareuang.domain.model.ImportDraft
import com.ssajudn.bareuang.domain.model.ParsedReceipt
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionEntryTemplate
import com.ssajudn.bareuang.domain.model.TransactionType
import kotlinx.coroutines.flow.StateFlow

/**
 * Platform ports intentionally remain small domain-owned boundaries. Their
 * implementations live in app/data modules so use cases never depend on
 * Android, Room, or provider SDKs. Keep a port only when it has an active
 * adapter or represents a planned replaceable integration boundary.
 */

interface ThemePreferencesPort { val darkMode: StateFlow<AppThemeDarkMode>; fun setDarkMode(mode: AppThemeDarkMode) }
interface CurrencyPreferencesPort { val currency: StateFlow<AppCurrency>; fun setCurrency(currency: AppCurrency); fun getCurrency(): AppCurrency }
interface WidgetPreferencesPort { val hideBalance: StateFlow<Boolean>; fun setHideBalance(hidden: Boolean) }
interface TourPreferencesPort { val isTourCompleted: Boolean; fun markTourCompleted(); fun resetTour() }
interface ImportPreferencesPort { val importCount: StateFlow<Int>; fun increment(count: Int) }
interface TransactionEntryPreferencesPort {
    fun lastWalletId(type: TransactionType): String?
    fun lastDestinationWalletId(): String?
    fun lastCategory(type: TransactionType): TransactionCategory?
    fun saveLastEntry(
        type: TransactionType,
        walletId: String,
        destinationWalletId: String?,
        category: TransactionCategory
    )
    fun favoriteTemplates(): List<TransactionEntryTemplate>
    fun saveFavoriteTemplate(template: TransactionEntryTemplate)
    fun deleteFavoriteTemplate(templateId: String)
}
interface OnboardingStatePort {
    var isOnboardingCompleted: Boolean
    fun completeOnboarding()
    fun resetOnboarding()
}
interface BackupRestorePort {
    suspend fun exportBackup(uri: String, password: CharArray): Result<Unit>
    suspend fun previewBackup(uri: String, password: CharArray?): Result<BackupRestorePreview>
    suspend fun confirmRestore(previewId: String): Result<BackupRestoreSummary>
    fun discardRestore(previewId: String)
}
interface LocalDataResetPort { suspend fun wipe() }
interface CsvParserPort {
    fun parseWithStats(csvText: String): Pair<List<ImportDraft>, Int>
    fun inspect(csvText: String): CsvImportInspection
    fun parse(csvText: String, mapping: CsvColumnMapping): CsvImportParseOutput
}
interface ReceiptOcrPort {
    val isAvailable: Boolean
    suspend fun parseReceiptImage(uri: String): Result<ParsedReceipt>
}

interface BillReminderSchedulerPort { fun scheduleDailyAt(hour: Int, minute: Int); fun runNow() }
interface RecurringTransactionSchedulerPort { fun ensureScheduled(); fun runNow() }
interface BillReminderPreferencesPort {
    fun notificationsEnabled(): Boolean
    fun reminderHour(): Int
    fun reminderMinute(): Int
    fun setReminderTime(hour: Int, minute: Int)
}
interface DailyPacingPreferencesPort {
    /** Null means automatic pacing derived from the monthly budget. */
    val customTarget: StateFlow<Long?>
    /** Last saved custom value, retained when automatic mode is selected. */
    val lastCustomTarget: StateFlow<Long?>
    fun setCustomTarget(amount: Long?)
    fun reset()
}
