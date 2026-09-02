package com.ssajudn.bareuang.data.local

import android.content.Context
import androidx.core.content.edit
import com.ssajudn.bareuang.domain.model.AppCurrency
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyPreferences @Inject constructor(
    @ApplicationContext context: Context
) : com.ssajudn.bareuang.domain.port.CurrencyPreferencesPort {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val _currency = MutableStateFlow(readCurrency())
    override val currency: StateFlow<AppCurrency> = _currency.asStateFlow()

    override fun setCurrency(currency: AppCurrency) {
        prefs.edit { putString(KEY_CURRENCY, currency.code) }
        _currency.value = currency
    }

    override fun getCurrency(): AppCurrency = _currency.value

    private fun readCurrency(): AppCurrency =
        AppCurrency.fromCode(prefs.getString(KEY_CURRENCY, null))

    companion object {
        private const val PREF_NAME = "bareuang_currency"
        private const val KEY_CURRENCY = "active_currency"
    }
}
