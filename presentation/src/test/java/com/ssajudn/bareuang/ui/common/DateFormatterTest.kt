package com.ssajudn.bareuang.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class DateFormatterTest {
    private val indonesian = Locale.forLanguageTag("id-ID")

    @Test
    fun `formats ISO date string to day month and year`() {
        val result = DateFormatter.formatDisplayDate("2026-08-19", indonesian)

        assertTrue("Expected Indonesian August abbreviation, got: $result", result.contains("Agu"))
        assertTrue("Expected day 19, got: $result", result.startsWith("19"))
        assertTrue("Expected year 2026, got: $result", result.contains("2026"))
    }

    @Test
    fun `formats ISO timestamp to day and month`() {
        val result = DateFormatter.formatDisplayDate("2026-08-19T14:30:00Z", indonesian)

        assertTrue("Expected Indonesian August abbreviation, got: $result", result.contains("Agu"))
        assertTrue("Expected day 19, got: $result", result.startsWith("19"))
    }

    @Test
    fun `returns unparseable input unchanged`() {
        val raw = "not-a-date"

        assertEquals(raw, DateFormatter.formatDisplayDate(raw, indonesian))
    }
}
