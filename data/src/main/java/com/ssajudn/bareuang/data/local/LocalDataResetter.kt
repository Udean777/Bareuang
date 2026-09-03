package com.ssajudn.bareuang.data.local

import com.ssajudn.bareuang.data.local.room.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wipes every local table. Used by the settings "reset data" action;
 * Core financial data is local; this reset clears the local app state.
 */
@Singleton
class LocalDataResetter @Inject constructor(
    private val db: AppDatabase,
    private val dailyPacingPreferences: DailyPacingPreferences
) : com.ssajudn.bareuang.domain.port.LocalDataResetPort {
    override suspend fun wipe() {
        db.clearAllTables()
        dailyPacingPreferences.reset()
    }
}
