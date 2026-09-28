package com.ssajudn.bareuang.domain.utils

import com.ssajudn.bareuang.domain.model.ParsedReceipt
import com.ssajudn.bareuang.domain.model.TransactionCategory
import java.util.regex.Pattern

object ReceiptParser {

    fun parse(rawText: String): ParsedReceipt {
        val lines = rawText.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val merchantName = extractMerchant(lines)
        val totalAmount = extractTotalAmount(lines)
        val suggestedCategory = guessCategory(rawText, merchantName)
        val date = extractDate(rawText)

        return ParsedReceipt(
            merchantName = merchantName,
            totalAmount = totalAmount,
            suggestedCategory = suggestedCategory,
            rawText = rawText,
            date = date,
        )
    }

    private fun extractMerchant(lines: List<String>): String {
        for (i in 0 until minOf(3, lines.size)) {
            val line = lines[i]
            if (!line.matches(Regex("(?i).*(struk|receipt|tanggal|kasir|pos|selamat|welcome|nota).*"))) {
                if (line.length >= 3 && line.any { it.isLetter() }) {
                    return cleanMerchantName(line)
                }
            }
        }
        return lines.firstOrNull() ?: "Merchant"
    }

    private fun cleanMerchantName(name: String): String =
        name.replace(Regex("[^a-zA-Z0-9 .&'-]"), "").trim()

    private fun extractTotalAmount(lines: List<String>): Long {
        val totalKeywords = listOf(
            "TOTAL",
            "GRAND TOTAL",
            "JUMLAH",
            "BAYAR",
            "TAGIHAN",
            "HARGA JUAL",
            "NETTO",
            "DEBIT",
            "CASH",
        )
        val amountsFound = mutableListOf<Long>()

        for (line in lines) {
            val upper = line.uppercase()
            if (totalKeywords.any { upper.containsWord(it) }) {
                extractAmountFromLine(line).takeIf { it > 0 }?.let(amountsFound::add)
            }
        }

        if (amountsFound.isNotEmpty()) return amountsFound.maxOrNull() ?: 0L

        for (line in lines.reversed()) {
            extractAmountFromLine(line)
                .takeIf { it in 1000..50000000 }
                ?.let { return it }
        }

        return 0L
    }

    private fun extractAmountFromLine(line: String): Long {
        val pattern = Pattern.compile(
            "(?i)(?:rp\\.?[\\s]*)?([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,8})",
        )
        val matcher = pattern.matcher(line.replace('O', '0').replace('o', '0'))
        var maxAmount = 0L

        while (matcher.find()) {
            val match = matcher.group(1) ?: continue
            val parsed = match
                .replace(".", "")
                .replace(",", "")
                .toLongOrNull()
                ?: 0L
            if (parsed > maxAmount) maxAmount = parsed
        }
        return maxAmount
    }

    private fun extractDate(rawText: String): String? {
        val lines = rawText.lines()
        val excludedContext = Regex(
            "(?i)\\b(jatuh\\s+tempo|due|expired|kadaluarsa|kedaluwarsa|berlaku|valid\\s+(?:until|through|thru)|promo|periode|period)\\b",
        )
        val transactionDateLabel = Regex("(?i)\\b(tanggal|tgl|date|transaksi|transaction)\\b")

        lines.firstOrNull { line ->
            transactionDateLabel.containsMatchIn(line) && !excludedContext.containsMatchIn(line)
        }?.let(::parseDateFromText)?.let { return it }

        // Unlabelled dates are safe to use only when there is a single unambiguous candidate.
        val candidates = lines
            .filterNot(excludedContext::containsMatchIn)
            .flatMap(::findDatesInText)
            .distinct()
        return candidates.singleOrNull()
    }

    private fun parseDateFromText(text: String): String? = findDatesInText(text).firstOrNull()

    private fun findDatesInText(text: String): List<String> {
        val dates = mutableListOf<String>()
        val numericPattern = Regex("\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})\\b")
        numericPattern.findAll(text).forEach { match ->
            val day = match.groupValues[1].toIntOrNull() ?: return@forEach
            val month = match.groupValues[2].toIntOrNull() ?: return@forEach
            val year = match.groupValues[3].toIntOrNull() ?: return@forEach
            val normalizedYear = if (year < 100) 2000 + year else year
            runCatching { java.time.LocalDate.of(normalizedYear, month, day).toString() }
                .getOrNull()?.let(dates::add)
        }

        val isoPattern = Regex("\\b(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})\\b")
        isoPattern.findAll(text).forEach { match ->
            val year = match.groupValues[1].toIntOrNull() ?: return@forEach
            val month = match.groupValues[2].toIntOrNull() ?: return@forEach
            val day = match.groupValues[3].toIntOrNull() ?: return@forEach
            runCatching { java.time.LocalDate.of(year, month, day).toString() }
                .getOrNull()?.let(dates::add)
        }
        return dates
    }

    private fun guessCategory(rawText: String, merchant: String): TransactionCategory {
        val lower = ("$rawText $merchant").lowercase()
        return when {
            lower.containsAny("indomaret", "alfamart", "supermarket", "hypermart", "transmart", "toko", "belanja", "mart") ->
                TransactionCategory.SHOPPING
            lower.containsAny("kopi", "cafe", "restoran", "bakso", "ayam", "mcdonald", "kfc", "hokben", "gofood", "grabfood", "nasi", "warung", "mie", "sate", "kitchen", "coffee", "tea", "boba") ->
                TransactionCategory.FOOD
            lower.containsAny("bensin", "spbu", "pertamina", "shell", "grab", "gojek", "parkir", "tol", "krl", "mrt", "ojek") ->
                TransactionCategory.TRANSPORT
            lower.containsAny("pln", "listrik", "pdam", "air", "wifi", "indihome", "telkomsel", "pulsa", "kuota", "xl", "tri", "smartfren") ->
                TransactionCategory.BILLS
            lower.containsAny("bioskop", "cinema", "xxi", "cgv", "game", "steam", "playstation", "karaoke", "timezone") ->
                TransactionCategory.ENTERTAINMENT
            lower.containsAny("arisan", "kondangan", "sumbangan", "donasi", "infaq") ->
                TransactionCategory.SOCIAL
            else -> TransactionCategory.OTHER
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean =
        keywords.any { contains(it) }

    private fun String.containsWord(keyword: String): Boolean {
        val pattern = keyword.split(' ').joinToString("\\s+") { Regex.escape(it) }
        return Regex("(?i)(?<![A-Z0-9])$pattern(?![A-Z0-9])").containsMatchIn(this)
    }
}
