package com.ssajudn.bareuang.utils

import org.junit.Assert.*
import org.junit.Test

class ReceiptParserTest {
    @Test fun `extract total`() {
        val text = "INDOMARET\nTOTAL Rp 45.000\nTunai 50.000"
        val p = ReceiptParser.parse(text)
        assertEquals(45000L, p.totalAmount)
        assertTrue(p.merchantName.contains("INDOMARET", true))
    }

    @Test fun `fallback largest`() {
        val text = "NOTA\nItem 10000\nItem 20000"
        val p = ReceiptParser.parse(text)
        assertEquals(20000L, p.totalAmount)
    }

    @Test fun `category guess`() {
        val p = ReceiptParser.parse("Kopi Kenangan\nTOTAL 25000")
        assertEquals(com.ssajudn.bareuang.domain.model.TransactionCategory.FOOD, p.suggestedCategory)
    }
}
