package com.ssajudn.bareuang.utils

import com.ssajudn.bareuang.domain.model.AppCurrency
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    private val indonesianLocale = Locale("id", "ID")
    private val usLocale = Locale.US

    private val rupiahFormat = NumberFormat.getCurrencyInstance(indonesianLocale).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    private val dollarFormat = NumberFormat.getCurrencyInstance(usLocale).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    @Volatile
    private var activeCurrency: AppCurrency = AppCurrency.IDR

    fun setActiveCurrency(currency: AppCurrency) {
        activeCurrency = currency
    }

    fun getActiveCurrency(): AppCurrency = activeCurrency

    fun getCurrencySymbol(currency: AppCurrency = activeCurrency): String = currency.symbol

    /**
     * Format number to Currency string:
     * e.g. IDR 50000 -> "Rp 50.000"
     * e.g. USD 50000 -> "$ 50,000"
     */
    fun formatCurrency(amount: Long, currency: AppCurrency = activeCurrency): String {
        return when (currency) {
            AppCurrency.IDR -> {
                rupiahFormat.format(amount)
                    .replace("Rp", "Rp ")
                    .trim()
            }
            AppCurrency.USD -> {
                dollarFormat.format(amount)
                    .replace("$", "$ ")
                    .trim()
            }
        }
    }

    /**
     * Format number to active currency (kept for backwards compatibility).
     */
    fun formatRupiah(amount: Long): String {
        return formatCurrency(amount, activeCurrency)
    }

    /**
     * Parse raw currency input string (e.g. "Rp 50.000" or "$ 50,000" or "50000") to Long
     */
    fun parseAmount(input: String): Long {
        val cleanString = input.replace(Regex("[^0-9]"), "")
        return cleanString.toLongOrNull() ?: 0L
    }

    /**
     * Compact format for large numbers:
     * For IDR: 1.500.000 -> "1.5 jt", 50.000 -> "50 rb"
     * For USD: 1.500.000 -> "1.5 M", 50.000 -> "50 K"
     */
    fun formatCompact(amount: Long, currency: AppCurrency = activeCurrency): String {
        return when (currency) {
            AppCurrency.IDR -> {
                when {
                    amount >= 1_000_000_000 -> String.format(indonesianLocale, "%.1f M", amount / 1_000_000_000.0)
                    amount >= 1_000_000 -> String.format(indonesianLocale, "%.1f jt", amount / 1_000_000.0)
                    amount >= 1_000 -> String.format(indonesianLocale, "%.0f rb", amount / 1_000.0)
                    else -> amount.toString()
                }
            }
            AppCurrency.USD -> {
                when {
                    amount >= 1_000_000_000 -> String.format(usLocale, "%.1f B", amount / 1_000_000_000.0)
                    amount >= 1_000_000 -> String.format(usLocale, "%.1f M", amount / 1_000_000.0)
                    amount >= 1_000 -> String.format(usLocale, "%.0f K", amount / 1_000.0)
                    else -> amount.toString()
                }
            }
        }
    }
}
