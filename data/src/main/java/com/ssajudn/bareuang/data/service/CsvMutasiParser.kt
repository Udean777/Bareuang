package com.ssajudn.bareuang.data.service

import com.ssajudn.bareuang.domain.model.CsvColumnMapping
import com.ssajudn.bareuang.domain.model.CsvImportInspection
import com.ssajudn.bareuang.domain.model.CsvImportParseOutput
import com.ssajudn.bareuang.domain.model.CsvRowIssue
import com.ssajudn.bareuang.domain.model.CsvRowIssueReason
import com.ssajudn.bareuang.domain.model.ImportDraft
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType
import com.ssajudn.bareuang.domain.port.CsvParserPort
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvMutasiParser @Inject constructor() : CsvParserPort {

    private data class CsvTable(
        val delimiter: Char,
        val records: List<List<String>>,
        val hasHeader: Boolean,
        val mapping: CsvColumnMapping,
        val positiveMeansIncome: Boolean = false,
        val tooManyRows: Boolean = false,
    )

    override fun inspect(csvText: String): CsvImportInspection {
        val table = readTable(csvText)
        if (table.tooManyRows) {
            return CsvImportInspection(
                columns = emptyList(),
                sampleRows = emptyList(),
                hasHeader = false,
                autoMapping = CsvColumnMapping(),
                needsMapping = true,
                dataRowCount = 0,
                error = com.ssajudn.bareuang.domain.model.CsvImportInspectionError.TOO_MANY_ROWS,
            )
        }
        val columnCount = maxOf(1, table.records.maxOfOrNull { it.size } ?: 1)
        val headers = if (table.hasHeader) {
            (0 until columnCount).map { index ->
                table.records.firstOrNull()?.getOrNull(index)?.ifBlank { "Kolom ${index + 1}" }
                    ?: "Kolom ${index + 1}"
            }
        } else {
            (0 until columnCount).map { "Kolom ${it + 1}" }
        }
        val dataRecords = table.records.drop(if (table.hasHeader) 1 else 0)
        return CsvImportInspection(
            columns = headers,
            sampleRows = dataRecords.take(5).map { it.padTo(columnCount) },
            hasHeader = table.hasHeader,
            autoMapping = table.mapping,
            needsMapping = !table.mapping.canParse,
            dataRowCount = dataRecords.size,
        )
    }

    override fun parse(csvText: String, mapping: CsvColumnMapping): CsvImportParseOutput {
        if (!mapping.canParse) return CsvImportParseOutput(emptyList(), emptyList())
        val table = readTable(csvText)
        val rows = table.records.drop(if (table.hasHeader) 1 else 0)
        val drafts = mutableListOf<ImportDraft>()
        val issues = mutableListOf<CsvRowIssue>()

        rows.forEachIndexed { index, columns ->
            val rowNumber = index + if (table.hasHeader) 2 else 1
            val merchant = mapping.description?.let { columns.getOrNull(it).orEmpty().trim() }.orEmpty()
            if (merchant.contains("SALDO AWAL", true) || merchant.contains("SALDO AKHIR", true)) {
                issues += CsvRowIssue(rowNumber, CsvRowIssueReason.BALANCE_SUMMARY)
                return@forEachIndexed
            }
            val dateText = mapping.date?.let { columns.getOrNull(it).orEmpty() }.orEmpty()
            val date = parseDateOrNull(dateText)
            if (date == null) {
                issues += CsvRowIssue(rowNumber, CsvRowIssueReason.INVALID_DATE)
                return@forEachIndexed
            }

            if (merchant.isBlank()) {
                issues += CsvRowIssue(rowNumber, CsvRowIssueReason.MISSING_DESCRIPTION)
                return@forEachIndexed
            }

            val debitValue = mapping.debit?.let { columns.getOrNull(it)?.let(::parseAmount) ?: 0L }
            val creditValue = mapping.credit?.let { columns.getOrNull(it)?.let(::parseAmount) ?: 0L }
            val amountText = mapping.amount?.let { columns.getOrNull(it).orEmpty() }.orEmpty()
            val parsedAmount: Long
            val inferredType: TransactionType
            if (debitValue != null || creditValue != null) {
                val debit = kotlin.math.abs(debitValue ?: 0L)
                val credit = kotlin.math.abs(creditValue ?: 0L)
                if (debit > 0 && credit > 0) {
                    issues += CsvRowIssue(rowNumber, CsvRowIssueReason.AMOUNT_CONFLICT)
                    return@forEachIndexed
                }
                parsedAmount = when {
                    debit > 0 -> debit
                    credit > 0 -> credit
                    else -> 0L
                }
                inferredType = if (credit > 0) TransactionType.INCOME else TransactionType.EXPENSE
            } else {
                val signedAmount = parseAmount(amountText)
                parsedAmount = kotlin.math.abs(signedAmount)
                val explicitType = mapping.type?.let { columns.getOrNull(it).orEmpty() }
                    ?.let(::parseType)
                inferredType = explicitType ?: when {
                    signedAmount < 0 -> TransactionType.EXPENSE
                    table.positiveMeansIncome -> TransactionType.INCOME
                    else -> TransactionType.EXPENSE
                }
            }
            if (parsedAmount <= 0L) {
                issues += CsvRowIssue(rowNumber, CsvRowIssueReason.INVALID_AMOUNT)
                return@forEachIndexed
            }

            val categoryValue = mapping.category?.let { columns.getOrNull(it).orEmpty() }
            val category = categoryValue?.let(::parseCategory) ?: guessCategory(merchant)
            drafts += ImportDraft(
                id = UUID.randomUUID().toString(),
                amount = parsedAmount,
                type = inferredType,
                category = category,
                merchant = merchant,
                date = date,
                rawLine = columns.joinToString(table.delimiter.toString()),
            )
        }
        return CsvImportParseOutput(drafts, issues)
    }

    fun parse(csvText: String): List<ImportDraft> {
        val inspection = inspect(csvText)
        return if (inspection.autoMapping.canParse) parse(csvText, inspection.autoMapping).drafts else emptyList()
    }

    override fun parseWithStats(csvText: String): Pair<List<ImportDraft>, Int> {
        val inspection = inspect(csvText)
        if (!inspection.autoMapping.canParse) return emptyList<ImportDraft>() to inspection.dataRowCount
        val result = parse(csvText, inspection.autoMapping)
        return result.drafts to result.issues.size
    }

    private fun readTable(csvText: String): CsvTable {
        if (csvText.length > MAX_FILE_CHARS) return CsvTable(',', emptyList(), false, CsvColumnMapping())
        val normalized = csvText.removePrefix("\uFEFF").replace("\u0000", "")
        val delimiter = detectDelimiter(normalized)
        val records = parseRecords(normalized, delimiter)
            .filter { row -> row.any { it.isNotBlank() } }
            .take(MAX_ROWS + 1)
        if (records.size > MAX_ROWS) return CsvTable(delimiter, emptyList(), false, CsvColumnMapping(), tooManyRows = true)

        if (records.isEmpty()) return CsvTable(delimiter, emptyList(), false, CsvColumnMapping())
        val headerMapping = detectHeaderMapping(records.first())
        val hasRecognizedHeader = headerMapping.first
        if (hasRecognizedHeader) {
            return CsvTable(delimiter, records, true, headerMapping.second)
        }

        val firstRowMapping = inferMapping(records.first())
        if (firstRowMapping.canParse) return CsvTable(delimiter, records, false, firstRowMapping, positiveMeansIncome = true)

        // Unknown headers are still treated as a header when the next row has transaction-like values.
        val secondRowMapping = records.getOrNull(1)?.let(::inferMapping)
        if (secondRowMapping?.canParse == true) {
            return CsvTable(delimiter, records, true, secondRowMapping, positiveMeansIncome = true)
        }
        return CsvTable(delimiter, records, false, CsvColumnMapping())
    }

    private fun detectDelimiter(text: String): Char {
        val candidates = listOf(',', ';', '\t')
        return candidates.maxByOrNull { delimiter ->
            val sample = parseRecords(text, delimiter).take(20).filter { it.any(String::isNotBlank) }
            if (sample.isEmpty()) return@maxByOrNull 0
            val widths = sample.map { it.size }
            val multiColumnRows = widths.count { it > 1 }
            val consistentRows = widths.groupingBy { it }.eachCount().values.maxOrNull() ?: 0
            multiColumnRows * 10 + consistentRows
        } ?: ','
    }

    private fun detectHeaderMapping(header: List<String>): Pair<Boolean, CsvColumnMapping> {
        val indices = mutableMapOf<String, Int>()
        header.forEachIndexed { index, value ->
            val key = normalizeHeader(value)
            val role = when (key) {
                in dateHeaders -> "date"
                in descriptionHeaders -> "description"
                in amountHeaders -> "amount"
                in debitHeaders -> "debit"
                in creditHeaders -> "credit"
                in typeHeaders -> "type"
                in categoryHeaders -> "category"
                else -> null
            }
            if (role != null && role !in indices) indices[role] = index
        }
        val recognized = indices.isNotEmpty()
        return recognized to indices.toMapping()
    }

    private fun inferMapping(row: List<String>): CsvColumnMapping {
        val dateIndex = row.indexOfFirst { parseDateOrNull(it) != null }.takeIf { it >= 0 }
        val amountIndex = row.indices.firstOrNull { index ->
            index != dateIndex && parseAmount(row[index]) != 0L
        }
        val descriptionIndex = row.indices.firstOrNull { index ->
            index != dateIndex && index != amountIndex && row[index].any(Char::isLetter)
        }
        return CsvColumnMapping(date = dateIndex, description = descriptionIndex, amount = amountIndex)
    }

    private fun parseRecords(text: String, delimiter: Char): List<List<String>> {
        val records = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var index = 0
        while (index < text.length) {
            val char = text[index]
            when {
                char == '"' && quoted && index + 1 < text.length && text[index + 1] == '"' -> {
                    cell.append('"')
                    index++
                }
                char == '"' -> quoted = !quoted
                char == delimiter && !quoted -> {
                    row += cell.toString().trim()
                    cell.clear()
                }
                (char == '\n' || char == '\r') && !quoted -> {
                    row += cell.toString().trim()
                    cell.clear()
                    records += row.toList()
                    row.clear()
                    if (char == '\r' && index + 1 < text.length && text[index + 1] == '\n') index++
                }
                else -> cell.append(char)
            }
            index++
        }
        if (cell.isNotEmpty() || row.isNotEmpty()) {
            row += cell.toString().trim()
            records += row.toList()
        }
        return records
    }

    private fun Map<String, Int>.toMapping() = CsvColumnMapping(
        date = get("date"),
        description = get("description"),
        amount = get("amount"),
        debit = get("debit"),
        credit = get("credit"),
        type = get("type"),
        category = get("category"),
    )

    private fun List<String>.padTo(size: Int): List<String> = take(size).let { values ->
        values + List((size - values.size).coerceAtLeast(0)) { "" }
    }

    internal fun splitCsvLine(line: String, delimiter: String): List<String> =
        parseRecords(line, delimiter.firstOrNull() ?: ',').firstOrNull().orEmpty()

    internal fun parseAmount(raw: String): Long {
        if (raw.isBlank()) return 0L
        val normalized = raw.trim().replace("Rp", "", true).replace("IDR", "", true)
        val negative = normalized.startsWith("-") || (normalized.startsWith("(") && normalized.endsWith(")"))
        val digitsAndSeparators = normalized.filter { it.isDigit() || it == '.' || it == ',' }
        if (digitsAndSeparators.isEmpty()) return 0L
        val lastSeparator = maxOf(digitsAndSeparators.lastIndexOf(','), digitsAndSeparators.lastIndexOf('.'))
        val fractionLength = if (lastSeparator >= 0) digitsAndSeparators.length - lastSeparator - 1 else 0
        val cleaned = if (fractionLength == 2) {
            digitsAndSeparators.substring(0, lastSeparator).filter(Char::isDigit)
        } else {
            digitsAndSeparators.filter(Char::isDigit)
        }
        val amount = cleaned.toLongOrNull() ?: 0L
        return if (negative) -amount else amount
    }

    internal fun parseDate(raw: String): String = parseDateOrNull(raw) ?: ""

    internal fun parseDateOrNull(raw: String): String? {
        val value = raw.trim()
        if (value.isBlank()) return null
        val patterns = listOf("dd/MM/yyyy", "d/M/yyyy", "dd-MM-yyyy", "d-M-yyyy", "yyyy-MM-dd", "dd.MM.yyyy", "dd MMM yyyy", "d MMM yyyy")
        for (pattern in patterns) {
            for (locale in listOf(Locale.ENGLISH, Locale.forLanguageTag("id-ID"))) {
                runCatching { LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern, locale)) }
                    .getOrNull()?.let { return it.toString() }
            }
        }
        return runCatching { LocalDate.parse(value.take(10)).toString() }.getOrNull()
    }

    private fun parseType(value: String): TransactionType? = when (normalizeHeader(value)) {
        "in", "income", "pemasukan", "masuk", "credit", "kredit" -> TransactionType.INCOME
        "out", "expense", "pengeluaran", "keluar", "debit", "debet" -> TransactionType.EXPENSE
        else -> null
    }

    private fun parseCategory(value: String): TransactionCategory? {
        val normalized = normalizeHeader(value)
        return TransactionCategory.entries.firstOrNull { normalizeHeader(it.name) == normalized }
    }

    private fun normalizeHeader(value: String): String = value
        .removePrefix("\uFEFF")
        .lowercase(Locale.ROOT)
        .filter(Char::isLetterOrDigit)

    private fun guessCategory(merchant: String): TransactionCategory {
        val lower = merchant.lowercase(Locale.ROOT)
        return when {
            lower.containsAny("indomaret", "alfamart", "supermarket", "hypermart", "toko", "mart", "belanja") -> TransactionCategory.SHOPPING
            lower.containsAny("kopi", "cafe", "restoran", "bakso", "ayam", "mcdonald", "kfc", "gofood", "grabfood", "nasi", "warung", "mie", "sate", "coffee") -> TransactionCategory.FOOD
            lower.containsAny("bensin", "spbu", "pertamina", "shell", "grab", "gojek", "parkir", "tol", "krl", "mrt", "ojek") -> TransactionCategory.TRANSPORT
            lower.containsAny("pln", "listrik", "pdam", "wifi", "indihome", "telkomsel", "pulsa", "kuota") -> TransactionCategory.BILLS
            lower.containsAny("bioskop", "cinema", "xxi", "cgv", "game", "steam", "playstation") -> TransactionCategory.ENTERTAINMENT
            lower.containsAny("gaji", "salary", "upah") -> TransactionCategory.SALARY
            else -> TransactionCategory.OTHER
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean = keywords.any { contains(it) }

    private companion object {
        const val MAX_FILE_CHARS = 5 * 1024 * 1024
        const val MAX_ROWS = 5000
        val dateHeaders = setOf("tanggal", "tgl", "date", "transactiondate", "bookingdate", "valuedate", "postingdate", "transdate")
        val descriptionHeaders = setOf("keterangan", "description", "deskripsi", "merchant", "detail", "details", "uraian", "narration", "remarks", "remark", "transactiondetails", "transactiondescription", "particulars")
        val amountHeaders = setOf("jumlah", "amount", "nominal", "value", "mutasi", "nilai", "transactionamount", "amountbranch")
        val debitHeaders = setOf("debit", "debet", "withdrawal", "withdrawals")
        val creditHeaders = setOf("kredit", "credit", "deposit", "depositamount")
        val typeHeaders = setOf("tipe", "type", "jenis", "transactiontype")
        val categoryHeaders = setOf("kategori", "category")
    }
}
