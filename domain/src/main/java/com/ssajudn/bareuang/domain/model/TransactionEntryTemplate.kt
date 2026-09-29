package com.ssajudn.bareuang.domain.model

data class TransactionEntryTemplate(
    val id: String,
    val name: String,
    val type: TransactionType,
    val walletId: String,
    val destinationWalletId: String? = null,
    val category: TransactionCategory,
    val merchant: String,
    val notes: String = "",
    val amount: Long? = null,
    val recurringInterval: RecurringInterval = RecurringInterval.NONE
)
