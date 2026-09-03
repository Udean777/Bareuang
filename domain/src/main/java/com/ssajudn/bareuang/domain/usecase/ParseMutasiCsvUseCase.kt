package com.ssajudn.bareuang.domain.usecase

import com.ssajudn.bareuang.domain.model.ImportParseResult
import com.ssajudn.bareuang.domain.repository.TransactionRepository
import javax.inject.Inject

class ParseMutasiCsvUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend fun markDuplicates(drafts: List<com.ssajudn.bareuang.domain.model.ImportDraft>, skippedRows: Int = 0): Result<ImportParseResult> {
        val existing = transactionRepository.getAllTransactions().getOrElse { return Result.failure(it) }
        val existingKeys = existing.map { "${it.date.take(10)}|${it.amount}|${it.merchant?.lowercase()?.trim()}" }.toSet()
        var dup = 0
        val marked = drafts.map {
            val key = "${it.date}|${it.amount}|${it.merchant.lowercase().trim()}"
            val isDup = key in existingKeys
            if (isDup) dup++
            it.copy(isDuplicate = isDup, isSelected = !isDup)
        }
        return Result.success(ImportParseResult(marked, skippedRows = skippedRows, duplicateCount = dup))
    }
}
