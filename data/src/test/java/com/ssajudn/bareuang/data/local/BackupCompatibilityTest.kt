package com.ssajudn.bareuang.data.local

import com.google.gson.Gson
import com.ssajudn.bareuang.data.local.room.LocalTransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** Legacy v1 payloads may contain ownerId; guest-only parsing must ignore it. */
class BackupCompatibilityTest {
    @Test
    fun legacyEntityWithOwnerId_isReadableWithoutReintroducingIdentity() {
        val json = """
            {"id":"tx-1","amount":12500,"type":"EXPENSE","category":"FOOD",
             "merchant":"Warung","date":"2026-09-03","notes":null,"receiptUrl":null,
             "walletId":null,"toWalletId":null,"isSynced":false,"ownerId":"legacy-user"}
        """.trimIndent()

        val entity = Gson().fromJson(json, LocalTransactionEntity::class.java)

        assertEquals("tx-1", entity.id)
        assertEquals(12500L, entity.amount)
        assertFalse(LocalTransactionEntity::class.java.declaredFields.any { it.name == "ownerId" })
    }
}
