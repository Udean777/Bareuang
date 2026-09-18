package com.ssajudn.bareuang.domain.port

import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.AppThemeDarkMode
import com.ssajudn.bareuang.domain.model.ImportDraft
import com.ssajudn.bareuang.domain.model.ParsedReceipt
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
interface OnboardingStatePort {
    var isOnboardingCompleted: Boolean
    fun completeOnboarding()
    fun resetOnboarding()
}
interface BackupRestorePort {
    suspend fun exportBackup(uri: String): Result<Unit>
    suspend fun importBackup(uri: String): Result<Int>
}
interface LocalDataResetPort { suspend fun wipe() }
interface CsvParserPort { fun parseWithStats(csvText: String): Pair<List<ImportDraft>, Int> }
interface ReceiptOcrPort {
    val isAvailable: Boolean
    suspend fun parseReceiptImage(uri: String): Result<ParsedReceipt>
}

interface BillReminderSchedulerPort { fun scheduleDailyAt(hour: Int, minute: Int); fun runNow() }
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
