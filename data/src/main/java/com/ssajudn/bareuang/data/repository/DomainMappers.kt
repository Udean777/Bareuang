package com.ssajudn.bareuang.data.repository

import android.util.Log
import com.ssajudn.bareuang.domain.model.RecurringInterval
import com.ssajudn.bareuang.domain.model.TransactionCategory
import com.ssajudn.bareuang.domain.model.TransactionType

object DomainMappers {
    fun safeTransactionType(raw: String?): TransactionType =
        runCatching { TransactionType.valueOf(raw.orEmpty()) }
            .onFailure { android.util.Log.w("DomainMappers", "Unknown TransactionType: $raw, fallback to EXPENSE: ${it.message}") }
            .getOrDefault(TransactionType.EXPENSE)

    fun safeCategory(raw: String?): TransactionCategory =
        runCatching { TransactionCategory.valueOf(raw.orEmpty()) }
            .onFailure { android.util.Log.w("DomainMappers", "Unknown TransactionCategory: $raw, fallback to OTHER: ${it.message}") }
            .getOrDefault(TransactionCategory.OTHER)

    fun safeRecurringInterval(raw: String?): RecurringInterval =
        runCatching { RecurringInterval.valueOf(raw.orEmpty()) }
            .onFailure { android.util.Log.w("DomainMappers", "Unknown RecurringInterval: $raw, fallback to NONE: ${it.message}") }
            .getOrDefault(RecurringInterval.NONE)

    fun safeDueBillStatus(raw: String?): com.ssajudn.bareuang.domain.model.DueBillStatus =
        runCatching { com.ssajudn.bareuang.domain.model.DueBillStatus.valueOf(raw.orEmpty()) }
            .getOrDefault(com.ssajudn.bareuang.domain.model.DueBillStatus.UNPAID)

    const val DEFAULT_WALLET_NAME = com.ssajudn.bareuang.domain.AppConfig.DEFAULT_WALLET_NAME
    const val DEFAULT_WALLET_COLOR = com.ssajudn.bareuang.domain.AppConfig.DEFAULT_WALLET_COLOR
    const val DEFAULT_ICON = com.ssajudn.bareuang.domain.AppConfig.DEFAULT_WALLET_ICON
    const val DEFAULT_GOAL_COLOR = com.ssajudn.bareuang.domain.AppConfig.DEFAULT_GOAL_COLOR
}
