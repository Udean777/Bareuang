package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.repository.TransactionCommandRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionCommandRepository
) {
    suspend operator fun invoke(transactionId: String) =
        transactionRepository.deleteTransaction(transactionId)
}
