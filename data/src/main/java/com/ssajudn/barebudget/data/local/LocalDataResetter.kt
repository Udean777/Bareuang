package com.ssajudn.barebudget.data.local

import com.ssajudn.barebudget.data.local.room.AppDatabase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wipes every local table. Used by the settings "reset data" action;
 * the app is fully offline, so this is the equivalent of signing out.
 */
@Singleton
class LocalDataResetter @Inject constructor(
    private val db: AppDatabase
) {
    suspend fun wipe() {
        db.clearAllTables()
    }
}
