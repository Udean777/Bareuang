package com.ssajudn.bareuang

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class AppSmokeUnitTest {
    @Test
    fun currencyAmountArithmetic_isStable() {
        assertEquals(15000L, 10000L + 5000L)
    }
}
