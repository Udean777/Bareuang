package com.ssajudn.bareuang.data.service

import org.junit.Assert.*
import org.junit.Test

class CsvMutasiParserTest {
    private val parser = CsvMutasiParser()

    @Test fun `generic comma`() {
        val csv = "foo,bar,baz\n01/01/2026,Top Up GoPay,50000"
        val res = parser.parse(csv)
        assertEquals(1, res.size)
        assertEquals(50000L, res[0].amount)
        assertEquals("2026-01-01", res[0].date)
        assertEquals(com.ssajudn.bareuang.domain.model.TransactionType.INCOME, res[0].type)
    }

    @Test fun `generic negative amount is expense`() {
        val csv = "foo,bar,baz\n01/01/2026,Belanja,-50000"
        val result = parser.parse(csv).single()
        assertEquals(50000L, result.amount)
        assertEquals(com.ssajudn.bareuang.domain.model.TransactionType.EXPENSE, result.type)
    }

    @Test fun `BCA semicolon debit kredit`() {
        val csv = "Tanggal;Keterangan;Debit;Kredit\n02-01-2026;TRF E-BANKING;50000;0"
        val res = parser.parse(csv)
        assertEquals(1, res.size)
        assertEquals(50000L, res[0].amount)
    }

    @Test fun `Rp amount with dots`() {
        assertEquals(1250000L, parser.parseAmount("Rp 1.250.000"))
        assertEquals(1250000L, parser.parseAmount("1.250.000,00"))
    }

    @Test fun `SALDO skip`() {
        val csv = "Tanggal,Keterangan,Jumlah\n01/01/2026,SALDO AWAL,1000000\n02/01/2026,Belanja,50000"
        val (list, skipped) = parser.parseWithStats(csv)
        assertEquals(1, list.size)
        assertEquals(1, skipped)
    }

    @Test fun `date formats`() {
        assertEquals("2026-01-05", parser.parseDate("05/01/2026"))
        assertEquals("2026-01-05", parser.parseDate("2026-01-05"))
        assertEquals("2026-01-05", parser.parseDate("05-01-2026"))
    }

    @Test fun `quoted fields`() {
        val line = "\"01/01/2026\",\"Toko, Besar\",\"50.000\""
        val cols = parser.splitCsvLine(line, ",")
        assertEquals(3, cols.size)
        assertEquals("Toko, Besar", cols[1])
    }

    @Test fun `BOM CRLF and tab separated aliases are detected`() {
        val csv = "\uFEFFTgl\tNarration\tNominal\r\n05/01/2026\tKopi\tRp 25.000\r\n"
        val inspection = parser.inspect(csv)
        assertFalse(inspection.needsMapping)
        val result = parser.parse(csv).single()
        assertEquals("2026-01-05", result.date)
        assertEquals(25000L, result.amount)
    }

    @Test fun `quoted multiline description remains one row`() {
        val csv = "Tanggal,Keterangan,Jumlah\n01/01/2026,\"Kopi,\nSusu\",12.000"
        val result = parser.parse(csv)
        assertEquals(1, result.size)
        assertEquals("Kopi,\nSusu", result.single().merchant)
    }

    @Test fun `unknown columns can be mapped manually`() {
        val csv = "Waktu,Uraian,Nilai\n05 Jan 2026,Honor,1.500.000"
        val inspection = parser.inspect(csv)
        assertTrue(inspection.needsMapping)
        val parsed = parser.parse(
            csv,
            com.ssajudn.bareuang.domain.model.CsvColumnMapping(date = 0, description = 1, amount = 2),
        )
        assertEquals("2026-01-05", parsed.drafts.single().date)
        assertEquals(1500000L, parsed.drafts.single().amount)
    }

    @Test fun `invalid rows return reasons and debit credit conflict`() {
        val csv = "Tanggal,Keterangan,Debit,Kredit\n01/01/2026,Belanja,1000,500\nnot-a-date,Warung,0,2000\n02/01/2026,,0,3000"
        val parsed = parser.parse(csv, parser.inspect(csv).autoMapping)
        assertTrue(parsed.drafts.isEmpty())
        assertEquals(
            listOf(
                com.ssajudn.bareuang.domain.model.CsvRowIssueReason.AMOUNT_CONFLICT,
                com.ssajudn.bareuang.domain.model.CsvRowIssueReason.INVALID_DATE,
                com.ssajudn.bareuang.domain.model.CsvRowIssueReason.MISSING_DESCRIPTION,
            ),
            parsed.issues.map { it.reason },
        )
    }

    @Test fun `decimal thousands and parenthesized negatives parse`() {
        assertEquals(1234567L, parser.parseAmount("IDR 1.234,567"))
        assertEquals(-50000L, parser.parseAmount("(50.000)"))
    }

    @Test fun `balance summary without date is identified as a summary`() {
        val csv = "Tanggal,Keterangan,Jumlah\n,SALDO AWAL,1000000"
        val parsed = parser.parse(csv, parser.inspect(csv).autoMapping)
        assertEquals(com.ssajudn.bareuang.domain.model.CsvRowIssueReason.BALANCE_SUMMARY, parsed.issues.single().reason)
    }

    @Test fun `too many rows are reported as a file limit`() {
        val csv = "Tanggal,Keterangan,Jumlah\n" + (1..5001).joinToString("\n") { "01/01/2026,Toko $it,1000" }
        assertEquals(
            com.ssajudn.bareuang.domain.model.CsvImportInspectionError.TOO_MANY_ROWS,
            parser.inspect(csv).error,
        )
    }
}
