package com.ssajudn.bareuang.domain.model

data class ImportDraft(
    val id: String,
    val amount: Long,
    val type: TransactionType,
    val category: TransactionCategory,
    val merchant: String,
    val date: String, // yyyy-MM-dd
    val rawLine: String,
    val isDuplicate: Boolean = false,
    val isSelected: Boolean = true
)

data class ImportParseResult(
    val drafts: List<ImportDraft>,
    val skippedRows: Int,
    val duplicateCount: Int
)
