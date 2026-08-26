package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.ImportDraft
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import javax.inject.Inject

class BulkCreateTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val hasMonthlyBudget: HasMonthlyBudgetUseCase
) {
    suspend operator fun invoke(
        drafts: List<ImportDraft>,
        walletId: String
    ): Result<Int> {
        if (walletId.isBlank()) return Result.failure(IllegalArgumentException("Dompet wajib dipilih"))
        val selected = drafts.filter { it.isSelected && !it.isDuplicate }
        if (selected.isEmpty()) return Result.failure(IllegalStateException("Tidak ada transaksi yang dipilih"))
        // Budget gate — single check for all
        if (!hasMonthlyBudget()) return Result.failure(AppException.DataException("Budget bulan ini belum diatur"))
        // Saldo check — sum of expenses vs wallet balance
        val totalExpense = selected.filter { it.type != com.ssajudn.bareuang.domain.model.TransactionType.INCOME }.sumOf { it.amount }
        if (totalExpense > 0) {
            val wallet = walletRepository.getWallets().getOrNull()?.find { it.id == walletId }
            if (wallet != null && wallet.balance < totalExpense) {
                return Result.failure(AppException.DataException("Saldo dompet tidak cukup. Butuh ${totalExpense}, saldo ${wallet.balance}"))
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
