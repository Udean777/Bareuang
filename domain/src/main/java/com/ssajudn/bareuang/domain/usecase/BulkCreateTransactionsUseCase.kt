package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.model.AppCurrency
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.ImportDraft
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import com.ssajudn.bareuang.domain.utils.DomainCurrencyFormatter
import javax.inject.Inject

class BulkCreateTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val hasMonthlyBudget: HasMonthlyBudgetUseCase,
    private val checkDailyBudget: CheckDailyBudgetUseCase
) {
    suspend operator fun invoke(
        drafts: List<ImportDraft>,
        walletId: String,
        currency: AppCurrency,
        // Soft daily-budget nudge: when false the daily gate trips and returns a
        // DailyBudgetExceededException so the UI can ask before overriding; when
        // true (user confirmed) the gate is skipped.
        force: Boolean = false
    ): Result<Int> {
        if (walletId.isBlank()) return Result.failure(IllegalArgumentException("Dompet wajib dipilih"))
        val selected = drafts.filter { it.isSelected && !it.isDuplicate }
        if (selected.isEmpty()) return Result.failure(IllegalStateException("Tidak ada transaksi yang dipilih"))
        // Budget gate — single check for all
        if (!hasMonthlyBudget()) return Result.failure(AppException.DataException("Budget bulan ini belum diatur"))
        // Daily gate — cek per item yang tanggalnya hari ini dan bertipe EXPENSE
        if (!force) {
            for (d in selected) {
                if (d.type == com.ssajudn.bareuang.domain.model.TransactionType.EXPENSE) {
                    val dailyCheck = checkDailyBudget(d.amount, d.date, currency)
                    if (dailyCheck.isFailure) {
                        return Result.failure(
                            AppException.DailyBudgetExceededException(dailyCheck.exceptionOrNull()?.message ?: "Jatah harian terlampaui")
                        )
                    }
                }
            }
        }
        // Saldo check — sum of expenses vs wallet balance
        val totalExpense = selected.filter { it.type != com.ssajudn.bareuang.domain.model.TransactionType.INCOME }.sumOf { it.amount }
        if (totalExpense > 0) {
            val wallet = walletRepository.getWallets().getOrNull()?.find { it.id == walletId }
            if (wallet != null && wallet.balance < totalExpense) {
                return Result.failure(AppException.DataException("Saldo dompet tidak cukup. Butuh ${DomainCurrencyFormatter.format(totalExpense, currency)}, saldo ${DomainCurrencyFormatter.format(wallet.balance, currency)}"))
            }
        }
        val requests = selected.map { d ->
            CreateTransactionRequest(
                amount = d.amount,
                type = d.type,
                category = d.category,
                merchant = d.merchant,
                date = d.date,
                walletId = walletId
            )
        }
        return transactionRepository.bulkCreate(requests)
    }
}
