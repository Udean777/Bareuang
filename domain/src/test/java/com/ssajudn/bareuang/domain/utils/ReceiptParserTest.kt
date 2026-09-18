package com.ssajudn.bareuang.domain.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptParserTest {
    @Test
    fun `extract total`() {
        val text = "INDOMARET\nTOTAL Rp 45.000\nTunai 50.000"
        val parsed = ReceiptParser.parse(text)

        assertEquals(45000L, parsed.totalAmount)
        assertTrue(parsed.merchantName.contains("INDOMARET", true))
    }

    @Test
    fun `fallback largest`() {
        val text = "NOTA\nItem 10000\nItem 20000"
        assertEquals(20000L, ReceiptParser.parse(text).totalAmount)
    }

    @Test
    fun `category guess`() {
        val parsed = ReceiptParser.parse("Kopi Kenangan\nTOTAL 25000")
        assertEquals(
            com.ssajudn.bareuang.domain.model.TransactionCategory.FOOD,
            parsed.suggestedCategory,
        )
    }
}
