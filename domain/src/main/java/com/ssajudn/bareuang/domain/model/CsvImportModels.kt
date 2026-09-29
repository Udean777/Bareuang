package com.ssajudn.bareuang.domain.model

data class CsvColumnMapping(
    val date: Int? = null,
    val description: Int? = null,
    val amount: Int? = null,
    val debit: Int? = null,
    val credit: Int? = null,
    val type: Int? = null,
    val category: Int? = null,
) {
    val canParse: Boolean
        get() = date != null && description != null &&
            (amount != null || debit != null || credit != null)
}

data class CsvImportInspection(
    val columns: List<String>,
    val sampleRows: List<List<String>>,
    val hasHeader: Boolean,
    val autoMapping: CsvColumnMapping,
    val needsMapping: Boolean,
    val dataRowCount: Int,
    val error: CsvImportInspectionError? = null,
)

enum class CsvImportInspectionError { TOO_MANY_ROWS }

enum class CsvRowIssueReason {
    INVALID_DATE,
    MISSING_DESCRIPTION,
    INVALID_AMOUNT,
    AMOUNT_CONFLICT,
    BALANCE_SUMMARY,
}

data class CsvRowIssue(
    val rowNumber: Int,
    val reason: CsvRowIssueReason,
)

data class CsvImportParseOutput(
    val drafts: List<ImportDraft>,
    val issues: List<CsvRowIssue>,
)

data class CsvImportSummary(
    val importedCount: Int,
    val duplicateCount: Int,
    val invalidCount: Int,
    val walletName: String,
)
