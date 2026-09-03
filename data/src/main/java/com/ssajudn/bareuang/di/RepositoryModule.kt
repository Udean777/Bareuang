package com.ssajudn.bareuang.di

import com.ssajudn.bareuang.data.repository.AnalyticsRepositoryImpl
import com.ssajudn.bareuang.data.repository.BudgetRepositoryImpl
import com.ssajudn.bareuang.data.repository.DueBillRepositoryImpl
import com.ssajudn.bareuang.data.repository.GoalRepositoryImpl
import com.ssajudn.bareuang.data.repository.TransactionRepositoryImpl
import com.ssajudn.bareuang.data.repository.WalletRepositoryImpl
import com.ssajudn.bareuang.domain.repository.AnalyticsRepository
import com.ssajudn.bareuang.domain.repository.BudgetRepository
import com.ssajudn.bareuang.domain.repository.DueBillRepository
import com.ssajudn.bareuang.domain.repository.GoalRepository
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.ssajudn.bareuang.data.local.*
import com.ssajudn.bareuang.data.notification.*
import com.ssajudn.bareuang.data.service.*
import com.ssajudn.bareuang.domain.port.*

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    companion object {
        @Provides
        @Singleton
        fun provideClock(): java.time.Clock = java.time.Clock.systemDefaultZone()
    }

    @Binds
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    abstract fun bindDueBillRepository(impl: DueBillRepositoryImpl): DueBillRepository

    @Binds
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository

    @Binds abstract fun bindThemePreferences(impl: ThemePreferences): ThemePreferencesPort
    @Binds abstract fun bindCurrencyPreferences(impl: CurrencyPreferences): CurrencyPreferencesPort
    @Binds abstract fun bindWidgetPreferences(impl: WidgetPreferences): WidgetPreferencesPort
    @Binds abstract fun bindTourPreferences(impl: TourPreferences): TourPreferencesPort
    @Binds abstract fun bindImportPreferences(impl: ImportPreferences): ImportPreferencesPort
    @Binds abstract fun bindOcrConsent(impl: OcrConsentPreferences): OcrConsentPort
    @Binds abstract fun bindOnboardingState(impl: OnboardingStatePreferences): OnboardingStatePort
    @Binds abstract fun bindBackup(impl: BackupRestoreManager): BackupRestorePort
    @Binds abstract fun bindResetter(impl: LocalDataResetter): LocalDataResetPort
    @Binds abstract fun bindCsvParser(impl: CsvMutasiParser): CsvParserPort
    @Binds abstract fun bindReceiptAi(impl: ReceiptAiService): ReceiptAiPort
    @Binds abstract fun bindReminderScheduler(impl: BillReminderScheduler): BillReminderSchedulerPort
    @Binds abstract fun bindReminderPrefs(impl: BillReminderPrefs): BillReminderPreferencesPort
    @Binds abstract fun bindDailyPacingPrefs(impl: DailyPacingPreferences): DailyPacingPreferencesPort

}
