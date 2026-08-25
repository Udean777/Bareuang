package com.ssajudn.barebudget.utils

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Date helpers built on java.time (safe: minSdk 26).
 *
 * Contract: functions taking an ISO date string expect `yyyy-MM-dd` (an optional
 * `T…` timestamp suffix is tolerated) and THROW [IllegalArgumentException] on
 * unparseable input — silent fallbacks masked real bugs. The one exception is
 * [formatDisplayDate], which returns the raw input so corrupt values remain
 * visible in the UI instead of crashing a list render.
 */
object DateUtils {

    private val indonesianLocale = Locale("id", "ID")
    private val displayFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", indonesianLocale)

    /** ISO local date of "today". */
    fun getCurrentDateISO(): String = LocalDate.now().toString()

    fun getCurrentMonthYear(): String = YearMonth.now().toString()

    private fun parseLocalDate(isoDate: String): LocalDate =
        LocalDate.parse(isoDate.substring(0, 10))

    /**
     * Format "2026-08-19" or ISO timestamp to "19 Agu 2026".
     * Returns the raw input unchanged when unparseable (display-only concern).
     */
    fun formatDisplayDate(rawDate: String): String =
        try {
            parseLocalDate(rawDate).format(displayFormat)
        } catch (_: Exception) {
            rawDate
        }

    /**
     * Days remaining until due date (negative = past). Throws on unparseable input.
     */
    fun getDaysUntilDue(dueDateString: String): Long =
        ChronoUnit.DAYS.between(LocalDate.now(), parseLocalDate(dueDateString))

    /**
     * Calculates the next due date based on recurring interval.
     */
    fun calculateNextDueDate(currentDueDateStr: String, interval: String): String {
        val date = parseLocalDate(currentDueDateStr)
        val next = when (interval.uppercase()) {
            "WEEKLY" -> date.plusWeeks(1)
            "MONTHLY" -> date.plusMonths(1)
            "YEARLY" -> date.plusYears(1)
            else -> return currentDueDateStr
        }
        return next.toString()
    }

    /**
     * Calculates next occurrence matching a target day of week (1 = Monday ... 7 = Sunday)
     */
    fun calculateNextWeeklyDay(fromDateStr: String, targetDayOfWeek: Int): String {
        val date = parseLocalDate(fromDateStr)
        // ISO day-of-week is already Monday=1..Sunday=7.
        var daysToAdd = (targetDayOfWeek - date.dayOfWeek.value + 7) % 7
        if (daysToAdd == 0) daysToAdd = 7 // next week
        return date.plusDays(daysToAdd.toLong()).toString()
    }

    /**
     * Calculates next occurrence matching a target day of month (1..31)
     */
    fun calculateNextMonthlyDate(fromDateStr: String, targetDayOfMonth: Int): String {
        val nextMonth = parseLocalDate(fromDateStr).plusMonths(1)
        val maxDay = nextMonth.lengthOfMonth()
        return nextMonth.withDayOfMonth(targetDayOfMonth.coerceIn(1, maxDay)).toString()
    }

    /**
     * Extracts ISO day of week (1=Monday ... 7=Sunday) from YYYY-MM-DD
     */
    fun getDayOfWeek(dateStr: String): Int = parseLocalDate(dateStr).dayOfWeek.value

    /**
     * Extracts Day of Month (1..31) from YYYY-MM-DD
     */
    fun getDayOfMonth(dateStr: String): Int = parseLocalDate(dateStr).dayOfMonth

    /**
     * Millis at UTC midnight of the given date, or null when unparseable.
     * UTC keeps the date-picker round-trip stable across device timezones.
     */
    fun parseIsoToMillis(isoDate: String): Long? =
        try {
            parseLocalDate(isoDate).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        } catch (_: Exception) {
            null
        }

    fun formatMillisToIso(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
}
