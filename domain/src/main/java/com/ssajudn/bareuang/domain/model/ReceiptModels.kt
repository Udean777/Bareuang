package com.ssajudn.bareuang.domain.model

data class ParsedReceipt(
    val merchantName: String,
    val totalAmount: Long,
    val suggestedCategory: TransactionCategory,
    val rawText: String,
    val date: String? = null,
    val items: List<String> = emptyList(),
)
