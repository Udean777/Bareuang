package com.ssajudn.bareuang.utils

import com.ssajudn.bareuang.domain.model.AppCurrency

object CurrencyFormatter {

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
     * Delegates to domain formatter (single source of truth)
     */
    fun formatCurrency(amount: Long, currency: AppCurrency = activeCurrency): String =
        com.ssajudn.bareuang.domain.utils.DomainCurrencyFormatter.format(amount, currency)

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
    fun formatCompact(amount: Long, currency: AppCurrency = activeCurrency): String =
        com.ssajudn.bareuang.domain.utils.DomainCurrencyFormatter.formatCompact(amount, currency)
}
