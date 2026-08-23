package com.ssajudn.barebudget.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {

    private val indonesianLocale = Locale("id", "ID")

    fun getCurrentDateISO(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getCurrentMonthYear(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Format "2026-08-19" or ISO timestamp to "19 Agu 2026"
     */
    fun formatDisplayDate(rawDate: String): String {
        return try {
            val inputFormat = if (rawDate.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            }
            val date = inputFormat.parse(rawDate.substring(0, minOf(19, rawDate.length)))
            val outputFormat = SimpleDateFormat("dd MMM yyyy", indonesianLocale)
            if (date != null) outputFormat.format(date) else rawDate
        } catch (e: Exception) {
            rawDate
        }
    }

    /**
     * Calculate days remaining until due date
     */
    fun getDaysUntilDue(dueDateString: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dueDate = sdf.parse(dueDateString.substring(0, 10)) ?: return 0
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val diffMillis = dueDate.time - today.time
            TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Human readable countdown e.g. "3 hari lagi", "Hari ini", "Terlewat 2 hari"
     */
    fun getDueStatusMessage(dueDateString: String): String {
        val days = getDaysUntilDue(dueDateString)
        return when {
            days > 1 -> "$days hari lagi"
            days == 1L -> "Besok"
            days == 0L -> "Jatuh tempo hari ini!"
            else -> "Terlewat ${Math.abs(days)} hari"
        }
    }

    /**
     * Calculates the next due date based on recurring interval
     */
    fun calculateNextDueDate(currentDueDateStr: String, interval: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(currentDueDateStr.substring(0, 10)) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }

            when (interval.uppercase()) {
                "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                "MONTHLY" -> cal.add(Calendar.MONTH, 1)
                "YEARLY" -> cal.add(Calendar.YEAR, 1)
                else -> return currentDueDateStr
            }

            sdf.format(cal.time)
        } catch (e: Exception) {
            currentDueDateStr
        }
    }

    /**
     * Calculates next occurrence matching a target day of week (1 = Monday ... 7 = Sunday)
     */
    fun calculateNextWeeklyDay(fromDateStr: String, targetDayOfWeek: Int): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(fromDateStr.substring(0, minOf(10, fromDateStr.length))) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }

            // Convert Calendar.DAY_OF_WEEK (Sunday=1, Monday=2..Saturday=7) to ISO (Monday=1..Sunday=7)
            val currentCalDay = cal.get(Calendar.DAY_OF_WEEK)
            val currentIsoDay = if (currentCalDay == Calendar.SUNDAY) 7 else currentCalDay - 1

            var daysToAdd = (targetDayOfWeek - currentIsoDay + 7) % 7
            if (daysToAdd == 0) daysToAdd = 7 // next week
            cal.add(Calendar.DAY_OF_MONTH, daysToAdd)
            sdf.format(cal.time)
        } catch (_: Exception) {
            calculateNextDueDate(fromDateStr, "WEEKLY")
        }
    }

    /**
     * Calculates next occurrence matching a target day of month (1..31)
     */
    fun calculateNextMonthlyDate(fromDateStr: String, targetDayOfMonth: Int): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(fromDateStr.substring(0, minOf(10, fromDateStr.length))) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }

            cal.add(Calendar.MONTH, 1)
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, targetDayOfMonth.coerceIn(1, maxDay))
            sdf.format(cal.time)
        } catch (_: Exception) {
            calculateNextDueDate(fromDateStr, "MONTHLY")
        }
    }

    /**
     * Extracts ISO day of week (1=Monday ... 7=Sunday) from YYYY-MM-DD
     */
    fun getDayOfWeek(dateStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr.substring(0, minOf(10, dateStr.length))) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }
            val calDay = cal.get(Calendar.DAY_OF_WEEK)
            if (calDay == Calendar.SUNDAY) 7 else calDay - 1
        } catch (_: Exception) {
            1
        }
    }

    /**
     * Extracts Day of Month (1..31) from YYYY-MM-DD
     */
    fun getDayOfMonth(dateStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr.substring(0, minOf(10, dateStr.length))) ?: Date()
            val cal = Calendar.getInstance().apply { time = date }
            cal.get(Calendar.DAY_OF_MONTH)
        } catch (_: Exception) {
            1
        }
    }

    fun parseIsoToMillis(isoDate: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.parse(isoDate.substring(0, 10))?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    fun formatMillisToIso(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        // Avoid timezone shift issues with date picker
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(millis))
    }
}
