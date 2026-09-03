package com.ssajudn.bareuang.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        LocalTransactionEntity::class,
        LocalDueBillEntity::class,
        LocalBudgetEntity::class,
        LocalCategoryBudgetEntity::class,
        LocalGoalEntity::class,
        LocalWalletEntity::class
    ],
    version = 16,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun dueBillDao(): DueBillDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun walletDao(): WalletDao

    companion object {
        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE local_transactions ADD COLUMN toWalletId TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE local_due_bills ADD COLUMN paidWalletId TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE local_transactions ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE local_due_bills ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE local_budgets ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE local_goals ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE local_wallets ADD COLUMN ownerId TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS outbox (id TEXT NOT NULL PRIMARY KEY, ownerId TEXT NOT NULL, entityType TEXT NOT NULL, entityId TEXT NOT NULL, payloadJson TEXT NOT NULL, idempotencyKey TEXT NOT NULL, state TEXT NOT NULL, attempts INTEGER NOT NULL, createdAt INTEGER NOT NULL, nextRetryAt INTEGER)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_outbox_state ON outbox(state)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_outbox_ownerId ON outbox(ownerId)")
            }
        }

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS cached_translations (cacheKey TEXT NOT NULL PRIMARY KEY, sourceLang TEXT NOT NULL, targetLang TEXT NOT NULL, originalText TEXT NOT NULL, translatedText TEXT NOT NULL, createdAt INTEGER NOT NULL)")
            }
        }

        val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS local_category_budgets (monthYear TEXT NOT NULL, category TEXT NOT NULL, limitAmount INTEGER NOT NULL, isSynced INTEGER NOT NULL DEFAULT 0, ownerId TEXT NOT NULL DEFAULT '', PRIMARY KEY(monthYear, category))")
            }
        }

        val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE local_transactions ADD COLUMN recurringInterval TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE local_transactions ADD COLUMN isRecurringParent INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE local_transactions ADD COLUMN parentRecurringId TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE local_transactions ADD COLUMN nextOccurrenceDate TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Outbox sync was never shipped as a feature; drop the dead table.
                db.execSQL("DROP TABLE IF EXISTS outbox")
            }
        }

        val MIGRATION_13_14 = object : androidx.room.migration.Migration(13, 14) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_local_transactions_date` ON `local_transactions` (`date`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_local_transactions_amount` ON `local_transactions` (`amount`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_local_transactions_merchant` ON `local_transactions` (`merchant`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_local_transactions_date_amount_merchant` ON `local_transactions` (`date`, `amount`, `merchant`)")
            }
        }

        val MIGRATION_14_15 = object : androidx.room.migration.Migration(14, 15) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Translation caching was never consumed; remove its residue safely.
                db.execSQL("DROP TABLE IF EXISTS cached_translations")
            }
        }

        /** Removes unshipped multi-user ownership while preserving every record. */
        val MIGRATION_15_16 = object : androidx.room.migration.Migration(15, 16) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                rebuild(db, "local_transactions", "id TEXT NOT NULL, amount INTEGER NOT NULL, type TEXT NOT NULL, category TEXT NOT NULL, merchant TEXT, date TEXT NOT NULL, notes TEXT, receiptUrl TEXT, walletId TEXT, toWalletId TEXT, isSynced INTEGER NOT NULL, recurringInterval TEXT NOT NULL, isRecurringParent INTEGER NOT NULL, parentRecurringId TEXT, nextOccurrenceDate TEXT, PRIMARY KEY(id)", "id, amount, type, category, merchant, date, notes, receiptUrl, walletId, toWalletId, isSynced, recurringInterval, isRecurringParent, parentRecurringId, nextOccurrenceDate")
                rebuild(db, "local_due_bills", "id TEXT NOT NULL, providerName TEXT NOT NULL, providerIconUrl TEXT, totalAmount INTEGER NOT NULL, dueDate TEXT NOT NULL, status TEXT NOT NULL, paidWalletId TEXT, isRecurring INTEGER NOT NULL, recurringInterval TEXT NOT NULL, notes TEXT, isSynced INTEGER NOT NULL, PRIMARY KEY(id)", "id, providerName, providerIconUrl, totalAmount, dueDate, status, paidWalletId, isRecurring, recurringInterval, notes, isSynced")
                rebuild(db, "local_budgets", "monthYear TEXT NOT NULL, monthlyLimit INTEGER NOT NULL, isSynced INTEGER NOT NULL, PRIMARY KEY(monthYear)", "monthYear, monthlyLimit, isSynced")
                rebuild(db, "local_category_budgets", "monthYear TEXT NOT NULL, category TEXT NOT NULL, limitAmount INTEGER NOT NULL, isSynced INTEGER NOT NULL, PRIMARY KEY(monthYear, category)", "monthYear, category, limitAmount, isSynced")
                rebuild(db, "local_goals", "id TEXT NOT NULL, name TEXT NOT NULL, targetAmount INTEGER NOT NULL, currentAmount INTEGER NOT NULL, targetDate TEXT, colorHex TEXT NOT NULL, notes TEXT, isSynced INTEGER NOT NULL, PRIMARY KEY(id)", "id, name, targetAmount, currentAmount, targetDate, colorHex, notes, isSynced")
                rebuild(db, "local_wallets", "id TEXT NOT NULL, name TEXT NOT NULL, balance INTEGER NOT NULL, colorHex TEXT NOT NULL, iconName TEXT NOT NULL, createdAt TEXT NOT NULL, isSynced INTEGER NOT NULL, PRIMARY KEY(id)", "id, name, balance, colorHex, iconName, createdAt, isSynced")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_transactions_date ON local_transactions(date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_transactions_amount ON local_transactions(amount)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_transactions_merchant ON local_transactions(merchant)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_local_transactions_date_amount_merchant ON local_transactions(date, amount, merchant)")
            }

            private fun rebuild(db: androidx.sqlite.db.SupportSQLiteDatabase, table: String, columns: String, selected: String) {
                val replacement = "${table}_guest_only"
                db.execSQL("CREATE TABLE $replacement ($columns)")
                db.execSQL("INSERT INTO $replacement ($selected) SELECT $selected FROM $table")
                db.execSQL("DROP TABLE $table")
                db.execSQL("ALTER TABLE $replacement RENAME TO $table")
            }
        }
    }
}
