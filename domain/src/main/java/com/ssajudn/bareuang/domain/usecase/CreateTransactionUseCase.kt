package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.CreateTransactionRequest
import com.ssajudn.bareuang.domain.model.Transaction
import com.ssajudn.bareuang.domain.error.AppException
import com.ssajudn.bareuang.domain.repository.TransactionCommandRepository
import com.ssajudn.bareuang.domain.repository.WalletRepository
import javax.inject.Inject

class CreateTransactionUseCase @Inject constructor(
    private val repository: TransactionCommandRepository,
    private val walletRepository: WalletRepository,
    private val validateTransaction: ValidateTransactionUseCase
) {
    suspend operator fun invoke(request: CreateTransactionRequest): Result<Transaction> {
        val wallets = walletRepository.getWallets().getOrElse { return Result.failure(it) }
        val validationError = validateTransaction(
            type = request.type,
            amount = request.amount,
            sourceWalletId = request.walletId,
            targetWalletId = request.toWalletId,
            wallets = wallets
        )
        if (validationError != null) {
            if (validationError == com.ssajudn.bareuang.domain.error.TransactionValidationReason.INSUFFICIENT_BALANCE) {
                val availableBalance = wallets.firstOrNull { it.id == request.walletId }?.balance ?: 0L
                return Result.failure(
                    AppException.InsufficientBalanceException(availableBalance, request.amount)
                )
            }
            return Result.failure(AppException.TransactionValidationException(validationError))
        }
        return repository.createTransaction(request)
    }
}
