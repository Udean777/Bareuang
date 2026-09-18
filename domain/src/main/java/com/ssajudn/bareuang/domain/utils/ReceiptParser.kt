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
        val numeric = Regex("\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})\\b")
            .find(rawText)
            ?.let { match ->
                val day = match.groupValues[1].toIntOrNull() ?: return@let null
                val month = match.groupValues[2].toIntOrNull() ?: return@let null
                val year = match.groupValues[3].toIntOrNull() ?: return@let null
                val normalizedYear = if (year < 100) 2000 + year else year
                runCatching { java.time.LocalDate.of(normalizedYear, month, day).toString() }.getOrNull()
            }
        if (numeric != null) return numeric

        val iso = Regex("\\b(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})\\b").find(rawText)
            ?: return null
        val year = iso.groupValues[1].toIntOrNull() ?: return null
        val month = iso.groupValues[2].toIntOrNull() ?: return null
        val day = iso.groupValues[3].toIntOrNull() ?: return null
        return runCatching { java.time.LocalDate.of(year, month, day).toString() }.getOrNull()
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
