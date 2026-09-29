package com.ssajudn.bareuang.domain.model

data class BackupRecordPreview(
    val total: Int,
    val new: Int,
    val replaced: Int,
)

data class BackupRestorePreview(
    val id: String,
    val backupDate: String,
    val appVersion: String,
    val currencyCode: String?,
    val isEncrypted: Boolean,
    val hasReceiptReferences: Boolean,
    val wallets: BackupRecordPreview,
    val transactions: BackupRecordPreview,
    val dueBills: BackupRecordPreview,
    val budgets: BackupRecordPreview,
    val categoryBudgets: BackupRecordPreview,
    val goals: BackupRecordPreview,
)

data class BackupRestoreSummary(
    val added: Int,
    val replaced: Int,
    val currencyCode: String?,
    val currencyApplied: Boolean = true,
)
