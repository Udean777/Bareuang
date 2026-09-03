package com.ssajudn.bareuang.data.local.room

import android.content.Context
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

/** Verifies the latest shipped schema upgrade without destructive fallback. */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate13To14_preservesTablesAndRows() {
        helper.createDatabase("migration-test", 13).apply {
            execSQL("INSERT INTO local_wallets (id, name, balance, colorHex, iconName, createdAt, isSynced, ownerId) VALUES ('w1', 'Main', 1000, '#000000', 'wallet', '2026-01-01', 0, 'owner')")
            close()
        }
        helper.runMigrationsAndValidate("migration-test", 14, true, AppDatabase.MIGRATION_13_14)
    }

    @Test
    fun migrate15To16_removesOwnerColumnsWithoutDroppingRows() {
        helper.createDatabase("guest-only-migration-test", 15).apply {
            execSQL("INSERT INTO local_wallets (id, name, balance, colorHex, iconName, createdAt, isSynced, ownerId) VALUES ('w1', 'Main', 1000, '#000000', 'wallet', '2026-01-01', 0, 'legacy-owner')")
            close()
        }
        helper.runMigrationsAndValidate("guest-only-migration-test", 16, true, AppDatabase.MIGRATION_15_16).apply {
            query("SELECT id, balance FROM local_wallets").use { cursor ->
                assertEquals(1, cursor.count)
                cursor.moveToFirst()
                assertEquals("w1", cursor.getString(0))
                assertEquals(1000L, cursor.getLong(1))
            }
            query("PRAGMA table_info(local_wallets)").use { cursor ->
                val names = generateSequence { if (cursor.moveToNext()) cursor.getString(1) else null }.toList()
                org.junit.Assert.assertFalse(names.contains("ownerId"))
            }
            close()
        }
    }
}
