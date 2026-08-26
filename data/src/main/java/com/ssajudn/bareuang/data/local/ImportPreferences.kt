package com.ssajudn.bareuang.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("bareuang_import", Context.MODE_PRIVATE)

    private val _importCount = MutableStateFlow(prefs.getInt(KEY_COUNT, 0))
    val importCount: StateFlow<Int> = _importCount.asStateFlow()

    fun increment(count: Int) {
        val newVal = _importCount.value + count
        prefs.edit().putInt(KEY_COUNT, newVal).putLong(KEY_LAST_AT, System.currentTimeMillis()).apply()
        _importCount.value = newVal
    }

    fun lastImportAt(): Long = prefs.getLong(KEY_LAST_AT, 0L)

    companion object {
        private const val KEY_COUNT = "import_count"
        private const val KEY_LAST_AT = "last_import_at"
    }
}
