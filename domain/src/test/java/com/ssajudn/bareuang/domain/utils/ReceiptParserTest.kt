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

    @Test
    fun `supports Indonesian amount formats and final total`() {
        val parsed = ReceiptParser.parse(
            "TOKO\nSUBTOTAL Rp25.000\nDISKON 5.000\nPPN 2.000\nGRAND TOTAL 22,000",
        )

        assertEquals(22000L, parsed.totalAmount)
    }

    @Test
    fun `extracts Indonesian date`() {
        val parsed = ReceiptParser.parse("TOKO\nTanggal: 18/09/2026\nTOTAL 25000")

        assertEquals("2026-09-18", parsed.date)
    }

    @Test
    fun `normalizes OCR letter O in amount`() {
        val parsed = ReceiptParser.parse("TOKO\nTOTAL 2O.000")

        assertEquals(20000L, parsed.totalAmount)
    }

    @Test
    fun `unknown merchant category stays other`() {
        val parsed = ReceiptParser.parse("JASA XYZ\nTOTAL 25000")

        assertEquals(
            com.ssajudn.bareuang.domain.model.TransactionCategory.OTHER,
            parsed.suggestedCategory,
        )
    }
}
